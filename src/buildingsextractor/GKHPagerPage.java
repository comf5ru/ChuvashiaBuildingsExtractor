package buildingsextractor;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Element;

/**
 * Класс загружает страницу со списком домов (одну из страниц пейджера) и создаёт новые задания в очередь 
 * загрузки - индивидуальные страницы для каждого дома 
 *
 */
public class GKHPagerPage extends PageJob {

	/**
	 * XPath доступ к элементу A на странице (немного странный синтаксис обеспечивает гибкость на случай, 
	 * если порядок классов будет изменён в будующем: class="c1 c2" на class="c12 c1", например)
	 */
	private static final String BUILDING_LINK = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' printAnketa ')]"
			+ "//html:table[contains(concat(' ', normalize-space(@class), ' '), ' b-rating-table ')]"
			+ "//html:td/html:a";

	/**
	 * Результат работы - список адресов страниц домов
	 */
	public Collection<String> buildingURLs;
	
	public GKHPagerPage(String stringURL, Crawler jobMaster) {
		super(stringURL, jobMaster);
		buildingURLs = new LinkedList<>();
	}

	@Override
	public void run() {
		super.run(); // --> dom
		List<Element> result = Main.queryXPathList(BUILDING_LINK, dom.getRootElement());
	
		for(Element element : result) {
			String href = element.getAttribute("href").getValue();
			String targetURLString = null;
			try {
				targetURLString = resolveLink(href).toExternalForm();
				buildingURLs.add(targetURLString);
				if (jobMaster != null)
					jobMaster.submit(new GKHBuildingPage(targetURLString, jobMaster));
			} catch (MalformedURLException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		if (jobMaster != null) 
			jobMaster.report_finished();
	}
	
}
