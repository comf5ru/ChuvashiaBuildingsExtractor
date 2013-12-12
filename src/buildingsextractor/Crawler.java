package buildingsextractor;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Класс для обработки множества страниц.
 * Получает адрес "стартовой страницы" и затем организует
 * скачивание/парсинг страниц и их "дочерних" страниц, когда необходимо.
 * 
 * Создаёт и контроллирует множество подчинённых объектов PageDownloader, работающих параллельно 
 * (multi-threading).
 */
public class Crawler extends PageDownloader{
	
	/**
	 * Ссылка на внешнее хранилище, куда будут сохраняться созданные объекты из страниц
	 */
	private final Collection<Building> results;
	
	
	/**
	 * Just a vector of objects {PageDownloader, Future<?>}, where Future is generated for said downloader.
	 * This is to track Futures for jobs, to cancel specific jobs, that has not been started yet.
	 */
	@SuppressWarnings("unused")
	private
	class JobFuturePair {
		public final PageDownloader job;
		public final Future<?> future;
		
		JobFuturePair(PageDownloader job, Future<?> future) {
			this.job = job; this.future = future;
		}
	}

	/**
	 * All submitted jobs have their Future's here. For result reporting/error checking/job resubmitting... etc
	 */
	private ConcurrentLinkedQueue<JobFuturePair> submittedPairs; 
	
	/**
	 * number of threads running in parallel by default
	 */
	static final public int CORETHREADS_NUMBER = 4; 
	
	/**
	 * Associated job executor
	 */
	private ExecutorService executor;	

	public Crawler(String stringURL, Collection<Building> results, int threadsNumber) {
		super(stringURL);
		this.results = results;
		if (threadsNumber <= 0)
			threadsNumber = CORETHREADS_NUMBER;
		executor = Executors.newFixedThreadPool(threadsNumber);
		assert (executor instanceof ThreadPoolExecutor);
		submittedPairs = new ConcurrentLinkedQueue<>();
	}

	@Override
	public void run() {
		//1. download base page to this.dom;
		super.run(); 
		
		//2. create more PageDownloader objects by parsing this page;
		
		//3. loop until all the objects are finished.
		// wait until all the jobs are finished before returning.
		while (!submittedPairs.isEmpty()) {
			try {
				Future<?> headFuture = submittedPairs.peek().future; 
				headFuture.get(); // can't use awaitTermination, because jobs are submitting other jobs
				submittedPairs.poll(); // no synch with peek and isEmpty is needed as all elements are added to the tail and only this thread is running task removal.
			} catch (InterruptedException | ExecutionException e) {
				// Stop all jobs
				executor.shutdown(); // no new jobs submitted
				
				// stop all jobs submitted (whether running or not)
				for (JobFuturePair pair: submittedPairs)
					pair.future.cancel(true);
				break;
			}
		}
		
		executor.shutdown();		
	}
	

	
}
