package buildingsextractor;

import org.jdom2.Document;

/**
 * ����� �������� �� �������� �������� �������� ���� � ��������
 * ������� Building �� ����������� ��������. 
 *
 */
public class GKHBuildingPage extends PageDownloader {
	
	/**
	 * ����� ������ Building ��������� �� ����������� �������� ��������
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
