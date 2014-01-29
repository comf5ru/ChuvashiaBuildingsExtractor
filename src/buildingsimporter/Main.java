package buildingsimporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

	/**
	 * ��������� ��������/�������� ������ ��� ��������
	 */
	static interface IDataValidator {
		
		/**
		 * ��������� ����������� ������
		 * @param data - ������
		 * @return null ���� ������ �� ����� ���� ��������������, ������ ��� �������� �����
		 */
		String validate(Integer data, String testedURL);
	}
	public static XMLCacheFixer cache;
	public static void main(String[] args) throws IOException {
		KPGTaxonomer taxonomer = new KPGTaxonomer("kpg_locations_data_ids.txt");
		cache = new XMLCacheFixer("4import_Buildings_Chuvashia.xml");
		
		//��������� ������� ����� � ����.
		Collection<Element> allBuildings = cache.queryXPathList("/root/Building");
		System.out.println("Totally "+allBuildings.size()+" buildings loaded from file");
		
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

		generateCSV(allBuildings, "field_data_field_floors.csv", "floors", new IDataValidator() {
			@Override
			public String validate(Integer data, String testedURL) {
				if (data<1)
					return null;
				if (data > 20) {
					System.err.println("hmmm  possibly error with `floors`: "+testedURL);
					return null;
				}
				
				return String.valueOf(data);
			}
		});
		
		generateCSV(allBuildings, "field_data_field_house_expl.csv", "expl_year", new IDataValidator() {
			@Override
			public String validate(Integer data, String testedURL) {
				if (data<1)
					return null;
				if (data>2015 || data<1850) {
					System.err.println("hmmm  possibly error with `expl_year`: "+testedURL);
					return null;
				}				
				return String.valueOf(data);
			}
		});
		
		generateCSV(allBuildings, "field_data_field_house_flats.csv", "flats", new IDataValidator() {
			@Override
			public String validate(Integer data, String testedURL) {
				if (data<1)
					return null;
				return String.valueOf(data);
			}
		});
		
		generateCSV(allBuildings, "field_data_field_house_lift.csv", "lifts", new IDataValidator() {
			@Override
			public String validate(Integer data, String testedURL) {
				if (data > 1) data = 1;
				if (data < 0) data = 0;
				
				return String.valueOf(data);
			}
		});
		
		generateCSV(allBuildings, "field_data_field_house_porches.csv", "porches", new IDataValidator() {
			@Override
			public String validate(Integer data, String testedURL) {
				if (data < 1)
					return null;
				else return String.valueOf(data);
			}
		});
		
		generateCSV(allBuildings, "field_data_field_house_porches.csv", "porches", new IDataValidator() {
			@Override
			public String validate(Integer data, String testedURL) {
				if (data < 1)
					return null;
				else return String.valueOf(data);
			}
		});
		
		generateCSVHouseType(allBuildings);
	}
	
	
	/**
	 * ��������� CSV ���� � ������� �� ������� �����
	 * @param filename - ��� ����� ��� ��������
	 * @return ��������� {ID ����� -> {ID ����1, ID ����2, ..}}
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
	 * ��������� CSV ���� � ������� �� ������� �����
	 * @param filename - ��� ����� ��� ��������
	 * @return ��������� {ID ���� -> ����� ����}
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
		walls2HouseType.put("��������, ���������", "29479");
		walls2HouseType.put("����������", "29482");
		walls2HouseType.put("���������", "29481");
		walls2HouseType.put("���������", "29480");
		walls2HouseType.put("�������", "29547");
		walls2HouseType.put("����������", "29478");
	}
	
	/**
	 * �������� ������ ID ������� ���������� ��� ��������� ���� �� �������� ��������� ����.
	 * @param source - �������� ��������� ����, ��� ������� � ����
	 * @return ID ������� ���������� "��� ����"
	 */
	public static 
	String mapWallsType(String source) {
		String hT = source.toLowerCase();
		if (hT==null || hT.isEmpty() || hT.equals("��� ������"))
			return null;
		
		return walls2HouseType.get(hT);
	}
	
	//"node","house","0","114263","114263","und","0","2"
	static
	void generateCSV(Collection<Element> allBuildings, String filename, String dataKey, IDataValidator dv) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename); 
		OutputStreamWriter out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		
		for(Element el:allBuildings) {
			String data = el.getChildText(dataKey);
			List<Element> nodeIDelements = el.getChildren("houseId"); // ����������� ����� ���� ���������

			// ��������
			if (data == null || data.isEmpty())
				continue;
			
			int iData; 
			try { iData = Integer.parseInt(data);
			} catch (NumberFormatException e) {	continue; /* �� ����� */}
			
			String expData = dv.validate(iData, el.getAttributeValue("url"));
			if (expData == null)
				continue;
			// ����� ��������
			int iNodeID;
			for (Element nodeIDelement: nodeIDelements) {
				String nodeID = nodeIDelement.getText();
				if (nodeID == null || nodeID.isEmpty())
					continue;
				
				try { iNodeID = Integer.parseInt(nodeID);
				} catch (NumberFormatException e) {	continue; /* �� ����� */}
				
				out.write("\"node\",\"house\",\"0\",\""+String.valueOf(iNodeID)+"\",\""+String.valueOf(iNodeID)+"\",\"und\",\"0\",\""+expData+"\"\r\n");
			}
		}
		
		out.close();
	}
	
	//"node","house","0","114263","114263","und","0","2"
	static
	void generateCSVHouseType(Collection<Element> allBuildings) throws IOException {
		String filename="field_data_field_house_type.csv";
		
		FileOutputStream fos = new FileOutputStream(filename); 
		OutputStreamWriter out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		
		for(Element el:allBuildings) {
			String data = el.getChildText("walls");
			List<Element> nodeIDelements = el.getChildren("houseId"); // ����������� ����� ���� ���������

			// ��������
			if (data == null || data.isEmpty())
				continue;
			
//			int iData; 
//			try { iData = Integer.parseInt(data);
//			} catch (NumberFormatException e) {	continue; /* �� ����� */}
			
			String expData = mapWallsType(data);
			if (expData == null)
				continue;
			// ����� ��������
			int iNodeID;
			for (Element nodeIDelement: nodeIDelements) {
				String nodeID = nodeIDelement.getText();
				if (nodeID == null || nodeID.isEmpty())
					continue;
				
				try { iNodeID = Integer.parseInt(nodeID);
				} catch (NumberFormatException e) {	continue; /* �� ����� */}
				
				out.write("\"node\",\"house\",\"0\",\""+String.valueOf(iNodeID)+"\",\""+String.valueOf(iNodeID)+"\",\"und\",\"0\",\""+expData+"\"\r\n");
			}
		}
		
		out.close();
	}	
	
}
