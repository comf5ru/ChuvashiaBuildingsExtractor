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
		Element newRoot = new Element("root");
		
		Collection<Element> allBuildings = queryXPathList(ALL_BUILDINGS);
		int counter = allBuildings.size();
		System.out.println("About to go cray in this mess!");

		for (Element buildingElement: allBuildings) {
			if (--counter%100 ==0 )
				System.out.println(counter);
			
			if (buildingElement.getParent() == null) 
				continue; // already detached earlier
			
			//location='%s' and street='%s' and building_number='%s'
			String street = buildingElement.getChildText("street");
			String location = buildingElement.getChildText("location");
			String bnum = buildingElement.getChildText("building_number");
			
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
				System.out.println(location + " " + street + " " + bnum + ": " + sameAddress.size());
				// собираем в кучку все данные с записей о домах с совпадающим адресом.
				Element combined = new Element("Building");
				for (Element buildingSA: sameAddress) {
					buildingSA.detach();
					List<Element> properties = buildingSA.getChildren();
					for (Element p: properties) {
						String name = p.getName();
						String value = p.getText();
						if (value.equals("нет данных") || value.isEmpty())
							continue;
						Element updatedProperty = combined.getChild(name);
						if (updatedProperty == null) {
							updatedProperty = new Element(name);
							combined.addContent(updatedProperty);
						}
						updatedProperty.setText(value);
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
}
