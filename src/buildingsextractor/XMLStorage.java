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
 * ����� ��� ��������� ��������� �������� Building � ���������� �� � ����. 
 *
 */
public class XMLStorage {
	public
	XMLCache cache;

	/**
	 * �����������
	 * @param xmlFileName - ��� ����� XML
	 * @param rewrite - ���� �� ������� ������ ����. ���� false, �� ������ � ����� ����� ������ ��������� (����� ��������� � ��������� ������ Building, 
	 * �� ���������� ���� ��� ������ saveBuildings() )
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
	 * ��������� ������ ����� � XML ����. ���� ���������� ��������, �� ����� ������ ����� ���� �� �������� �� 
	 * ���������� ������� (� �� �������, ����������� �� ���������� �����, ���� XMLStorage ������ ��� ����� rewrite).
	 * ���� � ���� ��� ���� ������, �� ����� �������� ������ �� ������ ������ ��� ���������� ���� url. 
	 * @param buildings - ��������� ����� ������ ��� ���������� � ����.
	 * @throws IOException - �������� �������� ��� ���������� �����.
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
	 * ���������, ���������� �� ������ �� ������ � ���� (�� �� url). ����������� ���������, ��� ����� ����
	 * ����������� ������.
	 * @param buildings - ������ url ������� ������.
	 * @param aliveTime - �������� ������� (� �������������), ����� ������ ��������� ���������. �������� -1 �������� "������������ ����" 
	 * - �������� ����� �������������� ������ �� url.
	 * @return ���������� ������ ������, ������� ��� � ����, ��� ������ � ������� ��������.
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
				DateTime dt = new DateTime(); // ������
				
				if (buildingDate + aliveTime < dt.getMillis())
					urlsToDownload.add(buildingURL);
			}
		}
		return urlsToDownload;
	}
}
