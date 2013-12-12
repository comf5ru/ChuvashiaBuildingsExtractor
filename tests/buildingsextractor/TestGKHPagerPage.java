package buildingsextractor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGKHPagerPage {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRun() {
		GKHPagerPage pp = new GKHPagerPage("http://www.reformagkh.ru/myhouse/list?tid=2358783&page=2", null);
		pp.run();
		assertNotNull("Null results", pp.buildingURLs);
		assertTrue("No results", pp.buildingURLs.size()>0);
		assertEquals("Not 10 results for a page", 10, pp.buildingURLs.size());
		assertTrue(pp.buildingURLs.contains("http://www.reformagkh.ru/myhouse/view/7967974/?group=0"));
		assertTrue(pp.buildingURLs.contains("http://www.reformagkh.ru/myhouse/view/8185515/?group=0"));
	}

}
