package buildingsextractor;

import java.util.Collection;

public class GKHPagerPage extends PageDownloader {

	public Collection<String> buildingURLs;
	public GKHPagerPage(String stringURL) {
		super(stringURL);
	}

	@Override
	public void run() {
		super.run(); // --> dom
		
		//TODO добавить все дома на этой странице в список загрузки ->buildingURLs
		
	}

	
}
