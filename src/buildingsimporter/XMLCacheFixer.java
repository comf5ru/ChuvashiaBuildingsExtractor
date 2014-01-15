package buildingsimporter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.jdom2.Element;

import buildingsextractor.XMLCache;
import buildingsimporter.KPGTaxonomer.KPGTerm;

/**
 * Позволяет вносить изменения посредством XPath манипуляций в кэш,
 * подготавливая данные для совместимости с импортом 
 */
public class XMLCacheFixer extends XMLCache {

	public XMLCacheFixer(String xmlFileName) {
		super(xmlFileName);
	}

	private static final String ALL_BUILDINGS = "/root/Building";
	private static final String ALL_BUILDINGS_FOR_ADDRESS = "/root/Building[1=1 %s]";
	
	public void kill_duplicates() {
		Element root = doc.getRootElement();
		Element newRoot = new Element("root");
		
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("About to go cray in this mess!");

		for (Element buildingElement: allBuildings) {
			if (--counter%100 ==0 )
				System.out.println(counter);
			
			if (buildingElement.getParent() == null) 
				continue; // already detached earlier - т.е. адрес совпал с кем-то предыдущим и был обработан.
			
			//location='%s' and street='%s' and building_number='%s'
			String street = buildingElement.getChildText("street");
			String location = buildingElement.getChildText("location");
			String bnum = buildingElement.getChildText("building_number");
			
			// нет дома или нас.пункта
			if ((location == null) || (bnum == null))
				System.out.println("Something is missing! "+buildingElement.getAttributeValue("url")
						+" : "+location+"|"+street+"|"+bnum);

			// коррекция запроса с учётом возможной неполноты данных
			String conditions = "";
			if (location != null) 
				conditions += "and location='"+location+"'";
			if (street != null) 
				conditions += "and street='"+street+"'";
			if (bnum != null) 
				conditions += "and building_number='"+bnum+"'";
			
			// Все дома с идентичным адресом
			Collection<Element> sameAddress = queryXPathList(String.format(ALL_BUILDINGS_FOR_ADDRESS, conditions));
			
			if (sameAddress.size() > 1) {
				System.out.println("Same address buildings <" +location+ " " + street + " " + bnum + ">: " + sameAddress.size());
				// собираем в кучку все данные с записей о домах с совпадающим адресом.
				Element combined = new Element("Building");
				for (Element buildingSA: sameAddress) {
					buildingSA.detach();
					List<Element> properties = buildingSA.getChildren();
					for (Element p: properties) {
						String name = p.getName();
						String value = p.getText();
						if (value.equals("нет данных") || value.isEmpty())
							continue; //не обновлять пустыми данными
						Element updatedProperty = combined.getChild(name);
						if (updatedProperty == null) {
							updatedProperty = new Element(name);
							combined.addContent(updatedProperty);
						}
						updatedProperty.setText(value); //обновить
					}
				}
				newRoot.addContent(combined);
			} else if (sameAddress.size() == 0) {
				System.out.println("WHoopsy-daisy! where did it go? " + location + "-" + street + "-" + bnum);
			} else {
				// только один дом для данного адреса
				buildingElement.detach();
				newRoot.addContent(buildingElement);
			}
			
		} // for allBuildings
		
		assert(root.getChildren().size() == 0);
		doc.setRootElement(newRoot);
	}

	/**
	 * Получить список всех районов Чувашии
	 * @return Collection<String> - названия районов.
	 */
	public Collection<String> getAllAreas() {
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("Collecting areas!");

		Collection<String> allAreas = new HashSet<>();
		for (Element buildingElement: allBuildings) {
			if (--counter%100 ==0 )
				System.out.println(counter);
			String areaName = buildingElement.getChildText("areaName");
			if (areaName != null && !areaName.isEmpty()) 
				allAreas.add(areaName);
		}
		return allAreas;
	}
	
