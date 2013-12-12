package buildingsextractor;

import buildingsextractor.PageDownloader;
/**
 * Главный класс, будет запускать всю работу.
 * 
 */
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		PageDownloader myPage = new PageDownloader("http://google.com");
//		myPage.run();
		
//		Building b = new Building("no url", null);
//		System.out.print(myPage.dom != null);
		GKHPagerPage pp = new GKHPagerPage("http://www.reformagkh.ru/myhouse/list?tid=2358783&page=2", null);
		pp.run();
		for (String link: pp.buildingURLs){
			System.out.println(link);
		}
	}

}
