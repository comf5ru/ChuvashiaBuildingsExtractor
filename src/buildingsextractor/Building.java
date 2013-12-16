package buildingsextractor;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;

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
//	public final Document dom;
	
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
	public Building(String url, Document dom, String date) {
		pageURL = url;
//		this.dom = dom;
		this.pageDownloadDate = date; 
		data = new Properties();
		
		parse_data(dom);
//		this.dom = null; // сбросить
	}
	
	/**
	 * XPath получения адреса дома
	 */
	static final String TITLE_SPAN = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' printViewAnketa ')]"
			+ "//html:h1/html:span";
	
	/**
	 * XPath получения TD со значением по имени поля (в переменной)
	 */
	static final String VAR_RECORD_TD = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' form_block_1 ')]"
			+ "//html:table[contains(concat(' ', normalize-space(@class), ' '), ' mkd-table ')]"
			+ "//html:span[contains(concat(' ', normalize-space(@class), ' '), ' b-tabulation_text ') and text()=$textval]"
			+ "//ancestor::html:tr"
			+ "//html:td[contains(concat(' ', normalize-space(@class), ' '), ' b-td_value-def ')]"
			;
	
	/**
	 * Имена полей для подстановки в XPath для поиска на странице дома
	 */
	static final String[] parsedLines = {
		"Год ввода в эксплуатацию",
		"Этажность",
		"Количество подъездов",
		"Количество лифтов",
		"Материал стен",
		"Количество квартир"
	};
	

	/**
	 * Получает данные из загруженной страницы: dom -> data
	 * @param dom 
	 */
	public void parse_data (Document dom) {
		List<Element> result;
		Properties var = new Properties();

		// для каждого искомого свойства в таблице
		for (String lineText: parsedLines) {
			// устанавливаем переменную в XPath
			var.setProperty("textval", Main.UTF8_encode(lineText));
			// запрашиваем XPath
			result = Main.queryXPathList(VAR_RECORD_TD, dom.getRootElement(), var);
			if (result.size()>0)
				// сохраняем полученный результат
				data.setProperty(lineText, Main.UTF8_decode(result.get(0).getText()));
		}
		
		// теперь отдельно парсим адрес дома
		result = Main.queryXPathList(TITLE_SPAN, dom.getRootElement());
		if (result.size() == 0)
			return;
		String fullAddress = Main.UTF8_decode(result.get(0).getText());
		
		// общий вид:
		//  (сокращение_населённого_пункта Название Нас. Пункта)( сокр_улицы_бульвара_итп Название Улицы И Т П)? д.нумерация_дома
		Pattern regexp = Pattern.compile("^(\\S+(?:\\s+[^\\p{javaLowerCase}]\\S*)+)(?:\\s+(.+)|)\\sд\\.(.*)$"); // не получилось использовать (...)? для улицы. Пришлось применять обходной путь (...|)
		Matcher m = regexp.matcher(fullAddress);
		if (m.matches()) {
			data.setProperty("Населённый пункт", m.group(1)); // Включает сокращения "г ", "с ", "п "...
			if (m.group(2)!=null && m.group(2).length()>0)
				data.setProperty("Улица", m.group(2)); // включает сокращения типа "ул ", "пер ", "мкр " и т.п.
			data.setProperty("Номер дома", m.group(3)); // может включать литеры, корпус и т.п.
		}
	}
}