	/**
	 * Получить id термина из словаря "Города и улицы" для одного здания. НЕ ОКОНЧЕНО //TODO
	 * @param building - Здание
	 * @param taxonomer - словарь терминов
	 * @return id термина в словаре или 0 при ошибке.
	 */
	public int match_building_addr_term(Element building, KPGTaxonomer taxonomer) {
		String areaName = building.getChildText("areaName"); //Название района, полученное из ссылки на страницу района
		String locationName = building.getChildText("locationName"); //Название нас.пункта, полученное из ссылки на страницу района
		String location = building.getChildText("location"); //Название нас.пункта, полученное парсингом адреса. !! может не совпадать с locationName
		String street = building.getChildText("street"); //Название улицы.
		
//		String url = building.getChildText("url");
		
		int areaId = 0;
		int locationId = 0;
		// близжайшие похожие с заданной погрешностью по названию нас. пункты 
		LinkedList<KPGTerm> closeLocationMatches = new LinkedList<>();
		int streetId = 0;
		
		// верхний уровень - район
		HashMap<Integer,KPGTaxonomer.KPGTerm> subset = taxonomer.chooseForParent(0);
		if (areaName != null && !areaName.isEmpty()) {
			//"Алатырский муниципальный район" --> "р-н.Алатырский"
			String newAreaName = "р-н."+areaName.replace(" муниципальный район", "");
			areaId = taxonomer.findByName(subset, newAreaName);
			if (areaId == 0) {
				System.err.println("Area term not found: "+areaName);
				return 0;
			}
		}
		
		// Верхний уровень, сразу город
		if (areaName == null) {
			String newLocationName = locationName.replace("Город ", "г.");
			locationId = taxonomer.findByName(subset, newLocationName);
			if (locationId == 0) {
				System.err.println("Non-area town term not found: "+locationName);
				return 0;
			}
		}
		
		// теперь населённый пункт в районе (если areaId != 0)
		if (locationId == 0) {
			if (areaId == 0) {
				System.err.println("Something is very wrong. No area code and no location code");
				return 0;
			}
			
			//Все населённые пункты данного района
			subset = taxonomer.chooseForParent(areaId);
			
			// подготовим строки для сравнения
			String fixedLocationName = prepareName(locationName);
			String fixedLocation = prepareName(location);

			// Отображение id -> weight
			class TermIdWeightPair {
				int id;	int weight;
				TermIdWeightPair(int i, int w) {id = i; weight = w;};
			}
			List<TermIdWeightPair> matchingWeightsList = new LinkedList<>(); 
			
			// перебор терминов и поиск максимально подходящего
			int maxWeight = 0;
			for (KPGTerm term: subset.values()) {
				String fixedTermName = prepareName(term.name);
				// коэффициенты похожести 
				int linkNameWeight = likeness(fixedLocationName, fixedTermName);
				int parsedNameWeight = likeness(fixedLocation, fixedTermName);
				int parsedCombined = likeness(fixedLocation+fixedLocationName, fixedTermName); // иногда терм называется типа "д.Чиршкасы (Сирмапосинского с/п)" - т.е. loc+locName
				int weight = Math.max(Math.max(linkNameWeight, parsedNameWeight), parsedCombined);
				
				if (weight > maxWeight && weight>50) {
					maxWeight = weight;
					locationId = term.id;
				}
				matchingWeightsList.add(new TermIdWeightPair(term.id, weight));
			}
			
			// Построение списка близко совпавших, отсортированного по убыванию близости
			
			// Фильтрация по близости.
			List<TermIdWeightPair> removeList = new LinkedList<>(); 
			for (TermIdWeightPair entry: matchingWeightsList) 
				if (entry.weight+5 < maxWeight || entry.id==locationId)
					removeList.add(entry);
			
			matchingWeightsList.removeAll(removeList);
			
			// anonymous class. I'm a cool kid now.
			Collections.sort(matchingWeightsList,
					new Comparator<TermIdWeightPair>() {
						@Override
						public int compare(TermIdWeightPair o1, TermIdWeightPair o2) {
							return o1.weight-o2.weight;
						}
					}
			);
			
			// {id, weight} list -> {term} list
			for (TermIdWeightPair entry: matchingWeightsList) 
				closeLocationMatches.add(subset.get(entry.id));
			
			if (locationId == 0) {
				System.err.println("Can't match a location: "+areaName+"|"+locationName);
				return 0;
			} else if (matchingWeightsList.size()>0) {
				// нашли несколько близких, но ни одного идеального
				System.out.println("-------------------");
				System.out.println("Location "+areaName+"|"+locationName+"("+location+")"+" was matched to "+subset.get(locationId).name);
				for (TermIdWeightPair closeTerm: matchingWeightsList)
					System.out.println("  .. but there may be chance ("+(closeTerm.weight-maxWeight)+") it is "
							+subset.get(closeTerm.id).name);
			}
		} // получение locationID по известному areaID
		
		if (locationId == 0) {
			System.err.println("Can't match location for "+areaName+": "+locationName+" ("+location+")");
			return 0;
		}

		//TODO сопоставить улицы
		
		return 0;
	}
	
