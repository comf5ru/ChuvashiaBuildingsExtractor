package buildingsextractor;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestCrawler {
	/**
	 *  ласс, повтор€ющий Crawler, но не добавл€ющий новых подзаданий в очередь кроме нескольких первых 
	 * (аргумент конструктора allowSubmits). 
	 * ¬место этого они сохран€ютс€ в член класса queue (дл€ отладочных целей).
	 */
	public class ParalyzedCrawler extends Crawler {
		
		public LinkedList<PageDownloader> queue;
		public int submitsLeft;

		public ParalyzedCrawler(String stringURL, int threadsNumber, int allowSubmits) {
			super(stringURL, threadsNumber, null);
			queue = new LinkedList<>();
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
		paralyzedCrawler = new ParalyzedCrawler("http://www.reformagkh.ru/myhouse/list?tid=2358783", 4, 0);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBasePageParsing() {
		paralyzedCrawler.run();
		assertNotNull(paralyzedCrawler.queue);
		assertTrue(paralyzedCrawler.queue.size()>=1);
		assertTrue("ƒл€ „ебоксар", paralyzedCrawler.queue.size()>=242);
		
		assertEquals(paralyzedCrawler.url.toExternalForm()+"&page=1",paralyzedCrawler.queue.getFirst().url.toExternalForm());
		assertEquals(paralyzedCrawler.url.toExternalForm()+"&page="+String.valueOf(paralyzedCrawler.queue.size()),
				     paralyzedCrawler.queue.getLast().url.toExternalForm());
	}

	@Test
	public void testPagerPageSubmits() {
		paralyzedCrawler = new ParalyzedCrawler("http://www.reformagkh.ru/myhouse/list?tid=2358783", 4, 2);
		paralyzedCrawler.run();
		
		assertNotNull(paralyzedCrawler.queue);
		assertTrue(paralyzedCrawler.queue.size()>=1);
		assertTrue("ƒл€ „ебоксар", paralyzedCrawler.queue.size() == 260); // 242 -2 + 2*10
		assertNotEquals(paralyzedCrawler.url.toExternalForm()+"&page=1",paralyzedCrawler.queue.getFirst().url.toExternalForm());
		assertEquals(paralyzedCrawler.url.toExternalForm()+"&page=3",paralyzedCrawler.queue.getFirst().url.toExternalForm());

	}
}
