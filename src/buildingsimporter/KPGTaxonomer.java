package buildingsimporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KPGTaxonomer {

	private	Path file;
	
	static
	public class KPGTerm {
		public int id;
		public String name;
		public int parentId;
		public KPGTerm parent;
		
		KPGTerm(int id, String name, int pid) {this.id = id; this.name = name; this.parentId = pid;}

	}
	
	public HashMap<Integer, KPGTerm> terms;
	
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
		
		for (KPGTerm t: terms.values()) 
			t.parent = terms.get(t.parentId);
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
