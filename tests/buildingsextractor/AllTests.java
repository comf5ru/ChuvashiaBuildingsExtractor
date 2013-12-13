package buildingsextractor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestCrawler.class, TestGKHBuildingPage.class,
		TestGKHPagerPage.class, TestPageDownloader.class, TestXMLStorage.class })
public class AllTests {

}
