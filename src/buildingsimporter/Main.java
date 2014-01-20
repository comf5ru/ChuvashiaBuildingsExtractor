package buildingsimporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
 * ������� ����� ��� ������� �������������� XML -> mysql
 * (��������� ����� ��� ������� � Drupal �� ����� � �������) 
 *
 */
public class Main {

	public static XMLCacheFixer cache;
	public static void main(String[] args) throws IOException {
//		KPGTaxonomer taxonomer = new KPGTaxonomer("kpg_locations_data_ids.txt");
		cache = new XMLCacheFixer("4import_Buildings_Chuvashia.xml");
		
		//��������� ������� ����� � ����.
		Collection<Element> allBuildings = cache.queryXPathList("/root/Building");

//		Collection<String> bs = new LinkedList<>();
		
		// ������������� ������� ����� �������.
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
			
			Pattern NcorpCr = Pattern.compile("\\d+[�-�]?( ���\\.(\\d+[�-�]?|[�-�]))?");
			m = NcorpCr.matcher(bNum);
			if (m.matches()) {
				continue; //30 ���.� //30 ���.11 //30 ���.32�
			}
			
			Pattern NpN = Pattern.compile("\\d+ �\\.(\\s*\\d+)+"); 
			m = NpN.matcher(bNum);
			if (m.matches()) {
				continue; // 30 �.1 //26 �. 1 2
			}
			
			Pattern NppNN = Pattern.compile("\\d+ �\\.�\\. \\d+ \\d+"); 
			m = NppNN.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);					
				continue; // 48 �.�. 1 2
			}
			
			Pattern NNCr = Pattern.compile("\\d+ \\d+[�-�]"); 
			m = NNCr.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);					
				continue; // 56 56�
			}
			
			Pattern NpN_N = Pattern.compile("\\d+ �\\.\\d+-\\d+"); 
			m = NpN_N.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);					
				continue; // 48 �.1-2
			}
			
			Pattern NMore = Pattern.compile("\\d+ ������� ���������"); 
			m = NMore.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);				
				continue; // 1 ������� ���������

			}
			
			Pattern NCrMore = Pattern.compile("\\d+[�-�]? [�-�]*"); 
			m = NCrMore.matcher(bNum);
			if (m.matches()) {
				System.err.println(""+e.getChildText("areaText")+" | "+
						e.getChildText("locationName")+" ("+e.getChildText("location")+") | "+e.getChildText("street")
						+", "+bNum);				
				continue; 

			}
			
			Pattern NpMore = Pattern.compile("\\d+ �..*"); 
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
				continue; //2 �������� 1978
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
			
			p = Pattern.compile("\\d+�\\d+");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; // 30�1
			}
			
			p = Pattern.compile("\\d+[�-�]");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; // 30a
			}
			
			p = Pattern.compile("(\\d+[�-�]?)(���|�����|����������)(\\d+[�-�]?)");
			m = p.matcher(bNum);
			if (m.matches() && m.group(1).equals(m.group(3))) {
				continue; // 30���30
			}
			
			p = Pattern.compile("(\\d+[�-�]?)���(\\d+[�-�]?)");
			m = p.matcher(bNum);
			if (m.matches() && !m.group(1).equals(m.group(2))) {
				if (m.group(2).equals("1"))
					continue;
				System.err.println(String.valueOf(entry.getKey())+" | "+bNum+" | ");
				continue; // 30���12
			}
				

			p = Pattern.compile("(\\d+[�-�]?_\\d+)(���|�����|����������)(\\d+[�-�]?_\\d+)");
			m = p.matcher(bNum);
			if (m.matches() && m.group(1).equals(m.group(3))) {
				System.err.println(String.valueOf(entry.getKey())+" | "+bNum+" | ");
				continue; // 1�_2���1�_2
			}
			
			p = Pattern.compile("\\d+[�-�]?_\\d+");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; 
			}
			
			p = Pattern.compile("\\d+[�-�]?_\\d+");
			m = p.matcher(bNum);
			if (m.matches()) {
				continue; 
			}				
			
			
			System.out.println(String.valueOf(entry.getKey())+" | "+bNum+" | ");
			
//			return; 			
		}
		return;*/
		
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
						System.err.println(m.group(1)+" is not equal to "+m.group(2));
						continue;
					}
					
					fhn.put(Integer.parseInt(m.group(1)), m.group(3));
					
				}
			}
		}
		
		return fhn;
	}

}
