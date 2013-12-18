package buildingsextractor;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * ����� �������� �� �������� �������� �������� ���� � ��������
 * ������� Building �� ����������� ��������. 
 *
 */
public class GKHBuildingPage extends PageJob {
	
	/**
	 * ����� ������ Building ��������� �� ����������� �������� ��������
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
