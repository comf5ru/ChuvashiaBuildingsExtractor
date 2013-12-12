package buildingsextractor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.jdom2.xpath.XPathBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Объекты этого класса скачивают и хранят одиночные страницы 
 */
public class PageDownloader  implements Runnable {
	/**
	 * URL страницы для скачивания
	 */
	public final URL url;
	
	/**
	 * полученный в результате скачивания DOM. Также возвращается по call().
	 */
	public Document dom;
	
	/**
	 * @param stringURL - URL страницы для скачивания.
	 */
	public PageDownloader(String stringURL) {
		try {url = resolveLink(stringURL);}
		catch (MalformedURLException e) {throw new IllegalArgumentException(e);}
		catch (NullPointerException e) {throw new IllegalArgumentException(e);}		
	}
	
	/**
	 * Gets an URL to resource referenced from this page. 
	 * Uses this.url as a base link to resolve relative paths. 
	 * @param link - relative or absolute link
	 * @return proper URL with absolute path
	 * @throws MalformedURLException 
	 */
	protected URL resolveLink(String link) throws MalformedURLException {
		return new URL(url, fixURLString(url, link));
	}

	public final static 
	String fixURLString(URL base, String u) {
		if (u == null) return null;
		if (u.endsWith("/"))
			u = u.substring(0, u.length()-1); // uniform "...com/" to "...com" address
		if (base == null && !u.contains(":/"))
			u = "http://"+u; // default protocol
		return u.toLowerCase();
	}
	

	@Override
	public void run() {
		try {
			dom = downloadPage();
		} catch (InterruptedException | JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** 
	 * Downloads the page
	 * @throws ProblemsReadingDocumentException if any error
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	private final 
	Document downloadPage() throws InterruptedException, JDOMException, IOException {
//		Main.log(Level.FINE, String.format("Downloading %s from network...%n", url.toString()));
		
		Document doc = null;
		try {
			XMLReaderSAX2Factory saxConverter = new XMLReaderSAX2Factory(false, "org.ccil.cowan.tagsoup.Parser");
			SAXBuilder builder = new SAXBuilder(saxConverter);
			URLConnection connection = url.openConnection();
			
			if (!checkHttpResponseOK(connection))		
				throw new ConnectException("Error response from server");
			if (Thread.interrupted())
				throw new InterruptedException();
			doc = builder.build(connection.getInputStream());
		} catch (InterruptedException e) {
			throw e;
		} 
		
//		Main.log(Level.FINE, String.format("...finished %s.%n", url.toString()));
		return doc;
	}
	
	/**
	 * Check whether resource is accessible.
	 * @param connection
	 * @return true if everything is OK. False otherwise.
	 */
	public static boolean checkHttpResponseOK (URLConnection connection) {
		if (connection instanceof HttpURLConnection)
			try {
				return ((HttpURLConnection)connection).getResponseCode() == HttpURLConnection.HTTP_OK;
			} catch (IOException e) {
				return false;
			}
		
		// exceptional treatment of local files, for testing convenience
		URL u = connection.getURL();
		if (u.getProtocol().equals("file"))
			try {
				return Files.isReadable(Paths.get(java.net.URLDecoder.decode(u.getFile().substring(1), "UTF-8")));
			} catch (UnsupportedEncodingException e) {
				return false;
			}
		return false; // unknown connection type
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
//				Main.logger.log(Level.SEVERE,"",e);
				return new ArrayList<Element>(0);
			} 		
	}	
}
