package org.springframework.webflow.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.SimpleFlow;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.repository.ExternalMapFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.LocalMapLocator;
import org.springframework.webflow.execution.repository.NoSuchConversationException;
import org.springframework.webflow.manager.FlowExecutionManagerImpl;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionManagerTests extends TestCase {
	public static class SimpleFlowExecutionListener extends FlowExecutionListenerAdapter {
		private boolean invoked;

		public void requestSubmitted(RequestContext context) {
			invoked = true;
		}
	}

	private FlowExecutionManagerImpl manager = new FlowExecutionManagerImpl(new SimpleFlowLocator());

	private LocalMapLocator mapLocator = new LocalMapLocator();

	public void setUp() {
		ExternalMapFlowExecutionRepositoryFactory repositoryLocator = new ExternalMapFlowExecutionRepositoryFactory();
		repositoryLocator.setExternalMapLocator(mapLocator);
		manager.setRepositoryFactory(repositoryLocator);
	}

	public void testLaunchNewFlow() {
		Map input = new HashMap(1);
		input.put(manager.getFlowIdParameterName(), "simpleFlow");
		ViewSelection view = manager.handleFlowRequest(new MockExternalContext(input));
		assertNotNull(view.getModel().get(FlowExecutionManagerImpl.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertNotNull(view.getModel().get(org.springframework.webflow.manager.CURRENFlowExecutionManagerImpl));
		assertEquals(view.getModel().get(org.springframework.webflow.manager.CURRENFlowExecutionManagerImpl), "view");
		assertNotNull(view.getModel().get(FlowExecutionManagerImpl.FLOW_EXECUTION_CONTEXT_ATTRIBUTE));
		assertEquals("Wrong view name", "view", view.getViewName());
	}

	public void testLaunchNewFlowNoSuchFlowDefinition() {
		Map input = new HashMap(1);
		input.put(manager.getFlowIdParameterName(), "nonexistantFlow");
		try {
			manager.handleFlowRequest(new MockExternalContext(input));
			fail("Should have thrown no such flow exception");
		}
		catch (FlowArtifactException e) {
		}
	}

	public void testParticipateInExistingFlowExecution() {
		Map input = new HashMap(2);
		input.put(manager.getFlowIdParameterName(), "simpleFlow");
		ViewSelection view = manager.handleFlowRequest(new MockExternalContext(input));
		input.put(manager.getFlowExecutionIdParameterName(), view.getModel().get(
				FlowExecutionManagerImpl.FLOW_EXECUTION_ID_ATTRIBUTE));
		input.put(manager.getEventIdParameterName(), "submit");
		view = manager.handleFlowRequest(new MockExternalContext(input));
		assertEquals("Wrong view name", "confirm", view.getViewName());
		assertTrue("Should have been a redirect", view.isRedirect());
		assertNull("Flow has ended", view.getModel().get(FlowExecutionManagerImpl.FLOW_EXECUTION_ID_ATTRIBUTE));
	}

	public void testParticipateInExistingFlowExecutionNoSuchFlowExecution() {
		Map input = new HashMap(2);
		input.put(manager.getFlowIdParameterName(), "simpleFlow");
		manager.handleFlowRequest(new MockExternalContext(input));
		input.put(org.springframework.webflow.manager.FLOW_EXFlowExecutionManagerImpl, "_snot_ccorrect");
		try {
			manager.handleFlowRequest(new MockExternalContext(input));
			fail("should have thrown no such flow execution exception");
		}
		catch (NoSuchConversationException e) {
			// expected
		}
	}

	public void testFlowExecutionListener() {
		SimpleFlowExecutionListener listener = new SimpleFlowExecutionListener();
		manager.addListener(listener);
		Map input = new HashMap(1);
		input.put(FlowExecutionManagFlowExecutionManagerImpl, "simpleFlow");
		manager.handleFlowRequest(new MockExternalContext(input));
		assertTrue("Listener not invoked", listener.invoked);
	}

	public void testFlowExecutionListenerMap() {
		SimpleFlowExecutionListener listener1 = new SimpleFlowExecutionListener();
		SimpleFlowExecutionListener listener2 = new SimpleFlowExecutionListener();
		SimpleFlowExecutionListener listener3 = new SimpleFlowExecutionListener();
		Map listenerMap = new HashMap();
		listenerMap.put(listener1, FlowExecutionListenerCriteriaFactory.flows(new String[] { "simpleFlow",
				"some other flow" }));
		List listeners = new ArrayList();
		listeners.add(listener2);
		listeners.add(listener3);
		listenerMap.put(listeners, FlowExecutionListenerCriteriaFactory.flow("not this one!"));
		manager.setListenerMap(listenerMap);
		Map input = new HashMap(1);
		input.put(FlowExecutionManagFlowExecutionManagerImpl, "simpleFlow");
		manager.handleFlowRequest(new MockExternalContext(input));
		assertTrue("Listener not invoked", listener1.invoked);
		assertFalse("Listener invoked", listener2.invoked);
		assertFalse("Listener invoked", listener3.invoked);
	}

	public void testFlowExecutionListenerDoesNotApply() {
		SimpleFlowExecutionListener listener = new SimpleFlowExecutionListener();
		manager.addListener(listener, FlowExecutionListenerCriteriaFactory.flow("not this one"));
		Map input = new HashMap(1);
		input.put(FlowExecutionManagFlowExecutionManagerImpl, "simpleFlow");
		manager.handleFlowRequest(new MockExternalContext(input));
		assertFalse("Listener invoked", listener.invoked);
	}

	public void testFlowExecutionConfiguration() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowExecutionManagerImpl manager = (FlowExecutionManagerImpl)ac.getBean("flowExecutionManager");
		assertEquals("Wrong number of listeners", 1, manager.getListenerSet().size());
		assertTrue(manager.getFlowLocator() instanceof SimpleFlowLocator);
	}

	public static class SimpleFlowLocator implements FlowLocator {
		private SimpleFlow simpleFlow = new SimpleFlow();

		public Flow getFlow(String id) {
			if (simpleFlow.getId().equals(id)) {
				return simpleFlow;
			}
			else {
				throw new FlowArtifactException(id, Flow.class);
			}
		}
	}
}