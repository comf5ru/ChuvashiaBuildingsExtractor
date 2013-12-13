package buildingsextractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map.Entry;

import org.jdom2.Element;

/**
 * ����� ��� ��������� ��������� �������� Building � ���������� �� � ����. 
 *
 */
public class XMLStorage {
	XMLCache cache;

	/**
	 * �����������
	 * @param xmlFileName - ��� ����� XML
	 * @param rewrite - ���� �� ������� ������ ����. ���� false, �� ������ � ����� ����� ������ ��������� (����� ��������� � ��������� ������ Building, 
	 * �� ���������� ���� ��� ������ saveBuildings() )
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
	 * ��������� ������ ����� � XML ����. ���� ���������� ��������, �� ����� ������ ����� ���� �� �������� �� 
	 * ���������� ������� (� �� �������, ����������� �� ���������� �����, ���� XMLStorage ������ ��� ����� rewrite).
	 * ���� � ���� ��� ���� ������, �� ����� �������� ������ �� ������ ������ ��� ���������� ���� url. 
	 * @param buildings - ��������� ����� ������ ��� ���������� � ����.
	 * @throws IOException - �������� �������� ��� ���������� �����.
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
