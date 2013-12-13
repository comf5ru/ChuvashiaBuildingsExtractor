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
	 * ���� ���� ������ �������� ��� �������������� ������ �� �������� ��� ����� ������� (����-��������):
	 * �����, ����� ����, ����� �������, ����� �������, ����� ��������� � �.�. 
	 * ���� �������� �����������, �� ��� �������� ��������� "����������".
	 */
	public Properties data; // thread-safe class
	
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
		data = new Properties();
	}
	
	private static final String TITLE_SPAN = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' printViewAnketa ')]"
			+ "//html:h1/html:span";
	private static final String YEAR_TD = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' form_block_1 ')]"
			+ "//html:table[contains(concat(' ', normalize-space(@class), ' '), ' mkd-table ')]"
			+ "//html:span[contains(concat(' ', normalize-space(@class), ' '), ' b-tabulation_text ') and text()='"
			+Main.UTF8_encode("��� ����� � ������������")+"']"
			+ "//ancestor::html:tr"
			+ "//html:td[contains(concat(' ', normalize-space(@class), ' '), ' b-td_value-def ')]"
			;
	
	/**
	 * �������� ������ �� ����������� ��������: dom -> data
	 */
	public void parse_data () {
		List<Element> result;
		result = Main.queryXPathList(TITLE_SPAN, dom.getRootElement());
		assert (result.size() == 1);
		data.setProperty("������ �����", Main.UTF8_decode(result.get(0).getText()));
		
		result = Main.queryXPathList(YEAR_TD, dom.getRootElement());
		assert (result.size() == 1);
		
		data.setProperty("��� ����� � ������������", result.get(0).getText());
	}
}
