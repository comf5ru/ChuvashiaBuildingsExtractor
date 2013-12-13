package buildingsextractor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Element;

public class GKHLocationsPage extends PageJob {

	private static final String LOCATIONS_TD_LINK = 
			"//html:td[contains(concat(' ', normalize-space(@class), ' '), ' location ')]"
			+ "//html:a";
	
	public Collection<String> results;
	public GKHLocationsPage(String stringURL, Crawler owner) {
		super(stringURL, owner);
		results = new LinkedList<String>();
	}
	@Override
	public void run() {
		super.run();

		List<Element> links = Main.queryXPathList(LOCATIONS_TD_LINK, dom.getRootElement());
		for (Element link: links) {
			results.add(link.getAttributeValue("href"));
		}

	}
	
	

}
