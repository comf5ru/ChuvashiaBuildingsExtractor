package buildingsextractor;

import org.jdom2.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * �������� ������� ������� ������� - �������� "������".
 * �������� ������������� � �������������� ����������. 
 *
 */
public class Building {
	
	/**
	 * ����� ��������, � ������� ���� �������� ���������� �� ���� ������
	 */
	public final String pageURL;
	
	/**
	 * ���������� �������� � ����������� �� ���� ������
	 */
	public final Document dom;
	
	/**
	 * ���� ��������� ���������� � ������ �� ��������.
	 */
	public final String pageDownloadDate;
	
	
	/**
	 * ����������� ��� �������� ������� �� ��������� ������
	 * @param url - ����� �������� � ����������� �� ���� ������
	 * @param dom - ���������� �������� � ����������� �� ���� ������
	 */
	public Building(String url, Document dom) {
		pageURL = url;
		this.dom = dom;
		DateTime dt = new DateTime();
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		pageDownloadDate = fmt.print(dt);
	}
	
	
}
