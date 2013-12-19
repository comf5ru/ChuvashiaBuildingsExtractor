package buildingsextractor;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestRespublicCrawler {
	/**
	 *  ласс, повтор€ющий Crawler, но не добавл€ющий новых подзаданий в очередь кроме нескольких первых 
	 * (аргумент конструктора allowSubmits). 
	 * ¬место этого они сохран€ютс€ в член класса queue (дл€ отладочных целей).
	 */
	public class ParalyzedCrawler extends RespublicCrawler {
		
		public LinkedList<PageDownloader> queue;
		public LinkedList<String> queueURLS;
		public int submitsLeft;

		public ParalyzedCrawler(String stringURL, int threadsNumber, int allowSubmits) {
			super(stringURL, threadsNumber);
			queue = new LinkedList<>();
			queueURLS = new LinkedList<>();
			submitsLeft = allowSubmits;
		}

		@Override
		public synchronized void submit(PageDownloader job)
				throws InterruptedException {
			// if thread submitting a job was interrupted while waiting on synchronization
			//  then do not submit 
			if (Thread.interrupted())
				throw new InterruptedException();
			
			if (submitsLeft-- > 0)
				super.submit(job);
			else {
				queue.add(job);
				queueURLS.add(job.url.toExternalForm());
				assert(queue.size() == queueURLS.size());
			}
		}
	}

	public Collection<Building> result;
	public ParalyzedCrawler paralyzedCrawler;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		result = new LinkedList<Building>();
		paralyzedCrawler = new ParalyzedCrawler("http://www.reformagkh.ru/myhouse?tid=2358768",4, 0); 
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRunBasePage() {
		paralyzedCrawler.run();
		Collection<Object> result = paralyzedCrawler.results;
		assertEquals(5, result.size());
		assertNotEquals(0,paralyzedCrawler.queue.size());
		assertEquals(paralyzedCrawler.queueURLS.size(),paralyzedCrawler.queue.size());
		boolean found1=false; boolean found2=false;
		for(Object p: result) {
			Properties prop = (Properties) p;
			if (prop.getProperty("url").equals("http://www.reformagkh.ru/myhouse/list?tid=2358785"))
				found1 = true;
			if (prop.getProperty("url").equals("http://www.reformagkh.ru/myhouse/list?tid=2358787"))
				found2 = true;
		}
		assertTrue(found1 && found2);
		assertTrue(paralyzedCrawler.queueURLS.contains("http://www.reformagkh.ru/myhouse?tid=2358782&sort=alphabet&item=mkd"));
	}

}
