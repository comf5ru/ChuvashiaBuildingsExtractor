package buildingsimporter;

import java.util.Collection;
import java.util.List;

import org.jdom2.Element;

import buildingsextractor.XMLCache;

/**
 * Позволяет вносить изменения посредством XPath манипуляций в кэш,
 * подготавливая данные для совместимости с импортом 
 */
public class XMLCacheFixer extends XMLCache {

	public XMLCacheFixer(String xmlFileName) {
		super(xmlFileName);
	}

	private static final String ELEMENT_BY_URL_XPATH = "/root/*[@url='%s']";
	
	private static final String ALL_BUILDINGS = "/root/Building";
	private static final String ALL_BUILDINGS_FOR_ADDRESS = "/root/Building[1=1 %s]";	
	public void kill_duplicates() {
		Element root = doc.getRootElement();
		
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("About to go cray in this mess!");

		for (Element buildingElement: allBuildings) {
			if (counter--%100 ==0 )
				System.out.println(counter);
			
			if (buildingElement.getParent() == null) 
				continue; // already detached earlier
			
			//location='%s' and street='%s' and building_number='%s'
			String street = buildingElement.getChildText("street");
			String location = buildingElement.getChildText("location");
			String bnum = buildingElement.getChildText("building_number");
			
			String conditions = "";
			if ((location == null) || (street == null) || (bnum == null))
				System.out.println("Something is missing! "+buildingElement.getAttributeValue("url")
						+" : "+location+"|"+street+"|"+bnum);
				
			if (location == null) location = "";
			if (street == null) street = "";
			if (bnum == null) bnum = "";
			conditions += "and location='"+location+"'";
			conditions += "and street='"+street+"'";
			conditions += "and building_number='"+bnum+"'";
			
			Collection<Element> sameStreet = queryXPathList(String.format(ALL_BUILDINGS_FOR_ADDRESS, conditions));
			
			if (sameStreet.size() > 1) {
				System.out.println(location + " " + street + " " + bnum + ": " + sameStreet.size());
				for (Element buildingSS: sameStreet) {
					buildingSS.detach();
				}
			} else if (sameStreet.size() == 0) {
				System.out.println("WHoopsy-daisy! where did it go? " + location + "-" + street + "-" + bnum);
			}
			
		}
//		for (Element current: oldCachedElements) 
//			current.detach();
//
//		root.addContent((Element)e.clone());
//		
//		String searchXPath = String.format(ELEMENT_BY_URL_XPATH, pageURL);
//		List<Element> result = queryXPathList(searchXPath);
		//result.size()>0?(Element)result.get(0).clone():null; 
	}
}
