package buildingsextractor;

/**
 * ����� ������������ ����� ����������� ������ �� ��������� ��������, ������� � ������� �����.
 * ����������� ����������� ������� (jobMaster) � ��������� �����.
 * ����� ������ "jobMaster.submit(...)" ����� ������������ ��� ���������� ����� ����� � �������
 * ������������ ������.
 * 
 * This class is thread-safe. 
 */
public class PageJob extends PageDownloader {
	/**
	 *  ����������� ������.
	 */
	final protected Crawler jobMaster;
	
	/**
	 * @param owner - owning Crawler, through which reports go and new jobs are submitted.
	 */
	public
	PageJob (String stringURL, Crawler owner) {
		super(stringURL);
//		assert (owner != null);
		jobMaster = owner;
	}
}
