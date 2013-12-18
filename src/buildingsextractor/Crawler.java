package buildingsextractor;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.jdom2.Element;

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
	 * Доступ к элементу А содержащему номер последней страницы пейджера
	 */
	private static final String PAGER_LAST_PAGE_LINK = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' printAnketa ')]"
			+ "//html:ul[contains(concat(' ', normalize-space(@class), ' '), ' pager ')]"
			+ "/html:li[contains(concat(' ', normalize-space(@class), ' '), ' pager-last ')]"
			+ "//html:a";
	
	/**
	 * Ссылка на внешнее хранилище, куда будут сохраняться созданные объекты из страниц
	 */
	protected final Collection<Object> results;
	
	/**
	 * Just a vector of objects {PageDownloader, Future<?>}, where Future is generated for said downloader.
	 * This is to track Futures for jobs, to cancel specific jobs, that has not been started yet.
	 */
	protected
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

	// Следующие переменные служат для отслеживания прогресса загрузок
	private
	int pageCounter = 1;
	private	final
	int updateGranularity = 10;
	private
	int updateGranula = 0;
	private
	long startingTime;
	public 
	int totalSubmitted = 0;
	public 
	int skipped = 0;

	// Следующие переменные позволяют пропустить загрузку зданий, если они уже есть в кэше.
//	private XMLStorage referenceStorage;
//	private long referenceAliveTime;
	
	/**
	 * Конструктор
	 * @param stringURL - URL начальной страницы. Чтобы всё работало верно должен быть начальной страницей города:
	 *  "http://www.reformagkh.ru/myhouse/list?tid=2358783"  -- для Чебоксар
	 * @param results2 - куда выгружать результаты
	 * @param threadsNumber - максимальное количество одновременных закачек
	 */
	@SuppressWarnings("unchecked")
	public Crawler(String stringURL, Collection<?> results2, int threadsNumber) {
		super(stringURL);
		this.results = (Collection<Object>) results2;
		if (threadsNumber <= 0)
			threadsNumber = CORETHREADS_NUMBER;
		executor = Executors.newFixedThreadPool(threadsNumber);
		assert (executor instanceof ThreadPoolExecutor);
		submittedPairs = new ConcurrentLinkedQueue<>();
//		referenceStorage = null;
	}

	/**
	 * Конструктор с заданием кэша (для пропусков загрузок существующих данных)
	 * @param stringURL - URL начальной страницы. Чтобы всё работало верно должен быть начальной страницей города:
	 *  "http://www.reformagkh.ru/myhouse/list?tid=2358783"  -- для Чебоксар
	 * @param results2 - куда выгружать результаты
	 * @param threadsNumber - максимальное количество одновременных закачек
	 * @param cacheStorage - хранит уже загруженные данные.
	 * @param cacheAliveTime - таймаут валидности данных в кэше
	 */
//	@SuppressWarnings("unchecked")
//	public Crawler(String stringURL, Collection<?> results2, int threadsNumber,
//			XMLStorage cacheStorage, long cacheAliveTime) {
//		super(stringURL);
//		this.results = (Collection<Object>) results2;
//		if (threadsNumber <= 0)
//			threadsNumber = CORETHREADS_NUMBER;
//		executor = Executors.newFixedThreadPool(threadsNumber);
//		assert (executor instanceof ThreadPoolExecutor);
//		submittedPairs = new ConcurrentLinkedQueue<>();
//		referenceStorage = cacheStorage;
//		referenceAliveTime = cacheAliveTime;
//	}

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
			parseSelf();
			
			//3. loop until all the objects are finished.
			// wait until all the jobs are finished before returning.
			
			//3.0 Total pages estimate;
			pageCounter = 1;
			updateGranula = 0;
			startingTime = System.nanoTime();
			while (!submittedPairs.isEmpty()) {
				Future<?> headFuture = submittedPairs.peek().future; 
				// Отправка задания Future на выполнение (скачивание и далее).
				// Результат в сооветствующем объекте submittedPairs.peek().job 
				headFuture.get(); // can't use awaitTermination, because jobs are submitting other jobs
				proccessFinished(submittedPairs.poll()); // no synch with peek and isEmpty is needed as all elements are added to the tail and only this thread is running task removal.
			}
		
		} catch (InterruptedException | ExecutionException | MalformedURLException e) {
			// Stop all jobs
			executor.shutdown(); // no new jobs submitted
			
			// stop all jobs submitted (whether running or not)
			for (JobFuturePair pair: submittedPairs)
				pair.future.cancel(true);
		}
		executor.shutdown();
	}
	
	/**
	 * Получить подстраницы после загрузки стартовой страницы
	 * @throws InterruptedException
	 * @throws MalformedURLException 
	 */
	protected
	void parseSelf() throws InterruptedException, MalformedURLException {
		//2.1 Получить номер последней страницы пейджера
		List<Element> result = Main.queryXPathList(PAGER_LAST_PAGE_LINK, dom.getRootElement());
		assert (result.size() == 1);
		int lastPage = Integer.decode(result.get(0).getText()); 
		
		//2.2 Создать задания загрузки всех страниц пейджера
		for (int i=1; i<=lastPage; i++) {
			GKHPagerPage page = new GKHPagerPage(
					url.toExternalForm()+"&page="+String.valueOf(i),
					this);
			submit(page);
		}
	}
	
	/**
	 * Собрать результат после завершения работы в параллельном треде. Вызывается "когда-то" после завершения работы. 
	 * @param finishedPair
	 */
	protected
	void proccessFinished(JobFuturePair finishedPair) {
		// Если finishedPair - объект загрузки дома, то присоединить дом к результатам.
		if (finishedPair.job instanceof GKHBuildingPage) 
			results.add(((GKHBuildingPage)finishedPair.job).building);
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
		
		if (Main.referenceStorage != null) {
			LinkedList<String> subm = new LinkedList<>();
			subm.add(job.url.toExternalForm());
			Collection<String> res = Main.referenceStorage.checkBuildingsExist(subm, Main.referenceAliveTime);
			if (res.size() < 1) {
				skipped++;
				return;
			}
		}
		submittedPairs.add(new JobFuturePair(job, executor.submit(job)));
		totalSubmitted++;
	}
	
	/**
	 * Функция сообщения об окончании загрузки одной страницы.
	 * Вызывается из параллельного треда СРАЗУ после завершения загрузки (перед окончанием работы вызывающего треда).
	 */
	public synchronized 
	void report_finished() {
		pageCounter++;
		// check if needs updating
		long elapsed = (System.nanoTime() - startingTime)/1000_000_000;
		if (pageCounter/updateGranularity != updateGranula) {
			updateGranula = pageCounter/updateGranularity;
			System.out.println("Downloaded: "+pageCounter+", total: "+totalSubmitted+", skipped: "+skipped+" pages. ["+elapsed+" seconds]");
		}
	}
		
}
