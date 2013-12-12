package buildingsextractor;

/**
 * Класс отвечает за загрузку страницы описания дома и создание
 * объекта Building по результатам загрузки. 
 *
 */
public class GKHBuildingPage extends PageJob {
	
	/**
	 * Новый объект Building созданный по результатам загрузки страницы
	 */
	public Building building = null;

	public GKHBuildingPage(String stringURL, Crawler jobMaster) {
		super(stringURL, jobMaster);
	}

	@Override
	public void run() {
		super.run(); // --> dom
		
		building = new Building(url.toExternalForm(), dom);
	}

	
}