	/**
	 * Подготавливает строку для сравнения с другой строкой, удаляя неважные символы.
	 * @param name
	 * @return
	 */
	public String prepareName(String name) {
		return name.replace(" ", "").replace(".","").replace(",","").replace("/", "").replace("(", "").replace(")","");
	}
	
	/**
	 * Получить термины таксономии для адресов всех зданий. НЕ ОКОНЧЕНА. //TODO
	 * @param taxonomer
	 */
	public void match_all_buildings(KPGTaxonomer taxonomer) {
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("Matching buildings!");

		for (Element buildingElement: allBuildings) {
			if (--counter%1000 ==0 )
				System.out.println(counter);
			int termId = match_building_addr_term(buildingElement, taxonomer);
		}
	}
	
	/**
	 * Функция "умного" сравнения двух строк. Порядок строк важен, можно сказать, что ищется "похожесть" строки2 на строку1. 
	 * @param str1 - первая строка
	 * @param str2 - вторая строка
	 * @return оценка похожести в процентах 0..100, чем больше - тем более строки похожи.
	 */
	static
	int likeness(String str1, String str2) {
		int l1 = str1.length();
		int l2 = str2.length();
		
		int max_likeness = 0;
		for (int ofs=l1; ofs <= l1+l2-4; ofs++) {
			int start1 = ofs - l2; if (start1<0) start1 = 0;
			int end1 = (ofs>l1)? l1: ofs;
			int start2 = l2-ofs; if (start2<0) start2 = 0;
			int end2 = l2-(ofs-l1); if (end2 > l2) end2 = l2;
			
			assert (end1-start1 == end2-start2);

			int count_equals = 0; //Число совпавших символов
			for (int i = 0; i< end1-start1; i++)
				if (str1.charAt(start1+i) == str2.charAt(start2+i))
					count_equals++;
			
			max_likeness = Math.max(max_likeness, count_equals); 
		}
		
		int likeness_coeff = max_likeness*100/(Math.max(l1, l2));
		if (max_likeness>5)
			likeness_coeff += max_likeness/3; // бонусные баллы за длинное совпадение. 
		return likeness_coeff;
	}
	
	/**
	 * Удалить дома, где нет данных.
	 */
	public void dropEmpty() {
		int emptyDataBuildingsCounter = 0;
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("Removing buildings with no data!");

		for (Element buildingElement: allBuildings) {
			if (--counter%1000 ==0 )
				System.out.println(counter);
			
			String expl_year = buildingElement.getChildText("expl_year");
			String flats = buildingElement.getChildText("flats");
			String porches = buildingElement.getChildText("porches");
			String floors = buildingElement.getChildText("floors");
			String walls = buildingElement.getChildText("walls");
			String lifts = buildingElement.getChildText("lifts");
			
			if ((expl_year==null || expl_year.equals("нет данных")) &&
				(flats==null || flats.equals("нет данных")) &&
				(porches==null || porches.equals("нет данных")) &&
				(floors==null || floors.equals("нет данных")) &&
				(walls==null || walls.equals("нет данных")) &&
				(lifts==null || lifts.equals("нет данных"))
			) {
				buildingElement.detach();
				emptyDataBuildingsCounter++;
			}
		}
		System.out.println("Removed "+emptyDataBuildingsCounter+" buildings");
	}
}
