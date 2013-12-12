package buildingsextractor;

import java.util.Collection;

public class GKHPagerPage extends PageJob {

	public Collection<String> buildingURLs;
	public GKHPagerPage(String stringURL, Crawler jobMaster) {
		super(stringURL, jobMaster);
	}

	@Override
	public void run() {
		super.run(); // --> dom
		
		//TODO добавить все дома на этой странице в список загрузки ->buildingURLs
		
	}

	
}
