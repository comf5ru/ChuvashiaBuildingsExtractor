package buildingsextractor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;

import org.jdom2.Element;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestXMLStorage {
	/**
	 *  ласс, повтор€ющий Crawler, но не добавл€ющий новых подзаданий в очередь кроме GKHBuildingPage
	 * ¬место этого они сохран€ютс€ в член класса queue (дл€ отладочных целей).
	 */
	public class PickyCrawler extends Crawler {
		
		public LinkedList<PageDownloader> queue;

		public PickyCrawler(String stringURL, Collection<Building> results,
				int threadsNumber) {
			super(stringURL, results, threadsNumber);
			queue = new LinkedList<>();
		}

		public synchronized void submitForce(PageDownloader job) throws InterruptedException {
			super.submit(job);
		}
		@Override
		public synchronized void submit(PageDownloader job)
				throws InterruptedException {
			// if thread submitting a job was interrupted while waiting on synchronization
			//  then do not submit 
			if (Thread.interrupted())
				throw new InterruptedException();
			
			if (job instanceof GKHBuildingPage)
				super.submit(job);
			else 
				queue.add(job);
		}
	}
	
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
		assertNotNull(bp.building.data);
		assertNotEquals(0,bp.building.data.size());
		
//		bp.building.parse_data();
		
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
	
	
	@Test
	public void testSaveOnePageBuildings() throws InterruptedException, IOException {
		LinkedList<Building> result = new LinkedList<>();
		PickyCrawler cr= new PickyCrawler("http://www.reformagkh.ru/myhouse/list?tid=2358783",result,4);
		cr.submitForce(new GKHPagerPage("http://www.reformagkh.ru/myhouse/list?tid=2358783&page=2", cr));
		cr.run();
		
		assertEquals("Ќе 10 домов на страницу", 10, result.size());
//		for (Building b: result) 
//			b.parse_data();
		
		XMLStorage s = new XMLStorage(tmpfilename, true);
		s.saveBuildings(result);
		Path file = Paths.get(tmpfilename);
		assertTrue("File was not created", Files.exists(file));
		
	}
	
}
