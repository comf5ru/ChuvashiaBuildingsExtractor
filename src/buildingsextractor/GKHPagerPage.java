package buildingsextractor;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Element;

public class GKHPagerPage extends PageJob {

	private static final String BUILDING_LINK = 
			"//html:div[@id='printAnketa']//html:table[contains(concat(' ', normalize-space(@class), ' '), ' b-rating-table ')]//html:td/html:a";

	public Collection<String> buildingURLs;
	public GKHPagerPage(String stringURL, Crawler jobMaster) {
		super(stringURL, jobMaster);
		buildingURLs = new LinkedList<>();
	}

	@Override
	public void run() {
		super.run(); // --> dom
		List<Element> result = queryXPathList(BUILDING_LINK, dom.getRootElement());
	
		for(Element element : result) {
			String href = element.getAttribute("href").getValue();
			String targetURLString = null;
			try {
				targetURLString = resolveLink(href).toExternalForm();
				buildingURLs.add(targetURLString);
				if (jobMaster != null)
					jobMaster.submit(new GKHBuildingPage(targetURLString, jobMaster));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
