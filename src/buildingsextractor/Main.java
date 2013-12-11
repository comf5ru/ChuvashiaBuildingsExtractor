package buildingsextractor;

import buildingsextractor.PageDownloader;
/*
 * Главный класс, будет запускать всю работу.
 * 
 */
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		PageDownloader myPage = new PageDownloader("http://google.com");
		myPage.run();
		
		System.out.print(myPage.dom != null);
	}

}
