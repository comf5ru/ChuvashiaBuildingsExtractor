package buildingsextractor;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;

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
//	public final Document dom;
	
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
	public Building(String url, Document dom, String date) {
		pageURL = url;
//		this.dom = dom;
		this.pageDownloadDate = date; 
		data = new Properties();
		
		parse_data(dom);
//		this.dom = null; // ��������
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
	 * ����� ����� ��� ����������� � XPath ��� ������ �� �������� ����
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
	 * @param dom 
	 */
	public void parse_data (Document dom) {
		List<Element> result;
		Properties var = new Properties();

		// ��� ������� �������� �������� � �������
		for (String lineText: parsedLines) {
			// ������������� ���������� � XPath
			var.setProperty("textval", Main.UTF8_encode(lineText));
			// ����������� XPath
			result = Main.queryXPathList(VAR_RECORD_TD, dom.getRootElement(), var);
			if (result.size()>0)
				// ��������� ���������� ���������
				data.setProperty(lineText, Main.UTF8_decode(result.get(0).getText()));
		}
		
		// ������ �������� ������ ����� ����
		result = Main.queryXPathList(TITLE_SPAN, dom.getRootElement());
		if (result.size() == 0)
			return;
		String fullAddress = Main.UTF8_decode(result.get(0).getText());
		
		// ����� ���:
		//  (����������_����������_������ �������� ���. ������)( ����_�����_��������_��� �������� ����� � � �)? �.���������_����
		Pattern regexp = Pattern.compile("^(\\S+(?:\\s+[^\\p{javaLowerCase}]\\S*)+)(?:\\s+(.+)|)\\s�\\.(.*)$"); // �� ���������� ������������ (...)? ��� �����. �������� ��������� �������� ���� (...|)
		Matcher m = regexp.matcher(fullAddress);
		if (m.matches()) {
			data.setProperty("��������� �����", m.group(1)); // �������� ���������� "� ", "� ", "� "...
			if (m.group(2)!=null && m.group(2).length()>0)
				data.setProperty("�����", m.group(2)); // �������� ���������� ���� "�� ", "��� ", "��� " � �.�.
			data.setProperty("����� ����", m.group(3)); // ����� �������� ������, ������ � �.�.
		}
	}
}
