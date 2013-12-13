package buildingsextractor;

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
	 * Доступ к элементу А содержащему номер последней страницы пейджера
	 */
	private static final String LOCATIONS_TD = "//html:td[contains(concat(' ', normalize-space(@class), ' '), ' location ')]";
	private static final String EXPAND_DIV_LINK = 
			"//html:div[contains(concat(' ', normalize-space(@class), ' '), ' expand-button ')]"
			+ "/../html:a";
	
	private static final String LINK = "//html:a";
	
	@Override
	protected
	void parseSelf() throws InterruptedException {
		//2.1 Получить все элементы
		List<Element> tds = Main.queryXPathList(LOCATIONS_TD, dom.getRootElement());
		for (Element el: tds) {
			List<Element> expanded_link = Main.queryXPathList(EXPAND_DIV_LINK, el);
			
			if (expanded_link.size() > 0) {
				// el - td, который содержит кнопочку "+", а expanded_link - ссылка на подгружаемую страницу
				submit(new GKHLocationsPage(expanded_link.get(0).getAttributeValue("href"), this));
				
			} else {
				// el - td, который содержит ссылку прямо на город
				List<Element> link = Main.queryXPathList(LINK, el);
				if (link.size() != 1) 
					System.err.println("Not a single <A> link in unexpanded <TD> element");
				else
					results.add(link.get(0).getAttributeValue("href")); 
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
