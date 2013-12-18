package buildingsextractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.jdom2.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Класс для обработки множества объектов Building и сохранения их в файл. 
 *
 */
public class XMLStorage {
	public
	XMLCache cache;

	/**
	 * Конструктор
	 * @param xmlFileName - имя файла XML
	 * @param rewrite - надо ли стереть старый файл. Если false, то данные в файле будут только обновлены (будут загружены и сохранены записи Building, 
	 * не переданные явно при вызове saveBuildings() )
	 * @throws IOException if can't delete the file on rewrite==true
	 */
	public
	XMLStorage(String xmlFileName, boolean rewrite) throws IOException {

		if (rewrite) {
			if (xmlFileName==null || xmlFileName.isEmpty())
				throw new IllegalArgumentException();
			Path file = Paths.get(xmlFileName);
			if (Files.exists(file))
				Files.delete(file);

		}
		cache = new XMLCache(xmlFileName);
	}
	
	/**
	 * Сохранить список домов в XML файл. Если вызывается повторно, то новый список будет слит со списками из 
	 * предыдущих вызовов (и со списком, загруженным из начального файла, если XMLStorage создан без флага rewrite).
	 * Если в кэше уже есть здания, то будут заменены новыми из списка только при совпадении поля url. 
	 * @param buildings - Множество новых зданий для добавления в файл.
	 * @throws IOException - возникли проблемы при сохранении файла.
	 */
	public
	void saveBuildings(Collection<Building> buildings) throws IOException {
		for (Building b: buildings) {
			Element e = new Element("Building");
			e.setAttribute("url", b.pageURL);
			e.setAttribute("downloaded", b.pageDownloadDate);
			
			for (Entry<Object, Object> entry: b.data.entrySet()) {
				Element value = new Element("Dataentry");
				value.setAttribute("name", (String)entry.getKey());
				value.setAttribute("value", (String)entry.getValue());
				e.addContent(value);
			}
			cache.addElementWithReplacement(e);
		}
		cache.saveCache();
	}
	
	/**
	 * Проверяет, существуют ли здания из списка в кэше (по их url). Опционально проверяет, как давно были
	 * кэшированны данные.
	 * @param buildings - список url искомых зданий.
	 * @param aliveTime - интервал времени (в миллисекундах), когда данные считаются валидными. Величина -1 означает "игнорировать дату" 
	 * - проверка будет осуществляться только по url.
	 * @return Возвращает список зданий, которых нет в кэше, или данные о которых устарели.
	 */
	public
	Collection<String> checkBuildingsExist(Collection<String> buildings, long aliveTime) {
		LinkedList<String> urlsToDownload = new LinkedList<>();
		for (String buildingURL: buildings) {
			
			Element el = cache.getElementForPage(buildingURL);
			if (el == null)
				urlsToDownload.add(buildingURL);
			else if (aliveTime>=0) {
				String strDate = el.getAttributeValue("downloaded");
				DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
				long buildingDate = fmt.parseDateTime(strDate).getMillis();
				DateTime dt = new DateTime(); // Сейчас
				
				if (buildingDate + aliveTime < dt.getMillis())
					urlsToDownload.add(buildingURL);
			}
		}
		return urlsToDownload;
	}
}
