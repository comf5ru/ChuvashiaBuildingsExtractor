package buildingsextractor;

import org.jdom2.Document;

/**
 * Класс отвечает за загрузку страницы описания дома и создание
 * объекта Building по результатам загрузки. 
 *
 */
public class GKHBuildingPage extends PageDownloader {
	
	/**
	 * Новый объект Building созданный по результатам загрузки страницы
	 */
	public Building building = null;

	public GKHBuildingPage(String stringURL) {
		super(stringURL);
	}

	@Override
	public void run() {
		super.run(); // --> dom
		
		building = new Building(url.toExternalForm(), dom);
	}

	
}
