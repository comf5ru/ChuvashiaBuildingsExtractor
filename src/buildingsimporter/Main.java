package buildingsimporter;

/**
 * ������� ����� ��� ������� �������������� XML -> mysql
 * (��������� ����� ��� ������� � Drupal �� ����� � �������) 
 *
 */
public class Main {

	public static XMLCacheFixer cache;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		cache = new XMLCacheFixer("Buildings_Chuvashia.xml");
		cache.kill_duplicates();
	}

}
