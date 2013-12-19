package buildingsimporter;

/**
 * Главный класс для запуска преобразования XML -> mysql
 * (получение файла для импорта в Drupal из файла с данными) 
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
