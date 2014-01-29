package buildingsimporter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		System.out.println("Removing duplicates...");

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
	 * Получить id термина из словаря "Города и улицы" для одного здания.
	 * @param building - Здание
	 * @param taxonomer - словарь терминов
	 * @return id термина в словаре или 0 при ошибке.
	 */
	public int match_building_addr_term(Element building, KPGTaxonomer taxonomer) {
		String areaName = building.getChildText("areaName"); //Название района, полученное из ссылки на страницу района
		String locationName = building.getChildText("locationName"); //Название нас.пункта, полученное из ссылки на страницу района
		String location = building.getChildText("location"); //Название нас.пункта, полученное парсингом адреса. !! может не совпадать с locationName
		String street = building.getChildText("street"); //Название улицы.
		
		if (street != null && !street.isEmpty()) {
			// поправить имена улиц и нас. пунктов, когда улица задана видом "(Лапсарского с/п) ул Восточная"
	        Pattern specialLocStreet = Pattern.compile("(\\([^)]+\\)) (.*)$");
	        Matcher m = specialLocStreet.matcher(street);
	        if (m.matches()) {
	    		String subLocName = m.group(1);
	    		String subLocStreet = m.group(2);
	    		street = subLocStreet;
	    		location += " "+subLocName; 
	        }
		}
		
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
				int parsedCombined = likeness(fixedLocation+fixedLocationName, fixedTermName)+5; // иногда терм называется типа "д.Чиршкасы (Сирмапосинского с/п)" - т.е. loc+locName
				int weight = Math.max(Math.max(linkNameWeight, parsedNameWeight), parsedCombined);
				
				if (weight > maxWeight && weight>50) 
					maxWeight = weight;
				
				matchingWeightsList.add(new TermIdWeightPair(term.id, weight));
			}
			
			// Построение списка близко совпавших, отсортированного по убыванию близости
			
			// Фильтрация по близости.
			List<TermIdWeightPair> removeList = new LinkedList<>(); 
			for (TermIdWeightPair entry: matchingWeightsList) 
				if (entry.weight+5 < maxWeight) 
					removeList.add(entry);
			
			matchingWeightsList.removeAll(removeList);
			
			// Anonymous class. Yeah, baby, like a pro.
			Collections.sort(matchingWeightsList,
					new Comparator<TermIdWeightPair>() {
						@Override
						public int compare(TermIdWeightPair o1, TermIdWeightPair o2) {
							return o2.weight-o1.weight; // по убыванию
						}
					}
			);
			
			// {id, weight} list -> {term} list
			for (TermIdWeightPair entry: matchingWeightsList) 
				closeLocationMatches.add(subset.get(entry.id));
			
			if (matchingWeightsList.size() == 0) {
				System.err.println("Can't match location for "+areaName+": "+locationName+" ("+location+")");
				return 0;
			} else if (matchingWeightsList.size()>1) {
				// нашли несколько близких, но ни одного идеального
				System.out.println("-------------------");
				KPGTerm first = closeLocationMatches.getFirst();
				System.out.println("Location "+areaName+"|"+locationName+"("+location+")"+" was matched to "+first.name);
				for (TermIdWeightPair closeIdWPair: matchingWeightsList) {
					if (closeIdWPair.id == first.id) continue;
					System.out.println("  .. but there may be chance ("+(closeIdWPair.weight-maxWeight)+") it is "
							+subset.get(closeIdWPair.id).name);
				}
			}
			
			locationId = closeLocationMatches.getFirst().id;
		} // получение locationId и closeLocationMatches по известному areaId 
		else {
			// добавить locationId в список closeLocationMatches
			closeLocationMatches.add(taxonomer.terms.get(locationId));
		} 
		
		if (street==null || street.isEmpty()) 
			return locationId;

		if (street.equals("п Октябрьский ул Лесхозная")) {
			street = street+"";
		}
		//1. Проверка особой ситуации, когда улица указана как "п Октябрьский ул Лесхозная" (в Чебоксарах)
		int checkRes = checkSpecialStreet(street, taxonomer);
		if (checkRes != -1) 
			return checkRes; // возврат результата сопоставления, если это действительно особая ситуация.

		//2. Постараемся найти подходящую улицу для дома в дочерних тегах от locationId, если не получится, то в ближайших подходящих
		
		// проверяем каждый нас. пункт из совпавших ранее.
		for (KPGTerm loc: closeLocationMatches) {
			subset = taxonomer.chooseForParent(loc.id); // список улиц
			
			// проверяем улицы в нас. пункте.
			int maxWeight = 0;
			for (KPGTerm streetTerm: subset.values()) {
				int weight = likeness(street, streetTerm.name);
				if (weight>50) {
					weight = weight + 0;
				}
				if (weight > maxWeight && weight>50) {
					maxWeight = weight;
					streetId = streetTerm.id;
				}				
			}
			
			if (streetId != 0) 
				return streetId;
		}
		
		System.out.println("Can't match a street: "+areaName+"|"+locationName+"("+location+") "+street);
		return 0;
	}
	
	/**
	 * Простое сравнение строки с именами терминов
	 * @param needle - строка для поиска
	 * @param haystack - набор терминов
	 * @return 0 если удовлетворительного термина не найдено, иначе id наиболее подходящего термина.
	 */
	private int simlpeTermMatch(String needle, HashMap<Integer,KPGTaxonomer.KPGTerm> haystack) {
		int maxWeight = 0; int resultId = 0;
		needle = prepareName(needle);
		for (KPGTerm term: haystack.values()) {
			int weight = likeness(needle, term.name);
			
			if (weight>maxWeight && weight>50) {
				maxWeight = weight;
				resultId = term.id;
			}				
		}
		
		return resultId;
	}
	
	/**
	 * Проверяет особую ситуацию, когда для города Чебоксары улица задана в виде "п Октябрьский ул Лесхозная"
	 * @param street - строка улицы
	 * @param taxonomer - хранилище терминов
	 * @return -1, если строка не является особой ситуацией
	 *   		0, если строка является особой ситуацией, но совпадений не найдено
	 *   	   id термина улицы, если удалось сопоставить особую ситуацию.
	 */
	private int checkSpecialStreet(String street, KPGTaxonomer taxonomer) {
        Pattern specialLocStreet = Pattern.compile("п ([^ ]+) (.*)$");
        Matcher m = specialLocStreet.matcher(street);
        if (m.matches()) {
    		String subLocName = "п " + m.group(1);
    		String subLocStreet = m.group(2);
    		
    		HashMap<Integer,KPGTaxonomer.KPGTerm> subset = taxonomer.chooseForParent(17004); //посёлки
    		int subLocId = simlpeTermMatch(subLocName, subset);
    		if (subLocId == 0)
    			return 0;
    		
    		subset = taxonomer.chooseForParent(subLocId); // теперь улицы
    		int subLocStreetId = simlpeTermMatch(subLocStreet, subset);
			return subLocStreetId;
        }
        
		return -1;
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
	 * Получить термины таксономии и ID нод домов для адресов всех зданий.
	 * Изменяет здания в кэше, добавляя поле "streetTermId" с номером термина из таксономии.
	 * @param taxonomer
	 */
	public void match_buildings_streets(KPGTaxonomer taxonomer) {
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("Matching buildings!");
		
		for (Element buildingElement: allBuildings) {
			if (--counter%1000 ==0 )
				System.out.println(counter);
			int termId = match_building_addr_term(buildingElement, taxonomer);

			if (termId != 0) {
				Element stID = buildingElement.getChild("streetTermId");
				if (stID == null) { 
					stID = new Element("streetTermId"); // новый подчинённый элемент для сохранения ID термина адреса.
					buildingElement.addContent(stID);
				}
				stID.setText(String.valueOf(termId));
			}
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
			
//			String expl_year = buildingElement.getChildText("expl_year");
			String flats = buildingElement.getChildText("flats");
			String porches = buildingElement.getChildText("porches");
			String floors = buildingElement.getChildText("floors");
			String walls = buildingElement.getChildText("walls");
			String lifts = buildingElement.getChildText("lifts");
			
			if (//(expl_year==null || expl_year.isEmpty() || expl_year.equals("нет данных")) &&
				(flats==null || flats.isEmpty() || flats.equals("нет данных")) &&
				(porches==null || porches.isEmpty() || porches.equals("нет данных")) &&
				(floors==null || floors.isEmpty() || floors.equals("нет данных")) &&
				(walls==null || walls.isEmpty() || walls.equals("нет данных")) &&
				(lifts==null || lifts.isEmpty() || lifts.equals("нет данных"))
			) {
				buildingElement.detach();
				emptyDataBuildingsCounter++;
			}
		}
		System.out.println("Removed "+emptyDataBuildingsCounter+" buildings");
	}

	/**
	 * Поправляет строку номера дома, полученного из базы, чтобы можно было совместить номер дома базы с номером дома из парсера 
	 * @param source - строка полученная из базы
	 * @return строка для сравнения.
	 */
	public static
	String prepareDatabaseBnum(String source) {
		String s = source.toLowerCase().replace("двлд", "").replace("влд", "");
		Pattern p; Matcher m;

		p = Pattern.compile("(.*)(\\.|стр_|сооружение_)");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1);
		
		p = Pattern.compile("(сооружение|стр)([^а-я].*)");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(2);
		
		p = Pattern.compile("(\\d+)(стр|литер|сооружение)([а-я])");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1)+m.group(3); 
		
		p = Pattern.compile("(\\d+)_([а-я])");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1)+m.group(2); 

		p = Pattern.compile("(.*\\d)к([а-я])$");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1)+m.group(2);
		return s;
	}	
	
	/**
	 * Поправляет строку номера дома полученного из парсера, чтобы можно было совместить номер дома базы с номером дома из парсера 
	 * @param source - строка полученная из парсера
	 * @return строка для сравнения.
	 */
	public static
	List<String> prepareParsedBnum(String source) {
		String s = source.toLowerCase().replace("a", "а").replace(" строение", "").replace(" жилое", "").replace("подъезд", "п.")
				.replace(" жилой", "").replace(" дом", "");
		
		Pattern p; Matcher m;
		List<String> rvalue = new LinkedList<>(); // Список возвращаемых вариантов (чаще 1)

		
		p = Pattern.compile("([^\"]*)\"([^\"]*)\"(.*)?"); // литера в кавычках.
		m = p.matcher(s);
		if (m.matches()) { 
			//  4"a"  --> 4a
			if (m.group(3) != null)
				s = m.group(1)+m.group(2)+m.group(3);
			else
				s = m.group(1)+m.group(2);
		}
		
		p = Pattern.compile("(\\d+)\\s+([а-я])"); // литера отделена пробелом
		m = p.matcher(s);
		if (m.matches()) { 
			//  4 a  --> 4a
			s = m.group(1)+m.group(2);
		}
		
		p = Pattern.compile("(\\d+ кор.[а-я])\\d+"); // повторение(другой) номер после корпуса
		m = p.matcher(s);
		if (m.matches()) { 
			//  30 кор.а30 -> 30 кор.а 
			s = m.group(1);
		}
		
		p = Pattern.compile("(\\d+) кор.([а-я])");
		m = p.matcher(s);
		if (m.matches()) { 
			//  30 кор.а -> 30а 
			s = m.group(1)+m.group(2);
		}
		
		p = Pattern.compile("(\\d+) кор.(\\d+)"); 
		m = p.matcher(s);
		if (m.matches()) { 
			//  30 кор.1 -> 30к1 
			rvalue.add(m.group(1)+"к"+m.group(2));
			rvalue.add(m.group(1)+"/"+m.group(2));
			return rvalue;
		}			

		rvalue.add(s);
		
		p = Pattern.compile("([^/]*)/([^ ]*)( .*)?"); // нумерация по перпендикулярной улице.
		m = p.matcher(s);
		if (m.matches()) { 
			// "a/b c" --> "aкb c", "a c"
			if (m.group(3) != null) {
				rvalue.add(m.group(1)+"к"+m.group(2)+m.group(3));
				rvalue.add(m.group(1)+m.group(3));
			} else {
				rvalue.add(m.group(1)+"к"+m.group(2));
				rvalue.add(m.group(1));
			}
		}
		
		return rvalue;
	}
	
	//TODO
	/**
	 * Совмещает данные кэша с базой домов, определяя для каждого здания из кэша номер соответствующей ноды.
	 * Создаёт для каждого элемента <Building> в кэше создаёт один или несколько
	 * дочерних элементов "houseId" и сохраняет туда номер ноды, если найден.
	 * @param addrID_houseIDs - отображение {ID улицы -> {ID дома1, ID дома2, ..}}
	 * @param houseID_bnum - отображение {ID дома -> номер дома}
	 */
	public void match_buildings_number(Map<Integer, Collection<Integer>> addrID_houseIDs, Map<Integer, String> houseID_bnum) {
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("Matching buildings' numbers.");
		
		// Дополнительные прочтения bNum из базы
		Map<Integer, String> houseID_bnum1 = new HashMap<>();
		Map<Integer, String> houseID_bnum2 = new HashMap<>();
		
		// составим карты дополнительных прочтений номера, где возможно: "30/12" -> "30к12" и "30"
		for (Entry<Integer, String> entry: houseID_bnum.entrySet()) {
			Pattern extraBNumPattern = Pattern.compile("(\\d+)/(\\d+)");
			Matcher m = extraBNumPattern.matcher(entry.getValue());
			
			if (m.matches()) {
				houseID_bnum1.put(entry.getKey(), m.group(1)+"к"+m.group(2));
				houseID_bnum2.put(entry.getKey(), m.group(1));
			}
		}
		
		int successCounter = 0;
		//1 Для каждого дома...
		for (Element buildingElement: allBuildings) {
			if (--counter%1000 ==0 )
				System.out.println(counter);
			
			int nodeId = 0;
			
			// получим ID адреса дома.
			int addrID = 0;
			try {
				addrID = Integer.parseInt(buildingElement.getChildText("streetTermId"));
			} catch (NumberFormatException e) {
				// не  удалось прочитать id термина адреса - возможно он не был сопоставлен.
				continue;
			}
			
			// определим все дома на его улице в базе,
			Collection<Integer> houseIDs = addrID_houseIDs.get(addrID);
			if (houseIDs == null || houseIDs.size()==0)
				continue; //у этого термина нет домов, как странно!
			
			// и для каждого дома попробуем сопоставить его номер.
			List<String> sourceNums = prepareParsedBnum(buildingElement.getChildText("building_number"));
			LinkedList<Integer> matchedIDs = new LinkedList<>();
			
			if (sourceNums.size() == 0) {
				System.err.println("can't generated building num variants for "+buildingElement.getChildText("areaText")+" | "+
						buildingElement.getChildText("locationName")+" ("+buildingElement.getChildText("location")+
						") | "+buildingElement.getChildText("street")
						+", "+buildingElement.getChildText("building_number"));
				continue; // следующее здание.
			}
			
			// берём все возможные прочтения номера этого дома из парсера. (N1/N2 --> N1/N2, N1кN2, N1)
			for (String sourceNum : sourceNums) {
				// и каждое прочтение сверяем с базой
				for (int houseID: houseIDs) {
					String targetNum = prepareDatabaseBnum(houseID_bnum.get(houseID));
					
					if (targetNum.equals(sourceNum)) {
						matchedIDs.add(houseID);
					} 
				}
				
				if (matchedIDs.size() >0 ) 
					break; // достаточно, чтобы совпало хотя бы одно прочтение.
			}
			
			String sourceNumFirst = sourceNums.get(0);
			
			if (matchedIDs.size() == 0 ) {
				//ай-ай, не нашли. попробует тогда альтенативное прочтения 1 по базе. 
				// Теперь берём только основное прочтение дома из парсера.
				
				for (int houseID: houseIDs) {
					if (houseID_bnum1.containsKey(houseID)) {
						String targetNum = prepareDatabaseBnum(houseID_bnum1.get(houseID));
						
						if (targetNum.equals(sourceNumFirst)) 
							matchedIDs.add(houseID);
					}
				}
			}
			
			if (matchedIDs.size() == 0 ) {
				//ай-ай, снова не нашли. попробует тогда альтенативное прочтения 2 по базе. 
				// Теперь берём только основное прочтение дома из парсера.
				
				for (int houseID: houseIDs) {
					if (houseID_bnum2.containsKey(houseID)) {
						String targetNum = prepareDatabaseBnum(houseID_bnum2.get(houseID));
						
						if (targetNum.equals(sourceNumFirst)) 
							matchedIDs.add(houseID);
					}
				}				
			}
			
			if (matchedIDs.size() == 1) {
				nodeId = matchedIDs.getFirst();
				
				// removing old data
				List<Element> houseIDChildren = buildingElement.getChildren("houseId"); 
				for (Element ch: houseIDChildren) 
					ch.detach();
				
				// новый подчинённый элемент для сохранения ID термина адреса.
				Element stID = new Element("houseId"); 
				stID.setText(String.valueOf(nodeId));
				buildingElement.addContent(stID);
				
				successCounter++;
			} else if (matchedIDs.size()==2 && houseID_bnum.get(matchedIDs.getFirst()).equals(
					houseID_bnum.get(matchedIDs.getLast()))) {
				// дубликаты в базе - сохраняем оба ID
				
				// removing old data
				List<Element> houseIDChildren = buildingElement.getChildren("houseId"); 
				for (Element ch: houseIDChildren) 
					ch.detach();
				
				// новый подчинённый элемент для сохранения ID термина адреса.
				Element stID = new Element("houseId"); 
				stID.setText(String.valueOf(matchedIDs.getFirst()));
				buildingElement.addContent(stID);
				
				stID = new Element("houseId"); 
				stID.setText(String.valueOf(matchedIDs.getLast()));
				buildingElement.addContent(stID);
				
				successCounter++;
			} else {
				System.out.print(""+buildingElement.getChildText("areaText")+" | "+
						buildingElement.getChildText("locationName")+" ("+buildingElement.getChildText("location")+
						") | "+buildingElement.getChildText("street")
						+", "+buildingElement.getChildText("building_number") + 
						"("+sourceNums.get(0)+") <"+matchedIDs.size()+">");
				for (Integer hID: matchedIDs) {
					System.out.print(" {"+houseID_bnum.get(hID)+"}");
				}
				System.out.println();

			} //if (nodeId != 0)
		} //for (Element buildingElement: allBuildings)
		
		System.out.println(""+successCounter+" out of "+allBuildings.size()+" were matched");
	}
	
}
