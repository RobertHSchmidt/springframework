package org.springframework.webflow.executor.jsf;

import junit.framework.TestCase;

public class FlowNavigationHandlerParameterExtractorTests extends TestCase {
	private FlowNavigationHandlerArgumentExtractor extractor = new FlowNavigationHandlerArgumentExtractor();

	public void testExtractFlowId() {
		JsfExternalContext context = new JsfExternalContext(new MockFacesContext(), "action", "flowId:foo");
		String flowId = extractor.extractFlowId(context);
		assertEquals("Wrong flow id", "foo", flowId);
	}
	
	public void testExtractFlowIdWrongFormat() {
		JsfExternalContext context = new JsfExternalContext(new MockFacesContext(), "action", "flow:foo");
		String flowId = extractor.extractFlowId(context);
		assertNull(flowId);
	}
	
	public void testExtractEventId() {
		JsfExternalContext context = new JsfExternalContext(new MockFacesContext(), "action", "submit");
		String eventId = extractor.extractEventId(context);
		assertEquals("Wrong event id", "submit", eventId);
	}
	
}
