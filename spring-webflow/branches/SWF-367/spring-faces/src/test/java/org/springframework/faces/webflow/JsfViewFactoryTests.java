package org.springframework.faces.webflow;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;

public class JsfViewFactoryTests extends TestCase {

	private static final String VIEW_ID = "testView.xhtml";

	private ViewFactory factory;

	private JSFMockHelper jsfMock = new JSFMockHelper();

	private RequestContext context = (RequestContext) EasyMock.createMock(RequestContext.class);

	private MutableAttributeMap flowScope = (MutableAttributeMap) EasyMock.createMock(MutableAttributeMap.class);

	private ViewHandler viewHandler = new MockViewHandler();

	private TestLifecycle lifecycle;

	protected void setUp() throws Exception {
		jsfMock.setUp();
		jsfMock.facesContext().setViewRoot(null);
		jsfMock.application().setViewHandler(viewHandler);
		lifecycle = new TestLifecycle(jsfMock.lifecycle());
		factory = new JsfViewFactory(jsfMock.facesContextFactory(), lifecycle, VIEW_ID);
	}

	/**
	 * Scenario 1 - view has not yet been created
	 */
	public final void testGetView_Create() {

		UIViewRoot newRoot = new UIViewRoot();
		newRoot.setViewId(VIEW_ID);
		((MockViewHandler) viewHandler).setCreateView(newRoot);

		EasyMock.expect(context.getFlowScope()).andStubReturn(flowScope);
		EasyMock.expect(flowScope.get(JsfView.STATE_KEY)).andReturn(null);
		EasyMock.expect(flowScope.put(EasyMock.matches(JsfView.STATE_KEY), EasyMock.notNull())).andReturn(null);
		EasyMock.replay(new Object[] { context, flowScope });

		View newView = factory.getView(context);

		EasyMock.verify(new Object[] { context, flowScope });
		assertNotNull("A View was not created", newView);
		assertTrue("A JsfView was expected", newView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) newView).getViewRoot().getViewId());
		assertFalse("An unexpected event was signaled,", newView.eventSignaled());
		assertFalse("The lifecycle should not have been invoked", lifecycle.executed);
	}

	/**
	 * Scenario 2 - view already exists in flowscope and must be restored and the lifecycle executed, no event signaled
	 */
	public final void testGetView_Restore_NoEvent() {

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId(VIEW_ID);
		View existingView = new JsfView(existingRoot);

		EasyMock.expect(context.getFlowScope()).andStubReturn(flowScope);
		EasyMock.expect(flowScope.get(JsfView.STATE_KEY)).andStubReturn(existingView);
		EasyMock.expect(flowScope.put(EasyMock.matches(JsfView.STATE_KEY), EasyMock.notNull())).andReturn(existingView);
		EasyMock.replay(new Object[] { context, flowScope });

		View restoredView = factory.getView(context);

		EasyMock.verify(new Object[] { context, flowScope });

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) restoredView).getViewRoot().getViewId());
		assertFalse("An unexpected event was signaled,", restoredView.eventSignaled());
		assertTrue("The lifecycle should have been invoked", lifecycle.executed);
	}

	/**
	 * Scenario 3 - view already exists in flowscope and must be restored and the lifecycle executed, an event is
	 * signaled
	 */
	public final void testGetView_Restore_EventSignaled() {

		String event = "foo";

		jsfMock.externalContext().getRequestMap().put(JsfView.EVENT_KEY, event);

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId(VIEW_ID);
		View existingView = new JsfView(existingRoot);

		EasyMock.expect(context.getFlowScope()).andStubReturn(flowScope);
		EasyMock.expect(flowScope.get(JsfView.STATE_KEY)).andStubReturn(existingView);
		EasyMock.expect(flowScope.put(EasyMock.matches(JsfView.STATE_KEY), EasyMock.notNull())).andReturn(existingView);
		EasyMock.replay(new Object[] { context, flowScope });

		View restoredView = factory.getView(context);

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) restoredView).getViewRoot().getViewId());
		assertTrue("No event was signaled,", restoredView.eventSignaled());
		assertEquals("Event should be " + event, event, restoredView.getEvent().getId());
		assertTrue("The lifecycle should have been invoked", lifecycle.executed);
	}

	/**
	 * Scenario 4 - view is restored, and then the same view-state is re-entered at the end of the request
	 */
	public final void testGetView_RestoreTwice() {

		String event = "foo";

		jsfMock.externalContext().getRequestMap().put(JsfView.EVENT_KEY, event);

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId(VIEW_ID);
		View existingView = new JsfView(existingRoot);

		EasyMock.expect(context.getFlowScope()).andStubReturn(flowScope);
		EasyMock.expect(flowScope.get(JsfView.STATE_KEY)).andStubReturn(existingView);
		EasyMock.expect(flowScope.put(EasyMock.matches(JsfView.STATE_KEY), EasyMock.notNull())).andStubReturn(
				existingView);
		EasyMock.replay(new Object[] { context, flowScope });

		View restoredView = factory.getView(context);

		View recursiveView = factory.getView(context);

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) restoredView).getViewRoot().getViewId());
		assertSame("Re-entered view should be the same instance", restoredView, recursiveView);
		assertTrue("No event was signaled,", restoredView.eventSignaled());
		assertEquals("Event should be " + event, event, restoredView.getEvent().getId());
		assertTrue("The lifecycle should have been invoked", lifecycle.executed);
	}

	private class TestLifecycle extends FlowLifecycle {

		boolean executed = false;

		public TestLifecycle(Lifecycle delegate) {
			super(delegate);
		}

		public void execute(FacesContext context) throws FacesException {
			assertFalse("Lifecycle executed more than once", executed);
			super.execute(context);
			executed = true;
		}

	}

}
