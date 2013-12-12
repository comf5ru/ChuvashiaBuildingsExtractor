package buildingsextractor;

import org.jdom2.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Основная рабочая единица проекта - сущность "Здание".
 * Содержит промежуточную и результирующую информацию. 
 *
 */
public class Building {
	
	/**
	 * Адрес страницы, с которой была получена информация об этом здании
	 */
	public final String pageURL;
	
	/**
	 * Содержимое страницы с информацией об этом здании
	 */
	public final Document dom;
	
	/**
	 * Дата получения информации о здании со страницы.
	 */
	public final String pageDownloadDate;
	
	
	/**
	 * Конструктор для создания объекта из скачанных данных
	 * @param url - адрес страницы с информацией об этом здании
	 * @param dom - содержимое страницы с информацией об этом здании
	 */
	public Building(String url, Document dom) {
		pageURL = url;
		this.dom = dom;
		DateTime dt = new DateTime();
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		pageDownloadDate = fmt.print(dt);
	}
	
	
}
