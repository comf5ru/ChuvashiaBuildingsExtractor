package buildingsimporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Главный класс для запуска преобразования XML -> mysql
 * (получение файла для импорта в Drupal из файла с данными) 
 *
 */
public class Main {

	public static XMLCacheFixer cache;
	public static void main(String[] args) throws IOException {
//		KPGTaxonomer taxonomer = new KPGTaxonomer("kpg_locations_data_ids.txt");
		cache = new XMLCacheFixer("4import_Buildings_Chuvashia.xml");
		
//		cache.dropEmpty();
//		cache.kill_duplicates();
		
//		cache.match_buildings_streets(taxonomer);
//		taxonomer = null; //free mem
		
		// {addrID -> {houseID}}
		Map<Integer, Collection<Integer>> houseID_addrID = loadFieldHouseCity("field_data_field_house_city.csv");
		Map<Integer, String> houseID_bnum = loadFieldHouseNumber("field_data_field_house_number.csv");
		
		System.out.println("Read "+houseID_addrID.size()+" termIDs from <field_data_field_house_city.csv>");
		System.out.println("Read "+houseID_bnum.size()+" lines from <field_data_field_house_number.csv>");
		
		cache.match_buildings_number(houseID_addrID, houseID_bnum);
//		cache.saveCache();

	}
	
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
						System.err.println(m.group(1)+" is no equal to "+m.group(2));
						continue;
					}
					
					fhn.put(Integer.parseInt(m.group(1)), m.group(3)); 
				}
			}
		}
		return fhn;
	}

}
