package buildingsimporter;

import java.io.IOException;
import java.util.Collection;

/**
 * ������� ����� ��� ������� �������������� XML -> mysql
 * (��������� ����� ��� ������� � Drupal �� ����� � �������) 
 *
 */
public class Main {

	public static XMLCacheFixer cache;
	public static void main(String[] args) throws IOException {
		KPGTaxonomer taxonomer = new KPGTaxonomer("kpg_locations_data_ids.txt");
		cache = new XMLCacheFixer("4import_Buildings_Chuvashia.xml");
		
		cache.match_all_buildings(taxonomer);

//		cache.dropEmpty();
//		cache.saveCache();
		
//		cache.kill_duplicates();
//		cache.saveCache();
		
//		Collection<String> areas = cache.getAllAreas();
//		for(String a : areas) {
//			System.out.println(a);
//		}
		
	}

}
