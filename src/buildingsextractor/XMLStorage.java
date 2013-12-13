package buildingsextractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map.Entry;

import org.jdom2.Element;

/**
 * Класс для обработки множества объектов Building и сохранения их в файл. 
 *
 */
public class XMLStorage {
	XMLCache cache;

	/**
	 * Конструктор
	 * @param xmlFileName - имя файла XML
	 * @param rewrite - надо ли стереть старый файл. Если false, то данные в файле будут только обновлены (будут загружены и сохранены записи Building, 
	 * не переданные явно при вызове saveBuildings() )
	 * @throws IOException if can't delete the file on rewrite==true
	 */
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
}
