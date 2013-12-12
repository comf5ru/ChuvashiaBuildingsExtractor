package buildingsextractor;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Главный класс, будет запускать всю работу.
 */
public class Main {

	public static void main(String[] args) {
		GKHBuildingPage bp = new GKHBuildingPage("http://www.reformagkh.ru/myhouse/view/7117774/?group=0", null);
		bp.run();
		
		assert (bp.building != null) ;
		assert (bp.building.dom != null);
		
		bp.building.parse_data();
		
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
