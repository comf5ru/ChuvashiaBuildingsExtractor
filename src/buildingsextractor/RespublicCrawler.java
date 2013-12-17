package buildingsextractor;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;

import org.jdom2.Element;

/**
 * В отличие от предка, эта модификация паука предназначена для обработки страницы региона (республики) и
 * сбора информации о городах (населённых пунктах), которые уже будут, в свою очередь, использованы
 * для запуска Crawler
 *
 */
public class RespublicCrawler extends Crawler {

	public RespublicCrawler(String stringURL, Collection<?> results,
			int threadsNumber) {
		super(stringURL, results, threadsNumber);
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
				String fullURL = resolveLink(relativeURL).toExternalForm();
				submit(new GKHLocationsPage(fullURL, this));
				
			} else {
				// el - td, который содержит ссылку прямо на город
				List<Element> direct_link = Main.queryXPathList(LINK, el);
				if (direct_link.size() != 1) 
					System.err.println("Not a single <A> link in unexpanded <TD> element");
				else {
					String relativeURL = direct_link.get(0).getAttributeValue("href");
					String fullURL = resolveLink(relativeURL).toExternalForm();
					results.add(fullURL);
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
