package buildingsextractor;

import java.util.Properties;

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
	 * Этот член класса содержит все результирующие данные со страницы как набор свойств (ключ-значение):
	 * улица, номер дома, номер корпуса, жилая площадь, число подъездов и т.п. 
	 * Если свойство отсутствует, то его значение считается "неизвестно".
	 */
	public Properties data; // thread-safe class
	
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
