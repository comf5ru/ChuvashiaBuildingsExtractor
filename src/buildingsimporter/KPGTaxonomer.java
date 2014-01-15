package buildingsimporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Интефейсный класс для загрузки и доступа к файлу экспорта таксономии. 
 *
 */
public class KPGTaxonomer {

	// Загружаемый файл с экпортом таксономии
	private	Path file;
	
	// Термин таксономии
	static
	public class KPGTerm {
		public int id;
		public String name;
		public int parentId; // ид родителя, если есть (иначе 0). Первичен по отношению к свойству "parent".
		public KPGTerm parent;
		
		KPGTerm(int id, String name, int pid) {this.id = id; this.name = name; this.parentId = pid;}
	}
	
	public HashMap<Integer, KPGTerm> terms; // набор терминов {id, KPGTerm}
	
	public KPGTaxonomer(String filename) {
		file = Paths.get(filename);
		if (!Files.exists(file))
			throw new InvalidParameterException();
		
		terms = new HashMap<>();
		
		Charset charset = StandardCharsets.UTF_8;
		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
		    String line = null;
	        Pattern termCVSregexp = Pattern.compile("\"2\";\"([^\"]*)\";\"([^\"]*)\";\"\";\"([^\"]*)\"");
		    while ((line = reader.readLine()) != null) {
		    	Matcher m = termCVSregexp.matcher(line);

		    	if (m.matches()) {
		    		Integer id = Integer.decode(m.group(1));
		    		terms.put(id, new KPGTerm(id, m.group(2), Integer.decode(m.group(3))));
		    	}
		    	
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
		Collection <Integer> failedIds = new LinkedList<>();
		for (KPGTerm t: terms.values()) {
			int pid = t.parentId;
			if (pid != 0) {
				t.parent = terms.get(pid);
				// родитель не найден!
				if (t.parent == null) failedIds.add(t.id);
			}
		}
		
		if (failedIds.size() > 0) {
			System.out.println(String.format("Found %d bad terms on loading (parent doesn't exist)", failedIds.size()));
			for (Integer f_id: failedIds)
				terms.remove(f_id);
		}
		System.out.println(String.format("Loaded %d terms", terms.size()));
	}

	/**
	 * Получить подмножество терминов, имееющих данного родителя
	 * @param pid - id родителя для выборки
	 * @return 
	 */
	public HashMap<Integer, KPGTerm> chooseForParent(int pid) {
		HashMap<Integer, KPGTerm> subset = new HashMap<>();
		
		for (KPGTerm t: terms.values()) 
			if (t.parentId == pid) 
				subset.put(t.id, t);
		
		return subset;
	}

	/**
	 * Поиск термина по имени в подможестве
	 * @param subset - подмножество для поиска. Значение null - искать в terms (все термины таксономии)
	 * @param name - имя, которое ищется
	 * @return 0 - не найдено, иначе id термина.
	 */
	public int findByName(HashMap<Integer, KPGTerm> subset, String name) {
		for (KPGTerm t: subset.values()) 
			if (t.name.equals(name))
				return t.id;
		return 0;
	}
}
