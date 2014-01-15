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
 * ����������� ����� ��� �������� � ������� � ����� �������� ����������. 
 *
 */
public class KPGTaxonomer {

	// ����������� ���� � �������� ����������
	private	Path file;
	
	// ������ ����������
	static
	public class KPGTerm {
		public int id;
		public String name;
		public int parentId; // �� ��������, ���� ���� (����� 0). �������� �� ��������� � �������� "parent".
		public KPGTerm parent;
		
		KPGTerm(int id, String name, int pid) {this.id = id; this.name = name; this.parentId = pid;}
	}
	
	public HashMap<Integer, KPGTerm> terms; // ����� �������� {id, KPGTerm}
	
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
				// �������� �� ������!
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
	 * �������� ������������ ��������, �������� ������� ��������
	 * @param pid - id �������� ��� �������
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
	 * ����� ������� �� ����� � �����������
	 * @param subset - ������������ ��� ������. �������� null - ������ � terms (��� ������� ����������)
	 * @param name - ���, ������� ������
	 * @return 0 - �� �������, ����� id �������.
	 */
	public int findByName(HashMap<Integer, KPGTerm> subset, String name) {
		for (KPGTerm t: subset.values()) 
			if (t.name.equals(name))
				return t.id;
		return 0;
	}
}
