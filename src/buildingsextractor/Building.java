package buildingsextractor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.jdom2.Document;
import org.jdom2.Element;
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
		data = new Properties();
	}
	
	private static final String TITLE_SPAN = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' printViewAnketa ')]"
			+ "//html:h1/html:span";
	private static final String YEAR_TD = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' form_block_1 ')]"
			+ "//html:table[contains(concat(' ', normalize-space(@class), ' '), ' mkd-table ')]"
			+ "//html:span[contains(concat(' ', normalize-space(@class), ' '), ' b-tabulation_text ') and text()='"
			+Main.UTF8_encode("Год ввода в эксплуатацию")+"']"
			+ "//ancestor::html:tr"
			+ "//html:td[contains(concat(' ', normalize-space(@class), ' '), ' b-td_value-def ')]"
			;
	
	/**
	 * Получает данные из загруженной страницы: dom -> data
	 */
	public void parse_data () {
		List<Element> result;
		result = Main.queryXPathList(TITLE_SPAN, dom.getRootElement());
		assert (result.size() == 1);
		data.setProperty("Полный адрес", Main.UTF8_decode(result.get(0).getText()));
		
		result = Main.queryXPathList(YEAR_TD, dom.getRootElement());
		assert (result.size() == 1);
		
		data.setProperty("Год ввода в эксплуатацию", result.get(0).getText());
	}
}
