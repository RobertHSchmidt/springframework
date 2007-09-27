package org.springframework.faces.webflow;

import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jboss.el.ExpressionFactoryImpl;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.faces.el.FlowELExpressionParser;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;

public class JsfViewFactoryTests extends TestCase {

	private static final String VIEW_ID = "testView.xhtml";

	private ViewFactory factory;

	private JSFMockHelper jsfMock = new JSFMockHelper();

	private RequestContext context = (RequestContext) EasyMock.createMock(RequestContext.class);

	private ViewHandler viewHandler = new MockViewHandler();

	private TestLifecycle lifecycle;

	private PhaseListener trackingListener;

	private ExpressionParser parser = new FlowELExpressionParser(new ExpressionFactoryImpl());

	protected void setUp() throws Exception {
		configureJsf();
	}

	private void configureJsf() throws Exception {
		jsfMock.setUp();
		trackingListener = new TrackingPhaseListener();
		jsfMock.lifecycle().addPhaseListener(trackingListener);
		jsfMock.facesContext().setViewRoot(null);
		jsfMock.application().setViewHandler(viewHandler);
		lifecycle = new TestLifecycle(jsfMock.lifecycle());
		factory = new JsfViewFactory(lifecycle, parser.parseExpression(VIEW_ID));
	}

	/**
	 * View has not yet been created
	 */
	public final void testGetView_Create() {

		UIViewRoot newRoot = new UIViewRoot();
		newRoot.setViewId(VIEW_ID);
		((MockViewHandler) viewHandler).setCreateView(newRoot);

		View newView = factory.getView(context);

		assertNotNull("A View was not created", newView);
		assertTrue("A JsfView was expected", newView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) newView).getViewRoot().getViewId());
		assertFalse("An unexpected event was signaled,", newView.eventSignaled());
		assertFalse("The lifecycle should not have been invoked", lifecycle.executed);
	}

	/**
	 * View already exists in flash scope and must be restored and the lifecycle executed, no event signaled
	 */
	public final void testGetView_Restore_NoEvent() {

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId(VIEW_ID);
		((MockViewHandler) viewHandler).setRestoreView(existingRoot);

		View restoredView = factory.getView(context);

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) restoredView).getViewRoot().getViewId());
		assertFalse("An unexpected event was signaled,", restoredView.eventSignaled());
		assertTrue("The lifecycle should have been invoked", lifecycle.executed);
	}

	/**
	 * View already exists in flowscope and must be restored and the lifecycle executed, an event is signaled
	 */
	public final void testGetView_Restore_EventSignaled() {

		String event = "foo";

		jsfMock.externalContext().getRequestMap().put(JsfView.EVENT_KEY, event);

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId(VIEW_ID);
		((MockViewHandler) viewHandler).setRestoreView(existingRoot);

		View restoredView = factory.getView(context);

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) restoredView).getViewRoot().getViewId());
		assertTrue("No event was signaled,", restoredView.eventSignaled());
		assertEquals("Event should be " + event, event, restoredView.getEvent().getId());
		assertTrue("The lifecycle should have been invoked", lifecycle.executed);
	}

	/**
	 * View is restored, and then the same view-state is re-entered at the end of the request
	 * @throws Exception
	 */
	public final void testGetView_RestoreTwice() throws Exception {

		String event = "foo";

		jsfMock.externalContext().getRequestMap().put(JsfView.EVENT_KEY, event);

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId(VIEW_ID);
		((MockViewHandler) viewHandler).setRestoreView(existingRoot);

		View restoredView = factory.getView(context);

		assertNull("FacesContext was not released", FacesContext.getCurrentInstance());

		configureJsf();

		View recursiveView = factory.getView(context);

		assertNotNull("A View was not restored", restoredView);
		assertTrue("A JsfView was expected", restoredView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) restoredView).getViewRoot().getViewId());
		assertSame("Re-entered view should be the same instance", ((JsfView) restoredView).getViewRoot(),
				((JsfView) recursiveView).getViewRoot());
		assertTrue("No event was signaled,", restoredView.eventSignaled());
		assertEquals("Event should be " + event, event, restoredView.getEvent().getId());
		assertTrue("The lifecycle should have been invoked", lifecycle.executed);
	}

	/**
	 * Third party sets the view root before RESTORE_VIEW
	 */
	public final void testGetView_ExternalViewRoot() {

		UIViewRoot newRoot = new UIViewRoot();
		newRoot.setViewId(VIEW_ID);
		jsfMock.facesContext().setViewRoot(newRoot);
		jsfMock.facesContext().renderResponse();

		View newView = factory.getView(context);

		assertNotNull("A View was not created", newView);
		assertTrue("A JsfView was expected", newView instanceof JsfView);
		assertEquals("View name did not match", VIEW_ID, ((JsfView) newView).getViewRoot().getViewId());
		assertSame("View root was not the third party instance", newRoot, ((JsfView) newView).getViewRoot());
		assertFalse("An unexpected event was signaled,", newView.eventSignaled());
		assertFalse("The lifecycle should not have been invoked", lifecycle.executed);
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

	private class TrackingPhaseListener implements PhaseListener {

		private List phaseCallbacks = new ArrayList();

		public void afterPhase(PhaseEvent event) {
			String phaseCallback = "AFTER_" + event.getPhaseId();
			assertFalse("Phase callback " + phaseCallback + " already executed.", phaseCallbacks
					.contains(phaseCallback));
			phaseCallbacks.add(phaseCallback);
		}

		public void beforePhase(PhaseEvent event) {
			String phaseCallback = "BEFORE_" + event.getPhaseId();
			assertFalse("Phase callback " + phaseCallback + " already executed.", phaseCallbacks
					.contains(phaseCallback));
			phaseCallbacks.add(phaseCallback);
		}

		public PhaseId getPhaseId() {
			return PhaseId.ANY_PHASE;
		}

		public List getPhaseCallbacks() {
			return phaseCallbacks;
		}

	}

}
