package buildingsextractor;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

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

		public ParalyzedCrawler(String stringURL, Collection<Building> results,
				int threadsNumber, int allowSubmits) {
			super(stringURL, results, threadsNumber);
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
		paralyzedCrawler = new ParalyzedCrawler("http://www.reformagkh.ru/myhouse?tid=2358768",result,4, 0); 
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRunBasePage() {
		paralyzedCrawler.run();
		assertEquals(5, result.size());
		assertNotEquals(0,paralyzedCrawler.queue.size());
		assertEquals(paralyzedCrawler.queueURLS.size(),paralyzedCrawler.queue.size());
		assertTrue(result.contains("http://www.reformagkh.ru/myhouse/list?tid=2358785"));
		assertTrue(result.contains("http://www.reformagkh.ru/myhouse/list?tid=2358787"));
		assertTrue(paralyzedCrawler.queueURLS.contains("http://www.reformagkh.ru/myhouse?tid=2358782&sort=alphabet&item=mkd"));
	}

}
