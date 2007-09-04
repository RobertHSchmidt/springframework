package org.springframework.faces.webflow;

import javax.faces.application.ViewHandler;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;

public class JsfViewFactoryTests extends TestCase {

	private static final String VIEW_ID = "testView.xhtml";

	private ViewFactory factory;

	private JSFMockHelper jsfMock = new JSFMockHelper();

	private RequestContext context = (RequestContext) EasyMock.createMock(RequestContext.class);

	private ViewHandler viewHandler = new MockViewHandler();

	protected void setUp() throws Exception {
		jsfMock.setUp();
		jsfMock.application().setViewHandler(viewHandler);
		factory = new JsfViewFactory(jsfMock.facesContextFactory(), new FlowLifecycle(jsfMock.lifecycle()), VIEW_ID);
	}

	public final void testCreateView() {

		View newView = factory.createView(context);

		assertNotNull("A View was not created", newView);
		assertTrue("A JsfView was expected", newView instanceof JsfView);
		assertFalse("An unexpected event was signaled,", newView.eventSignaled());
	}

	public final void testRestoreView_NoEvent() {

		View restoredView = factory.restoreView(context);

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertFalse("An unexpected event was signaled,", restoredView.eventSignaled());
	}

	public final void testRestoreView_EventSignaled() {

		String event = "foo";

		jsfMock.externalContext().getRequestMap().put(JsfView.EVENT_KEY, event);

		View restoredView = factory.restoreView(context);

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertTrue("No event was signaled,", restoredView.eventSignaled());
		assertEquals("Event should be " + event, event, restoredView.getEvent().getId());
	}
}
