package buildingsextractor;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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
		DateTime dt = new DateTime();
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		building = new Building(url.toExternalForm(), dom, fmt.print(dt));
		if (jobMaster != null) 
			jobMaster.report_finished();
	}

	
}
