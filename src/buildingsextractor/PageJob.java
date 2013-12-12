package buildingsextractor;

/**
 *  ласс представл€ет собой абстрактную задачу по обработке страницы, сто€щую в очереди задач.
 * «апускаетс€ управл€ющим классом (jobMaster) в отдельном треде.
 * ¬ызов метода "jobMaster.submit(...)" можно использовать дл€ добавлени€ новых задач в очередь
 * управл€ющего класса.
 * 
 * This class is thread-safe. 
 */
public class PageJob extends PageDownloader {
	/**
	 *  ”правл€ющий объект.
	 */
	final protected Crawler jobMaster;
	
	/**
	 * @param owner - owning Crawler, through which reports go and new jobs are submitted.
	 */
	public
	PageJob (String stringURL, Crawler owner) {
		super(stringURL);
//		assert (owner != null);
		jobMaster = owner;
	}
}
