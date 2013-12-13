package buildingsextractor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.jdom2.Element;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestXMLStorage {
	static public String tmpfilename = "test_storage.xml";

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
		Path file = Paths.get(tmpfilename);
		if (Files.exists(file))
			Files.delete(file);
	}

	@Test
	public void testConstructor() throws IOException {
		XMLStorage s = new XMLStorage(tmpfilename, false);
		s.saveBuildings(new LinkedList<Building>());
		
		Path file = Paths.get(tmpfilename);
		assertTrue("File was not created", Files.exists(file));
	}

	@Test
	public void testSaveBuilding_one_building() throws IOException {
		GKHBuildingPage bp = new GKHBuildingPage("http://www.reformagkh.ru/myhouse/view/7967974/?group=0", null);
		bp.run();
		assertNotNull(bp.building);
		assertNotNull(bp.building.dom);
		
		bp.building.parse_data();
		
		XMLStorage s = new XMLStorage(tmpfilename, true);
		LinkedList<Building> l = new LinkedList<Building>();
		l.add(bp.building);
		s.saveBuildings(l);
		
		Path file = Paths.get(tmpfilename);
		assertTrue("File was not created", Files.exists(file));
		Element e = s.cache.getElementForPage("http://www.reformagkh.ru/myhouse/view/7967974/?group=0");
		assertNotNull(e);
		
		XMLStorage s2 = new XMLStorage(tmpfilename, false);
		Element e2 = s2.cache.getElementForPage("http://www.reformagkh.ru/myhouse/view/7967974/?group=0");
		assertNotNull(e2);
		
		XMLStorage s3 = new XMLStorage(tmpfilename, true);
		Element e3 = s3.cache.getElementForPage("http://www.reformagkh.ru/myhouse/view/7967974/?group=0");
		assertNull(e3);
	}
}
