package org.springframework.webflow.executor.jsf;

import junit.framework.TestCase;

import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractionException;

public class FlowNavigationHandlerParameterExtractorTests extends TestCase {
	private FlowNavigationHandlerArgumentExtractor extractor = new FlowNavigationHandlerArgumentExtractor();

	public void testExtractFlowId() {
		JsfExternalContext context = new JsfExternalContext(new MockFacesContext(), "action", "flowId:foo");
		String flowId = extractor.extractFlowId(context);
		assertEquals("Wrong flow id", "foo", flowId);
	}

	public void testExtractFlowIdWrongFormat() {
		JsfExternalContext context = new JsfExternalContext(new MockFacesContext(), "action", "flow:foo");
		try {
			String flowId = extractor.extractFlowId(context);
		} catch (FlowExecutorArgumentExtractionException e) {
			
		}
	}

	public void testExtractEventId() {
		JsfExternalContext context = new JsfExternalContext(new MockFacesContext(), "action", "submit");
		EventId eventId = extractor.extractEventId(context);
		assertEquals("Wrong event id", new EventId("submit"), eventId);
	}
}