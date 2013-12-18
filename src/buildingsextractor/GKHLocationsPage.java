package buildingsextractor;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Element;

/**
 * Класс загружает страницу области и получает из неё список (String) населённых пунктов 
 *
 */
public class GKHLocationsPage extends PageJob {

	/**
	 * Путь Xpath до ссылки на населённых пункт 
	 */
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
			try {
				results.add(resolveLink(link.getAttributeValue("href")).toExternalForm());
			} catch (MalformedURLException e) {
				System.err.println("[GKHLocationsPage] Cant resolve link ("+link.getAttributeValue("href")+") from ("+url+")");
			}
		}
		jobMaster.report_finished();
	}
	
	

}
