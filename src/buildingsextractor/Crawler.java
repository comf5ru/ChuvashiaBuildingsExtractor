package buildingsextractor;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.jdom2.Document;

/**
 * Класс для обработки множества страниц.
 * Получает адрес "стартовой страницы" и затем организует
 * скачивание/парсинг страниц и их "дочерних" страниц, когда необходимо.
 * 
 * Создаёт и контроллирует множество подчинённых объектов PageDownloader, работающих параллельно 
 * (multi-threading).
 */
public class Crawler extends PageDownloader {
	/**
	 * Ссылка на внешнее хранилище, куда будут сохраняться созданные объекты из страниц
	 */
	private final Collection<Building> results;
	
	
	/**
	 * Just a vector of objects {PageDownloader, Future<?>}, where Future is generated for said downloader.
	 * This is to track Futures for jobs, to cancel specific jobs, that has not been started yet.
	 */
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

	/**
	 * Конструктор
	 * @param stringURL - URL начальной страницы. Чтобы всё работало верно должен быть начальной страницей города:
	 *  "http://www.reformagkh.ru/myhouse/list?tid=2358783"  -- для Чебоксар
	 * @param results
	 * @param threadsNumber
	 */
	public Crawler(String stringURL, Collection<Building> results, int threadsNumber) {
		super(stringURL);
		this.results = results;
		if (threadsNumber <= 0)
			threadsNumber = CORETHREADS_NUMBER;
		executor = Executors.newFixedThreadPool(threadsNumber);
		assert (executor instanceof ThreadPoolExecutor);
		submittedPairs = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Метод для запуска полного всего цикла работы этого краулера. 
	 * Должен запускаться только один раз из главного треда (Main)
	 */
	@Override
	public void run(){
		try {
			//1. download base page to this.dom;
			super.run(); 
			
			//2. create more PageDownloader objects by parsing this page;
			//2.1 Получить номер последней страницы пейджера
			int lastPage = 242; // TODO
			
			//2.2 Создать задания загрузки всех страниц пейджера
			for (int i=1; i<=lastPage; i++) {
				GKHPagerPage page = new GKHPagerPage(
						url.toExternalForm()+"&page="+String.valueOf(i),
						this);
				submit(page);
			}
			
			//3. loop until all the objects are finished.
			// wait until all the jobs are finished before returning.
			while (!submittedPairs.isEmpty()) {
					Future<?> headFuture = submittedPairs.peek().future; 
					// Отправка задания Future на выполнение (скачивание и далее).
					// Результат в сооветствующем объекте submittedPairs.peek().job 
					headFuture.get(); // can't use awaitTermination, because jobs are submitting other jobs
					
					JobFuturePair finishedPair = submittedPairs.poll(); // no synch with peek and isEmpty is needed as all elements are added to the tail and only this thread is running task removal.
					// Если finishedPair - объект загрузки дома, то присоединить дом к результатам.
					if (finishedPair.job instanceof GKHBuildingPage) 
						results.add(((GKHBuildingPage)finishedPair.job).building);
			}
		
		} catch (InterruptedException | ExecutionException e) {
			// Stop all jobs
			executor.shutdown(); // no new jobs submitted
			
			// stop all jobs submitted (whether running or not)
			for (JobFuturePair pair: submittedPairs)
				pair.future.cancel(true);
		}
		executor.shutdown();
	}
	
	/**
	 * Entry point for Jobs to add more children jobs into a queue
	 * @param job - new job to be executed some moment in the future.
	 * @throws InterruptedException 
	 */
	synchronized public
	void submit (PageDownloader job) throws InterruptedException {
		// if thread submitting a job was interrupted while waiting on synchronization
		//  then do not submit 
		if (Thread.interrupted())
			throw new InterruptedException();
		
		submittedPairs.add(new JobFuturePair(job, executor.submit(job)));
	}
	
}
