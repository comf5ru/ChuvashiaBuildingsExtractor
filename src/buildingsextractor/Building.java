package buildingsextractor;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	/**
	 * XPath ��������� ������ ����
	 */
	static final String TITLE_SPAN = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' printViewAnketa ')]"
			+ "//html:h1/html:span";
	
	/**
	 * XPath ��������� TD �� ��������� �� ����� ���� (� ����������)
	 */
	static final String VAR_RECORD_TD = 
			"//html:div[contains(concat(' ', normalize-space(@id), ' '), ' form_block_1 ')]"
			+ "//html:table[contains(concat(' ', normalize-space(@class), ' '), ' mkd-table ')]"
			+ "//html:span[contains(concat(' ', normalize-space(@class), ' '), ' b-tabulation_text ') and text()=$textval]"
			+ "//ancestor::html:tr"
			+ "//html:td[contains(concat(' ', normalize-space(@class), ' '), ' b-td_value-def ')]"
			;
	
	/**
	 * ����� ����� ��� ����������� � XPath ��� ������ � ���������
	 */
	static final String[] parsedLines = {
		"��� ����� � ������������",
		"���������",
		"���������� ���������",
		"���������� ������",
		"�������� ����",
		"���������� �������"
	};
	
	/**
	 * �������� ������ �� ����������� ��������: dom -> data
	 */
	public void parse_data () {
		List<Element> result;
		Properties var = new Properties();

		for (String lineText: parsedLines) {
			var.setProperty("textval", Main.UTF8_encode(lineText));
			result = Main.queryXPathList(VAR_RECORD_TD, dom.getRootElement(), var);
			if (result.size()>0)
				data.setProperty(lineText, Main.UTF8_decode(result.get(0).getText()));
		}
		
		result = Main.queryXPathList(TITLE_SPAN, dom.getRootElement());
		if (result.size() == 0)
			return;
		String fullAddress = Main.UTF8_decode(result.get(0).getText());
		Pattern regexp = Pattern.compile("^(\\S \\S+)\\s+(.+)\\s�\\.(.*)$");
		Matcher m = regexp.matcher(fullAddress);
		if (m.matches()) {
			data.setProperty("��������� �����", m.group(1)); // �������� ���������� "� ", "� ", "� "...
			data.setProperty("�����", m.group(2)); // �������� ���������� ���� "��.", "���.", "���." � �.�.
			data.setProperty("����� ����", m.group(3)); // ����� �������� ������, ������ � �.�.
		}
	}
}
