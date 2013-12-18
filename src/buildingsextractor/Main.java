package buildingsextractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Главный класс, будет запускать всю работу.
 */
public class Main {
	static final String chuvashia = "http://www.reformagkh.ru/myhouse?tid=2358768";

	public static void main(String[] args) {
		LinkedList<String> chuvashian_places = new LinkedList<>();
		LinkedList<Building> results = new LinkedList<>();
				
		System.out.println("Downloading Chuvashia towns from "+chuvashia);
		RespublicCrawler rc = new RespublicCrawler(chuvashia, chuvashian_places, 10);
		rc.run();
		
		System.out.println();
		System.out.println("Downloaded total of "+chuvashian_places.size()+" places.");
		int counter = 1;
		for (String place: chuvashian_places) {
			System.out.println("Downloading data from town #"+counter+" ("+place+").");
			Crawler crawler = new Crawler(place, results, 10);
			crawler.run();
			System.out.println();
			counter++;
		}
		
		System.out.println("Parsing data to XML file...");
		XMLStorage s;
		try {
			s = new XMLStorage("Buildings_Chuvashia.xml", true);
			s.saveBuildings(results);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Queries given JDOM document with XPath string
	 * @param query - XPath string with all nodes with "html" namespace prefixes for parsed HTML files 
	 * @param doc - JDOM Document or Element
	 * @return List of found matches, may be of zero size if nothing is found
	 */
	public final static 
	List<Element> queryXPathList(String query, Element doc) {
			if (query == null) return new ArrayList<Element>(0);
			try {
				String nsURI = doc.getNamespaceURI();
				XPathBuilder<Element> xpb = new XPathBuilder<Element>(query,Filters.element()); // null filter
				// binding prefix to existing namespace as per XML standard requirement
				xpb.setNamespace("html", nsURI);
				XPathExpression<Element> xpe = xpb.compileWith(XPathFactory.instance()); // default factory
				return xpe.evaluate(doc);
			} catch (NullPointerException|IllegalStateException|IllegalArgumentException  e) {
				return new ArrayList<Element>(0);
			} 		
	}
	
	
	/**
	 * Queries given JDOM document with XPath string
	 * @param query - XPath string with all nodes with "html" namespace prefixes for parsed HTML files 
	 * @param doc - JDOM Document or Element
	 * @param vars - XPath variables
	 * @return List of found matches, may be of zero size if nothing is found
	 */
	public final static 
	List<Element> queryXPathList(String query, Element doc, Properties vars) {
			if (query == null) return new ArrayList<Element>(0);
			try {
				String nsURI = doc.getNamespaceURI();
				XPathBuilder<Element> xpb = new XPathBuilder<Element>(query,Filters.element()); // null filter
				// binding prefix to existing namespace as per XML standard requirement
				xpb.setNamespace("html", nsURI);
				for (Entry<Object, Object> entry: vars.entrySet()) {
					String name = (String)entry.getKey();
					xpb.setVariable(name, entry.getValue());
				}
				XPathExpression<Element> xpe = xpb.compileWith(XPathFactory.instance()); // default factory
				return xpe.evaluate(doc);
			} catch (NullPointerException|IllegalStateException|IllegalArgumentException  e) {
				return new ArrayList<Element>(0);
			} 		
	}	

	/**
	 * Convert from web text to Java String 
	 * @param encoded - String that was retuned by JDOM, encoded in UTF-8
	 * @return decoded String 
	 */
	public final static
	String UTF8_decode(String encoded) {
		byte[] bArray = encoded.getBytes();
		return new String(bArray, StandardCharsets.UTF_8);
	}

	/**
	 * Convert from Java String to web text  
	 * @param source - Java String
	 * @return String encoded in UTF-8 
	 */
	public final static
	String UTF8_encode(String source) {
		byte[] bArray = source.getBytes(StandardCharsets.UTF_8);
		return new String(bArray);
	}
}
