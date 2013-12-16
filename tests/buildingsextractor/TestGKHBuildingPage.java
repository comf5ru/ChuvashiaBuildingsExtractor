package buildingsextractor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGKHBuildingPage {

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
		GKHBuildingPage bp = new GKHBuildingPage("http://www.reformagkh.ru/myhouse/view/7967974/?group=0", null);
		bp.run();
		assertNotNull("Null building generated", bp.building);
		assertNotNull("Building has null data", bp.building.data);
		assertNotNull("Building has null date", bp.building.pageDownloadDate);
	}
	
	@Test
	public void testBuildingParsed() {
		GKHBuildingPage bp = new GKHBuildingPage("http://www.reformagkh.ru/myhouse/view/7967974/?group=0", null);
		bp.run();
		assertEquals("г Чебоксары",bp.building.data.getProperty("Населённый пункт"));
		assertEquals("б-р А.Миттова",bp.building.data.getProperty("Улица"));
		assertEquals("17",bp.building.data.getProperty("Номер дома"));
	}

	@Test
	public void testBuildingParsedSpaceTown() {
		GKHBuildingPage bp = new GKHBuildingPage("http://www.reformagkh.ru/myhouse/view/6663275/?group=0", null);
		bp.run();
		assertEquals("г Мариинский Посад",bp.building.data.getProperty("Населённый пункт"));
		assertEquals("ул Больничная",bp.building.data.getProperty("Улица"));
		assertEquals("13 кор.3",bp.building.data.getProperty("Номер дома"));		
	}
	
}
