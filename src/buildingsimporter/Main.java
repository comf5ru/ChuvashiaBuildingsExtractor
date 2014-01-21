package buildingsimporter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;

import buildingsimporter.KPGTaxonomer.KPGTerm;

/**
 * Главный класс для запуска преобразования XML -> mysql
 * (получение файла для импорта в Drupal из файла с данными) 
 *
 */
public class Main {

	public static XMLCacheFixer cache;
	public static void main(String[] args) throws IOException {
		KPGTaxonomer taxonomer = new KPGTaxonomer("kpg_locations_data_ids.txt");
		cache = new XMLCacheFixer("4import_Buildings_Chuvashia.xml");
		
		//Получение классов домов в кэше.
		Collection<Element> allBuildings = cache.queryXPathList("/root/Building");
		System.out.println("Totally "+allBuildings.size()+" buildings loaded from file");
		generateFDFF(allBuildings);
		
//		Collection<String> bs = new LinkedList<>();
		
		// Классификация номеров домов парсера.
/*		System.out.println("Testing parsed buildings' numbers");
		for (Element e: allBuildings) {
			String bNum = e.getChildText("building_number");
			
			bNum = XMLCacheFixer.prepareBnum(bNum);
			Matcher m = null;
			
			try {
				if (String.valueOf(Integer.parseInt(bNum)).equals(bNum))
					continue; // "12"
			} catch (NumberFormatException x) {
			};
			
			Pattern NcorpCr = Pattern.compile("\\d+[а-я]?( кор\\.(\\d+[а-я]?|[а-я]))?");
			m = NcorpCr.matcher(bNum);
			if (m.matches()) {
				continue; //30 кор.а //30 кор.11 //30 кор.32а
			}
			
			Pattern NpN = Pattern.compile("\\d+ п\\.(\\s*\\d+)+"); 
			m = NpN.matcher(bNum);
			if (m.matches()) {
				continue; // 30 п.1 //26 п. 1 2
			}
			
			Pattern NppNN = Pattern.compile("\\d+ п\\.п\\. \\d+ \\d+"); 
			m = NppNN.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);					
				continue; // 48 п.п. 1 2
			}
			
			Pattern NNCr = Pattern.compile("\\d+ \\d+[а-я]"); 
			m = NNCr.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);					
				continue; // 56 56а
			}
			
			Pattern NpN_N = Pattern.compile("\\d+ п\\.\\d+-\\d+"); 
			m = NpN_N.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);					
				continue; // 48 п.1-2
			}
			
			Pattern NMore = Pattern.compile("\\d+ нежилые помещения"); 
			m = NMore.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);				
				continue; // 1 нежилые помещения

			}
			
			Pattern NCrMore = Pattern.compile("\\d+[а-я]? [а-я]*"); 
			m = NCrMore.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);				
				continue; 

			}
			
			Pattern NpMore = Pattern.compile("\\d+ п..*"); 
			m = NpMore.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);				
				continue;

			}
			
			Pattern NSpecial1 = Pattern.compile("\\d+ 1978"); 
			m = NSpecial1.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);				
				continue; //2 строение 1978
			}
			
			System.out.println(""+e.getChildText("areaText")+" | "+
			e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
			+", "+bNum);
		}*/
		
/*		System.out.println("Testing database buildings' numbers");
		Map<Integer, String> houseID_bnum2 = loadFieldHouseNumber("field_data_field_house_number.csv");
		
		for (Entry<Integer, String> entry:houseID_bnum2.entrySet()) {
			String bNum = entry.getValue();

			Matcher m = null; Pattern p=null;
//preparation zone			
			bNum = XMLCacheFixer.prepareDatabaseBnum(bNum);
//preparation finished			
			
			try {
				if (String.valueOf(Integer.parseInt(bNum)).equals(bNum))
					continue; // "12"
			} catch (NumberFormatException x) {
			};
			
			p = Pattern.compile("\\d+/\\d+");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; // 30/1
			}
			
			p = Pattern.compile("\\d+к\\d+");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; // 30к1
			}
			
			p = Pattern.compile("\\d+[а-я]");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; // 30a
			}
			
			p = Pattern.compile("(\\d+[а-я]?)(стр|литер|сооружение)(\\d+[а-я]?)");
			m = p.matcher(bNum);
			if (m.matches() && m.group(1).equals(m.group(3))) {
				continue; // 30стр30
			}
			
			p = Pattern.compile("(\\d+[а-я]?)стр(\\d+[а-я]?)");
			m = p.matcher(bNum);
			if (m.matches() && !m.group(1).equals(m.group(2))) {
				if (m.group(2).equals("1"))
					continue;
				System.err.println(String.valueOf(entry.getKey())+" | "+bNum+" | ");
				continue; // 30стр12
			}
				

			p = Pattern.compile("(\\d+[а-я]?_\\d+)(стр|литер|сооружение)(\\d+[а-я]?_\\d+)");
			m = p.matcher(bNum);
			if (m.matches() && m.group(1).equals(m.group(3))) {
				System.err.println(String.valueOf(entry.getKey())+" | "+bNum+" | ");
				continue; // 1а_2стр1а_2
			}
			
			p = Pattern.compile("\\d+[а-я]?_\\d+");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; 
			}
			
			p = Pattern.compile("\\d+[а-я]?_\\d+");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; 
			}				
			
			
			System.out.println(String.valueOf(entry.getKey())+" | "+bNum+" | ");
			
//			return; 			
		}
		return;*/
		
//		cache.dropEmpty();
//		allBuildings = cache.queryXPathList("/root/Building");
//		System.out.println(""+allBuildings.size()+" contain data");
//		
//		cache.kill_duplicates();
//		allBuildings = cache.queryXPathList("/root/Building");
//		System.out.println(""+allBuildings.size()+" left after removing duplicates");
//		
//		cache.match_buildings_streets(taxonomer);
//		taxonomer = null; //free mem
//		
//		// {addrID -> {houseID}}
//		Map<Integer, Collection<Integer>> houseID_addrID = loadFieldHouseCity("field_data_field_house_city.csv");
//		Map<Integer, String> houseID_bnum = loadFieldHouseNumber("field_data_field_house_number.csv");
//		
//		System.out.println("Read "+houseID_addrID.size()+" termIDs from <field_data_field_house_city.csv>");
//		System.out.println("Read "+houseID_bnum.size()+" lines from <field_data_field_house_number.csv>");
//		
//		cache.match_buildings_number(houseID_addrID, houseID_bnum);
//		cache.saveCache();

		
	}
	
	
	/**
	 * Загрузить CSV файл с данными по адресам домов
	 * @param filename - имя файла для загрузки
	 * @return отношение {ID улицы -> {ID дома1, ID дома2, ..}}
	 * @throws IOException
	 */
	public static Map<Integer, Collection<Integer>> loadFieldHouseCity(String filename) throws IOException {
		Map<Integer, Collection<Integer>> fhc = new HashMap<>();
		try (Scanner sc = new Scanner(Paths.get(filename), StandardCharsets.UTF_8.name())) {
		
			Pattern p = Pattern.compile("\"node\",\"house\",\"0\",\"(\\d*)\",\"(\\d*)\",\"und\",\"0\",\"(\\d*)\"$");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (!line.isEmpty()) {
					Matcher m = p.matcher(line);
					
					if (!m.matches()) {
						System.err.println("Can't match a line of <"+filename+">: "+line);
						continue;
					}
					if (!m.group(1).equals(m.group(2))) {
						System.err.println(m.group(1)+" is no equal to "+m.group(2));
						continue;
					}
					
					int addrID = Integer.parseInt(m.group(3));
					Collection<Integer> houseIDs = fhc.get(addrID);
					if (houseIDs == null) {
						houseIDs = new LinkedList<>();
						fhc.put(addrID, houseIDs);
					}
					houseIDs.add(Integer.parseInt(m.group(1))); 
				}
			}
		}
		return fhc;
	}
	
	/**
	 * Загрузить CSV файл с данными по номерам домов
	 * @param filename - имя файла для загрузки
	 * @return отношение {ID дома -> номер дома}
	 * @throws IOException
	 */
	public static Map<Integer, String> loadFieldHouseNumber(String filename) throws IOException {
		Map<Integer, String> fhn = new HashMap<>();
		try (Scanner sc = new Scanner(Paths.get(filename), StandardCharsets.UTF_8.name())) {
		
			Pattern p = Pattern.compile("\"node\",\"house\",\"0\",\"(\\d*)\",\"(\\d*)\",\"und\",\"0\",\"([^\"]*)\",NULL");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (!line.isEmpty()) {
					Matcher m = p.matcher(line);
					
					if (!m.matches()) {
						System.err.println("Can't match a line of <"+filename+">: "+line);
						continue;
					}
					if (!m.group(1).equals(m.group(2))) {
						System.err.println(m.group(1)+" is not equal to "+m.group(2));
						continue;
					}
					
					fhn.put(Integer.parseInt(m.group(1)), m.group(3));
					
				}
			}
		}
		
		return fhn;
	}
	
	static public final
	HashMap<String, String> walls2HouseType = new HashMap<>();
	
	static {
		walls2HouseType.put("каменные, кирпичные", "29479");
		walls2HouseType.put("деревянные", "29482");
		walls2HouseType.put("смешанные", "29481");
		walls2HouseType.put("панельные", "29480");
		walls2HouseType.put("блочные", "29547");
		walls2HouseType.put("монолитные", "29478");
	}
	
	/**
	 * Получить строку ID термина таксономии для материала стен по названию материала стен.
	 * @param source - название материала стен, как указано в кэше
	 * @return ID термина таксономии "Тип дома"
	 */
	public static 
	String mapWallsType(String source) {
		String hT = source.toLowerCase();
		if (hT==null || hT.isEmpty() || hT.equals("нет данных"))
			return null;
		
		return walls2HouseType.get(hT);
	}
	
	//"node","house","0","114263","114263","und","0","2"
	static
	void generateFDFF(Collection<Element> allBuildings) throws IOException {
		String filename = "field_data_field_floors.csv";
		FileOutputStream fos = new FileOutputStream(filename); 
		OutputStreamWriter out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		
		for(Element el:allBuildings) {
			String floors = el.getChildText("floors");
			String nodeID = el.getChildText("houseId");
			// паранойя
			if (floors == null || floors.isEmpty() ||
				nodeID == null || nodeID.isEmpty())
				continue;
			
			int iFloors; int iNodeID;
			try { iFloors = Integer.parseInt(floors);
			} catch (NumberFormatException e) {	continue; /* не число */}
			
			try { iNodeID = Integer.parseInt(nodeID);
			} catch (NumberFormatException e) {	continue; /* не число */}
			
			if (iFloors<1 || iNodeID<1)
				continue;
			if (iFloors > 20) {
				System.err.println("hmmm  possibly error with `floors`: "+el.getAttributeValue("url"));
				continue;
			}
			// конец паранойи
			
			out.write("\"node\",\"house\",\"0\",\""+String.valueOf(iNodeID)+"\",\""+String.valueOf(iNodeID)+"\",\"und\",\"0\",\""+String.valueOf(iFloors)+"\"\r\n");
		}
		
		out.close();
	}
	
	//"node","house","0","114263","114263","und","0","1978"
	static
	void generateFDFHE(Collection<Element> allBuildings) throws IOException {
		String filename = "field_data_field_house_expl.csv";
		FileOutputStream fos = new FileOutputStream(filename); 
		OutputStreamWriter out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		
		for(Element el:allBuildings) {
			String explYear = el.getChildText("expl_year");
			String nodeID = el.getChildText("houseId");
			// паранойя
			if (explYear == null || explYear.isEmpty() ||
				nodeID == null || nodeID.isEmpty())
				continue;
			
			int iExplYear; int iNodeID;
			try { iExplYear = Integer.parseInt(explYear);
			} catch (NumberFormatException e) {	continue; /* не число */}
			
			try { iNodeID = Integer.parseInt(nodeID);
			} catch (NumberFormatException e) {	continue; /* не число */}
			
			if (iExplYear<1 || iNodeID<1)
				continue;
			if (iExplYear>2015 || iExplYear<1900) {
				System.err.println("hmmm  possibly error with `expl_year`: "+el.getAttributeValue("url"));
				continue;
			}
			// конец паранойи
			
			out.write("\"node\",\"house\",\"0\",\""+String.valueOf(iNodeID)+"\",\""+String.valueOf(iNodeID)+"\",\"und\",\"0\",\""+String.valueOf(iExplYear)+"\"\r\n");
		}
		
		out.close();
	}	
}
