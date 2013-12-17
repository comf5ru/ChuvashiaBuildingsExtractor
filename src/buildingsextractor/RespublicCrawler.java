package buildingsextractor;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;

import org.jdom2.Element;

/**
 * � ������� �� ������, ��� ����������� ����� ������������� ��� ��������� �������� ������� (����������) �
 * ����� ���������� � ������� (��������� �������), ������� ��� �����, � ���� �������, ������������
 * ��� ������� Crawler
 *
 */
public class RespublicCrawler extends Crawler {

	public RespublicCrawler(String stringURL, Collection<?> results,
			int threadsNumber) {
		super(stringURL, results, threadsNumber);
	}

	/**
	 * ������� �������, ���������� ������ �� ����� ��� ������ �� �����
	 */
	private static final String LOCATIONS_TD = "//html:td[contains(concat(' ', normalize-space(@class), ' '), ' location ')]";
	
	/**
	 * ������ �� �����
	 */
	private static final String EXPAND_DIV_LINK = 
			".//html:div[contains(concat(' ', normalize-space(@class), ' '), ' expand-button ')]"
			+ "/../html:a";
	/**
	 * ������ �� �����.
	 */
	private static final String LINK = ".//html:a";
	
	@Override
	protected
	void parseSelf() throws InterruptedException, MalformedURLException {
		//2.1 �������� ��� ��������
		List<Element> tds = Main.queryXPathList(LOCATIONS_TD, dom.getRootElement());
		for (Element el: tds) {
			List<Element> expanded_link = Main.queryXPathList(EXPAND_DIV_LINK, el);
			
			if (expanded_link.size() > 0) {
				// el - td, ������� �������� �������� "+", � expanded_link - ������ �� ������������ ��������
				String relativeURL = expanded_link.get(0).getAttributeValue("href");
				String fullURL = resolveLink(relativeURL).toExternalForm();
				submit(new GKHLocationsPage(fullURL, this));
				
			} else {
				// el - td, ������� �������� ������ ����� �� �����
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
