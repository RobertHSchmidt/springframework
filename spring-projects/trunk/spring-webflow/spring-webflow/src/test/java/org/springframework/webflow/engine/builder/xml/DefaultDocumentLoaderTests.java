package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

public class DefaultDocumentLoaderTests extends TestCase {
	private DefaultDocumentLoader loader = new DefaultDocumentLoader();
	
	public void testLoad() throws Exception {
		Resource resource = new ClassPathResource("testFlow1.xml", getClass());
		Document document = loader.loadDocument(resource);
		assertNotNull(document);
	}
}
