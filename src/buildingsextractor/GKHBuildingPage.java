package buildingsextractor;

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
		
		building = new Building(url.toExternalForm(), dom);
	}

	
}
