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
 * ��������� ������� ��������� ����������� XPath ����������� � ���,
 * ������������� ������ ��� ������������� � �������� 
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
				continue; // already detached earlier - �.�. ����� ������ � ���-�� ���������� � ��� ���������.
			
			//location='%s' and street='%s' and building_number='%s'
			String street = buildingElement.getChildText("street");
			String location = buildingElement.getChildText("location");
			String bnum = buildingElement.getChildText("building_number");
			
			// ��� ���� ��� ���.������
			if ((location == null) || (bnum == null))
				System.out.println("Something is missing! "+buildingElement.getAttributeValue("url")
						+" : "+location+"|"+street+"|"+bnum);

			// ��������� ������� � ������ ��������� ��������� ������
			String conditions = "";
			if (location != null) 
				conditions += "and location='"+location+"'";
			if (street != null) 
				conditions += "and street='"+street+"'";
			if (bnum != null) 
				conditions += "and building_number='"+bnum+"'";
			
			// ��� ���� � ���������� �������
			Collection<Element> sameAddress = queryXPathList(String.format(ALL_BUILDINGS_FOR_ADDRESS, conditions));
			
			if (sameAddress.size() > 1) {
				System.out.println("Same address buildings <" +location+ " " + street + " " + bnum + ">: " + sameAddress.size());
				// �������� � ����� ��� ������ � ������� � ����� � ����������� �������.
				Element combined = new Element("Building");
				for (Element buildingSA: sameAddress) {
					buildingSA.detach();
					List<Element> properties = buildingSA.getChildren();
					for (Element p: properties) {
						String name = p.getName();
						String value = p.getText();
						if (value.equals("��� ������") || value.isEmpty())
							continue; //�� ��������� ������� �������
						Element updatedProperty = combined.getChild(name);
						if (updatedProperty == null) {
							updatedProperty = new Element(name);
							combined.addContent(updatedProperty);
						}
						updatedProperty.setText(value); //��������
					}
				}
				newRoot.addContent(combined);
			} else if (sameAddress.size() == 0) {
				System.out.println("WHoopsy-daisy! where did it go? " + location + "-" + street + "-" + bnum);
			} else {
				// ������ ���� ��� ��� ������� ������
				buildingElement.detach();
				newRoot.addContent(buildingElement);
			}
			
		} // for allBuildings
		
		assert(root.getChildren().size() == 0);
		doc.setRootElement(newRoot);
	}

	/**
	 * �������� ������ ���� ������� �������
	 * @return Collection<String> - �������� �������.
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
	 * �������� id ������� �� ������� "������ � �����" ��� ������ ������.
	 * @param building - ������
	 * @param taxonomer - ������� ��������
	 * @return id ������� � ������� ��� 0 ��� ������.
	 */
	public int match_building_addr_term(Element building, KPGTaxonomer taxonomer) {
		String areaName = building.getChildText("areaName"); //�������� ������, ���������� �� ������ �� �������� ������
		String locationName = building.getChildText("locationName"); //�������� ���.������, ���������� �� ������ �� �������� ������
		String location = building.getChildText("location"); //�������� ���.������, ���������� ��������� ������. !! ����� �� ��������� � locationName
		String street = building.getChildText("street"); //�������� �����.
		
		if (street != null && !street.isEmpty()) {
			// ��������� ����� ���� � ���. �������, ����� ����� ������ ����� "(����������� �/�) �� ���������"
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
		// ���������� ������� � �������� ������������ �� �������� ���. ������ 
		LinkedList<KPGTerm> closeLocationMatches = new LinkedList<>();
		int streetId = 0;
		
		// ������� ������� - �����
		HashMap<Integer,KPGTaxonomer.KPGTerm> subset = taxonomer.chooseForParent(0);
		if (areaName != null && !areaName.isEmpty()) {
			//"���������� ������������� �����" --> "�-�.����������"
			String newAreaName = "�-�."+areaName.replace(" ������������� �����", "");
			areaId = taxonomer.findByName(subset, newAreaName);
			if (areaId == 0) {
				System.err.println("Area term not found: "+areaName);
				return 0;
			}
		}
		
		// ������� �������, ����� �����
		if (areaName == null) {
			String newLocationName = locationName.replace("����� ", "�.");
			locationId = taxonomer.findByName(subset, newLocationName);
			if (locationId == 0) {
				System.err.println("Non-area town term not found: "+locationName);
				return 0;
			}
		}
		
		// ������ ��������� ����� � ������ (���� areaId != 0)
		if (locationId == 0) {
			if (areaId == 0) {
				System.err.println("Something is very wrong. No area code and no location code");
				return 0;
			}
			
			//��� ��������� ������ ������� ������
			subset = taxonomer.chooseForParent(areaId);
			
			// ���������� ������ ��� ���������
			String fixedLocationName = prepareName(locationName);
			String fixedLocation = prepareName(location);

			// ����������� id -> weight
			class TermIdWeightPair {
				int id;	int weight;
				TermIdWeightPair(int i, int w) {id = i; weight = w;};
			}
			List<TermIdWeightPair> matchingWeightsList = new LinkedList<>(); 
			
			// ������� �������� � ����� ����������� �����������
			int maxWeight = 0;
			for (KPGTerm term: subset.values()) {
				String fixedTermName = prepareName(term.name);
				// ������������ ��������� 
				int linkNameWeight = likeness(fixedLocationName, fixedTermName);
				int parsedNameWeight = likeness(fixedLocation, fixedTermName);
				int parsedCombined = likeness(fixedLocation+fixedLocationName, fixedTermName)+5; // ������ ���� ���������� ���� "�.�������� (��������������� �/�)" - �.�. loc+locName
				int weight = Math.max(Math.max(linkNameWeight, parsedNameWeight), parsedCombined);
				
				if (weight > maxWeight && weight>50) 
					maxWeight = weight;
				
				matchingWeightsList.add(new TermIdWeightPair(term.id, weight));
			}
			
			// ���������� ������ ������ ���������, ���������������� �� �������� ��������
			
			// ���������� �� ��������.
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
							return o2.weight-o1.weight; // �� ��������
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
				// ����� ��������� �������, �� �� ������ ����������
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
		} // ��������� locationId � closeLocationMatches �� ���������� areaId 
		else {
			// �������� locationId � ������ closeLocationMatches
			closeLocationMatches.add(taxonomer.terms.get(locationId));
		} 
		
		if (street==null || street.isEmpty()) 
			return locationId;

		if (street.equals("� ����������� �� ���������")) {
			street = street+"";
		}
		//1. �������� ������ ��������, ����� ����� ������� ��� "� ����������� �� ���������" (� ����������)
		int checkRes = checkSpecialStreet(street, taxonomer);
		if (checkRes != -1) 
			return checkRes; // ������� ���������� �������������, ���� ��� ������������� ������ ��������.

		//2. ����������� ����� ���������� ����� ��� ���� � �������� ����� �� locationId, ���� �� ���������, �� � ��������� ����������
		
		// ��������� ������ ���. ����� �� ��������� �����.
		for (KPGTerm loc: closeLocationMatches) {
			subset = taxonomer.chooseForParent(loc.id); // ������ ����
			
			// ��������� ����� � ���. ������.
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
	 * ������� ��������� ������ � ������� ��������
	 * @param needle - ������ ��� ������
	 * @param haystack - ����� ��������
	 * @return 0 ���� ������������������� ������� �� �������, ����� id �������� ����������� �������.
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
	 * ��������� ������ ��������, ����� ��� ������ ��������� ����� ������ � ���� "� ����������� �� ���������"
	 * @param street - ������ �����
	 * @param taxonomer - ��������� ��������
	 * @return -1, ���� ������ �� �������� ������ ���������
	 *   		0, ���� ������ �������� ������ ���������, �� ���������� �� �������
	 *   	   id ������� �����, ���� ������� ����������� ������ ��������.
	 */
	private int checkSpecialStreet(String street, KPGTaxonomer taxonomer) {
        Pattern specialLocStreet = Pattern.compile("� ([^ ]+) (.*)$");
        Matcher m = specialLocStreet.matcher(street);
        if (m.matches()) {
    		String subLocName = "� " + m.group(1);
    		String subLocStreet = m.group(2);
    		
    		HashMap<Integer,KPGTaxonomer.KPGTerm> subset = taxonomer.chooseForParent(17004); //������
    		int subLocId = simlpeTermMatch(subLocName, subset);
    		if (subLocId == 0)
    			return 0;
    		
    		subset = taxonomer.chooseForParent(subLocId); // ������ �����
    		int subLocStreetId = simlpeTermMatch(subLocStreet, subset);
			return subLocStreetId;
        }
        
		return -1;
	}
	/**
	 * �������������� ������ ��� ��������� � ������ �������, ������ �������� �������.
	 * @param name
	 * @return
	 */
	public String prepareName(String name) {
		return name.replace(" ", "").replace(".","").replace(",","").replace("/", "").replace("(", "").replace(")","");
	}
	
	/**
	 * �������� ������� ���������� � ID ��� ����� ��� ������� ���� ������.
	 * �������� ������ � ����, �������� ���� "streetTermId" � ������� ������� �� ����������.
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
					stID = new Element("streetTermId"); // ����� ���������� ������� ��� ���������� ID ������� ������.
					buildingElement.addContent(stID);
				}
				stID.setText(String.valueOf(termId));
			}
		}
	}
	
	/**
	 * ������� "������" ��������� ���� �����. ������� ����� �����, ����� �������, ��� ������ "���������" ������2 �� ������1. 
	 * @param str1 - ������ ������
	 * @param str2 - ������ ������
	 * @return ������ ��������� � ��������� 0..100, ��� ������ - ��� ����� ������ ������.
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

			int count_equals = 0; //����� ��������� ��������
			for (int i = 0; i< end1-start1; i++)
				if (str1.charAt(start1+i) == str2.charAt(start2+i))
					count_equals++;
			
			max_likeness = Math.max(max_likeness, count_equals); 
		}
		
		int likeness_coeff = max_likeness*100/(Math.max(l1, l2));
		return likeness_coeff;
	}
	
	/**
	 * ������� ����, ��� ��� ������.
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
			
			if (//(expl_year==null || expl_year.isEmpty() || expl_year.equals("��� ������")) &&
				(flats==null || flats.isEmpty() || flats.equals("��� ������")) &&
				(porches==null || porches.isEmpty() || porches.equals("��� ������")) &&
				(floors==null || floors.isEmpty() || floors.equals("��� ������")) &&
				(walls==null || walls.isEmpty() || walls.equals("��� ������")) &&
				(lifts==null || lifts.isEmpty() || lifts.equals("��� ������"))
			) {
				buildingElement.detach();
				emptyDataBuildingsCounter++;
			}
		}
		System.out.println("Removed "+emptyDataBuildingsCounter+" buildings");
	}

	/**
	 * ���������� ������ ������ ����, ����������� �� ����, ����� ����� ���� ���������� ����� ���� ���� � ������� ���� �� ������� 
	 * @param source - ������ ���������� �� ����
	 * @return ������ ��� ���������.
	 */
	public static
	String prepareDatabaseBnum(String source) {
		String s = source.toLowerCase().replace("����", "").replace("���", "");
		Pattern p; Matcher m;

		p = Pattern.compile("(.*)(\\.|���_|����������_)");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1);
		
		p = Pattern.compile("(����������|���)([^�-�].*)");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(2);
		
		p = Pattern.compile("(\\d+)(���|�����|����������)([�-�])");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1)+m.group(3); 
		
		p = Pattern.compile("(\\d+)_([�-�])");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1)+m.group(2); 

		p = Pattern.compile("(.*\\d)�([�-�])$");
		m = p.matcher(s);
		if (m.matches()) 
			s = m.group(1)+m.group(2);
		return s;
	}	
	
	/**
	 * ���������� ������ ������ ���� ����������� �� �������, ����� ����� ���� ���������� ����� ���� ���� � ������� ���� �� ������� 
	 * @param source - ������ ���������� �� �������
	 * @return ������ ��� ���������.
	 */
	public static
	List<String> prepareParsedBnum(String source) {
		String s = source.toLowerCase().replace("a", "�").replace(" ��������", "").replace(" �����", "").replace("�������", "�.")
				.replace(" �����", "").replace(" ���", "");
		
		Pattern p; Matcher m;
		List<String> rvalue = new LinkedList<>(); // ������ ������������ ��������� (���� 1)

		
		p = Pattern.compile("([^\"]*)\"([^\"]*)\"(.*)?"); // ������ � ��������.
		m = p.matcher(s);
		if (m.matches()) { 
			//  4"a"  --> 4a
			if (m.group(3) != null)
				s = m.group(1)+m.group(2)+m.group(3);
			else
				s = m.group(1)+m.group(2);
		}
		
		p = Pattern.compile("(\\d+)\\s+([�-�])"); // ������ �������� ��������
		m = p.matcher(s);
		if (m.matches()) { 
			//  4 a  --> 4a
			s = m.group(1)+m.group(2);
		}
		
		p = Pattern.compile("(\\d+ ���.[�-�])\\d+"); // ����������(������) ����� ����� �������
		m = p.matcher(s);
		if (m.matches()) { 
			//  30 ���.�30 -> 30 ���.� 
			s = m.group(1);
		}
		
		p = Pattern.compile("(\\d+) ���.([�-�])");
		m = p.matcher(s);
		if (m.matches()) { 
			//  30 ���.� -> 30� 
			s = m.group(1)+m.group(2);
		}
		
		p = Pattern.compile("(\\d+) ���.(\\d+)"); 
		m = p.matcher(s);
		if (m.matches()) { 
			//  30 ���.1 -> 30�1 
			rvalue.add(m.group(1)+"�"+m.group(2));
			rvalue.add(m.group(1)+"/"+m.group(2));
			return rvalue;
		}			

		rvalue.add(s);
		
		p = Pattern.compile("([^/]*)/([^ ]*)( .*)?"); // ��������� �� ���������������� �����.
		m = p.matcher(s);
		if (m.matches()) { 
			// "a/b c" --> "a�b c", "a c"
			if (m.group(3) != null) {
				rvalue.add(m.group(1)+"�"+m.group(2)+m.group(3));
				rvalue.add(m.group(1)+m.group(3));
			} else {
				rvalue.add(m.group(1)+"�"+m.group(2));
				rvalue.add(m.group(1));
			}
		}
		
		return rvalue;
	}
	
	//TODO
	/**
	 * ��������� ������ ���� � ����� �����, ��������� ��� ������� ������ �� ���� ����� ��������������� ����.
	 * ������ ��� ������� �������� <Building> � ���� ������ ���� ��� ���������
	 * �������� ��������� "houseId" � ��������� ���� ����� ����, ���� ������.
	 * @param addrID_houseIDs - ����������� {ID ����� -> {ID ����1, ID ����2, ..}}
	 * @param houseID_bnum - ����������� {ID ���� -> ����� ����}
	 */
	public void match_buildings_number(Map<Integer, Collection<Integer>> addrID_houseIDs, Map<Integer, String> houseID_bnum) {
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("Matching buildings' numbers.");
		
		// �������������� ��������� bNum �� ����
		Map<Integer, String> houseID_bnum1 = new HashMap<>();
		Map<Integer, String> houseID_bnum2 = new HashMap<>();
		
		// �������� ����� �������������� ��������� ������, ��� ��������: "30/12" -> "30�12" � "30"
		for (Entry<Integer, String> entry: houseID_bnum.entrySet()) {
			Pattern extraBNumPattern = Pattern.compile("(\\d+)/(\\d+)");
			Matcher m = extraBNumPattern.matcher(entry.getValue());
			
			if (m.matches()) {
				houseID_bnum1.put(entry.getKey(), m.group(1)+"�"+m.group(2));
				houseID_bnum2.put(entry.getKey(), m.group(1));
			}
		}
		
		int successCounter = 0;
		//1 ��� ������� ����...
		for (Element buildingElement: allBuildings) {
			if (--counter%1000 ==0 )
				System.out.println(counter);
			
			int nodeId = 0;
			
			// ������� ID ������ ����.
			int addrID = 0;
			try {
				addrID = Integer.parseInt(buildingElement.getChildText("streetTermId"));
			} catch (NumberFormatException e) {
				// ��  ������� ��������� id ������� ������ - �������� �� �� ��� �����������.
				continue;
			}
			
			// ��������� ��� ���� �� ��� ����� � ����,
			Collection<Integer> houseIDs = addrID_houseIDs.get(addrID);
			if (houseIDs == null || houseIDs.size()==0)
				continue; //� ����� ������� ��� �����, ��� �������!
			
			// � ��� ������� ���� ��������� ����������� ��� �����.
			List<String> sourceNums = prepareParsedBnum(buildingElement.getChildText("building_number"));
			LinkedList<Integer> matchedIDs = new LinkedList<>();
			
			if (sourceNums.size() == 0) {
				System.err.println("can't generated building num variants for "+buildingElement.getChildText("areaText")+" | "+
						buildingElement.getChildText("locationName")+" ("+buildingElement.getChildText("location")+
						") | "+buildingElement.getChildText("street")
						+", "+buildingElement.getChildText("building_number"));
				continue; // ��������� ������.
			}
			
			// ���� ��� ��������� ��������� ������ ����� ���� �� �������. (N1/N2 --> N1/N2, N1�N2, N1)
			for (String sourceNum : sourceNums) {
				// � ������ ��������� ������� � �����
				for (int houseID: houseIDs) {
					String targetNum = prepareDatabaseBnum(houseID_bnum.get(houseID));
					
					if (targetNum.equals(sourceNum)) {
						matchedIDs.add(houseID);
					} 
				}
				
				if (matchedIDs.size() >0 ) 
					break; // ����������, ����� ������� ���� �� ���� ���������.
			}
			
			String sourceNumFirst = sourceNums.get(0);
			
			if (matchedIDs.size() == 0 ) {
				//��-��, �� �����. ��������� ����� ������������� ��������� 1 �� ����. 
				// ������ ���� ������ �������� ��������� ���� �� �������.
				
				for (int houseID: houseIDs) {
					if (houseID_bnum1.containsKey(houseID)) {
						String targetNum = prepareDatabaseBnum(houseID_bnum1.get(houseID));
						
						if (targetNum.equals(sourceNumFirst)) 
							matchedIDs.add(houseID);
					}
				}
			}
			
			if (matchedIDs.size() == 0 ) {
				//��-��, ����� �� �����. ��������� ����� ������������� ��������� 2 �� ����. 
				// ������ ���� ������ �������� ��������� ���� �� �������.
				
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
				
				// ����� ���������� ������� ��� ���������� ID ������� ������.
				Element stID = new Element("houseId"); 
				stID.setText(String.valueOf(nodeId));
				buildingElement.addContent(stID);
				
				successCounter++;
			} else if (matchedIDs.size()==2 && houseID_bnum.get(matchedIDs.getFirst()).equals(
					houseID_bnum.get(matchedIDs.getLast()))) {
				// ��������� � ���� - ��������� ��� ID
				
				// removing old data
				List<Element> houseIDChildren = buildingElement.getChildren("houseId"); 
				for (Element ch: houseIDChildren) 
					ch.detach();
				
				// ����� ���������� ������� ��� ���������� ID ������� ������.
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
