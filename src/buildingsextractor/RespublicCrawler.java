package buildingsextractor;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

import org.jdom2.Element;

/**
 * В отличие от предка, эта модификация паука предназначена для обработки страницы региона (республики) и
 * сбора информации о городах (населённых пунктах), которые уже будут, в свою очередь, использованы
 * для запуска Crawler
 * Получает URL республики, cохраняет в results список наборов Properties с ключами {"url", "areaName", "locationName"},
 * где url - адрес страницы населённого пункта 
 */
public class RespublicCrawler extends Crawler {

	public RespublicCrawler(String stringURL, int threadsNumber) {
		super(stringURL, threadsNumber, null);
	}

	/**
	 * Элемент таблицы, содержащий ссылку на город или ссылку на район
	 */
	private static final String LOCATIONS_TD = "//html:td[contains(concat(' ', normalize-space(@class), ' '), ' location ')]";
	
	/**
	 * Ссылка на район
	 */
	private static final String EXPAND_DIV_LINK = 
			".//html:div[contains(concat(' ', normalize-space(@class), ' '), ' expand-button ')]"
			+ "/../html:a";
	/**
	 * Ссылка на город.
	 */
	private static final String LINK = ".//html:a";
	
	@Override
	protected
	void parseSelf() throws InterruptedException, MalformedURLException {
		//2.1 Получить все элементы
		List<Element> tds = Main.queryXPathList(LOCATIONS_TD, dom.getRootElement());
		for (Element el: tds) {
			List<Element> expanded_link = Main.queryXPathList(EXPAND_DIV_LINK, el);
			
			if (expanded_link.size() > 0) {
				// el - td, который содержит кнопочку "+", а expanded_link - ссылка на подгружаемую страницу
				String relativeURL = expanded_link.get(0).getAttributeValue("href");
				String areaName = Main.UTF8_decode(expanded_link.get(0).getText());
				String fullURL = resolveLink(relativeURL).toExternalForm();
				submit(new GKHLocationsPage(fullURL, this, Main.sanitizeString(areaName)));
			} else {
				// el - td, который содержит ссылку прямо на город
				List<Element> direct_link = Main.queryXPathList(LINK, el);
				if (direct_link.size() != 1) 
					System.err.println("Not a single <A> link in unexpanded <TD> element");
				else {
					String relativeURL = direct_link.get(0).getAttributeValue("href");
					String fullURL = resolveLink(relativeURL).toExternalForm();
					
					Properties resProps = new Properties();
					String locationName = Main.UTF8_decode(direct_link.get(0).getText());
					resProps.setProperty("locationName", Main.sanitizeString(locationName));
					resProps.setProperty("url", fullURL);
					results.add(resProps);
				}
			}
		}
	}

	@Override
	protected
	void proccessFinished(JobFuturePair finishedPair) {
		if (finishedPair.job instanceof GKHLocationsPage) {
			results.addAll(((GKHLocationsPage)finishedPair.job).results);
		}
	}

	
}
