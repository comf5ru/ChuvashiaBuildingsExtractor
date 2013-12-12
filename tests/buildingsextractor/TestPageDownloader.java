package buildingsextractor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPageDownloader {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorInvalidURL() {
		PageDownloader pd = new PageDownloader("345354://35");
		fail("PageDownloader did not throw exception for bad URL");
	}

	@SuppressWarnings("unused")
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorNullURL() {
		PageDownloader pd = new PageDownloader(null);
		fail("PageDownloader did not throw exception for null URL");
	}
	
	@Test
	public void testConstructor() {
		PageDownloader pd = new PageDownloader("http://google.com");
		assertNotNull("PageDownloader was not created", pd);
	}
	
	@Test
	public void testGoogleDownload() throws Exception {
		PageDownloader pd = new PageDownloader("http://google.com");
		assertNotNull("PageDownloader was not created", pd);
		pd.run();
		assertNotNull("Null DOM produced from page downloaded", pd.dom);
	}
}
