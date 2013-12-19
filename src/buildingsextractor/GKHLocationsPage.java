package buildingsextractor;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.jdom2.Element;

/**
 * Класс загружает страницу области и получает из неё список населённых пунктов.
 * Сохраняет в results список наборов Properties с ключами {"url", "areaName", "locationName"} 
 *
 */
public class GKHLocationsPage extends PageJob {

	/**
	 * Путь Xpath до ссылки на населённых пункт 
	 */
	private static final String LOCATIONS_TD_LINK = 
			"//html:td[contains(concat(' ', normalize-space(@class), ' '), ' location ')]"
			+ "//html:a";
	
	public Collection<Properties> results;
	
	public String areaName;
	
	public GKHLocationsPage(String stringURL, Crawler owner, String areaName) {
		super(stringURL, owner);
		results = new LinkedList<Properties>();
		this.areaName = areaName;
	}
	
	@Override
	public void run() {
		super.run();

		List<Element> links = Main.queryXPathList(LOCATIONS_TD_LINK, dom.getRootElement());
		for (Element link: links) {
			try {
				Properties location = new Properties();
				if (areaName!= null) location.setProperty("areaName", areaName);
				String locationName = Main.UTF8_decode(link.getText());
				location.setProperty("locationName", Main.sanitizeString(locationName));
				location.setProperty("url", resolveLink(link.getAttributeValue("href")).toExternalForm());
				results.add(location);
			} catch (MalformedURLException e) {
				System.err.println("[GKHLocationsPage] Cant resolve link ("+link.getAttributeValue("href")+") from ("+url+")");
			}
		}
		if (jobMaster != null) 
			jobMaster.report_finished();

	}
	
	

}
