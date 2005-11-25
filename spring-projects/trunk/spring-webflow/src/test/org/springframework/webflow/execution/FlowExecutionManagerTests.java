package org.springframework.webflow.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.EndState;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.config.support.RedirectViewSelector;
import org.springframework.webflow.config.support.SimpleViewSelector;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionManagerTests extends TestCase {
	public static class MapDataStoreAccessor implements DataStoreAccessor {
		private MapAttributeSource source = new MapAttributeSource();

		private Map getMap() {
			return source.getAttributeMap();
		}

		public MutableAttributeSource getDataStore(ExternalContext context) {
			return source;
		}
	}

	public static class SimpleFlowExecutionListener extends FlowExecutionListenerAdapter {
		private boolean invoked;

		public void requestSubmitted(RequestContext context) {
			invoked = true;
		}
	}

	public static class SimpleFlow extends Flow {
		public SimpleFlow() {
			super("simpleFlow");
			add(new ViewState(this, "view", new SimpleViewSelector("view"), new Transition[] { new Transition("end") }));
			add(new EndState(this, "end", new RedirectViewSelector(new StaticExpression("confirm"))));
			resolveStateTransitionsTargetStates();
		}
	}

	public void testLaunchNewFlow() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
		Map input = new HashMap(1);
		input.put(manager.getFlowIdParameterName(), "simpleFlow");
		ViewSelection view = manager.onEvent(new MockExternalContext(input));
		assertNotNull(view.getModel().get(FlowExecutionManager.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertNotNull(view.getModel().get(FlowExecutionManager.CURRENT_STATE_ID_ATTRIBUTE));
		assertEquals(view.getModel().get(FlowExecutionManager.CURRENT_STATE_ID_ATTRIBUTE), "view");
		assertNotNull(view.getModel().get(FlowExecutionManager.FLOW_EXECUTION_CONTEXT_ATTRIBUTE));
		assertEquals("Wrong view name", "view", view.getViewName());
	}

	public void testLaunchNewFlowNoSuchFlowDefinition() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
		Map input = new HashMap(1);
		input.put(manager.getFlowIdParameterName(), "nonexistantFlow");
		try {
			manager.onEvent(new MockExternalContext(input));
			fail("Should have thrown no such flow exception");
		}
		catch (FlowArtifactLookupException e) {
		}
	}

	public void testParticipateInExistingFlowExecution() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		MapDataStoreAccessor map = new MapDataStoreAccessor();
		manager.setStorage(new DataStoreFlowExecutionStorage(map));
		Map input = new HashMap(2);
		input.put(manager.getFlowIdParameterName(), "simpleFlow");
		ViewSelection view = manager.onEvent(new MockExternalContext(input));
		input.put(manager.getFlowExecutionIdParameterName(), view.getModel().get(
				FlowExecutionManager.FLOW_EXECUTION_ID_ATTRIBUTE));
		input.put(manager.getEventIdParameterName(), "submit");
		view = manager.onEvent(new MockExternalContext(input));
		assertEquals("Wrong view name", "confirm", view.getViewName());
		assertTrue("Should have been a redirect", view.isRedirect());
		assertNull("Flow has ended", view.getModel().get(FlowExecutionManager.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertEquals("Should have been removed", 0, map.getMap().size());
	}

	public void testParticipateInExistingFlowExecutionNoSuchFlowExecution() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
		Map input = new HashMap(2);
		input.put(manager.getFlowIdParameterName(), "simpleFlow");
		manager.onEvent(new MockExternalContext(input));
		input.put(FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER, "not correct");
		try {
			manager.onEvent(new MockExternalContext(input));
			fail("should have thrown no such flow execution exception");
		}
		catch (NoSuchFlowExecutionException e) {
			// expected
		}
	}

	public void testFlowExecutionListener() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
		SimpleFlowExecutionListener listener = new SimpleFlowExecutionListener();
		manager.addListener(listener);
		Map input = new HashMap(1);
		input.put(FlowExecutionManager.FLOW_ID_PARAMETER, "simpleFlow");
		manager.onEvent(new MockExternalContext(input));
		assertTrue("Listener not invoked", listener.invoked);
	}

	public void testFlowExecutionListenerMap() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
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
		input.put(FlowExecutionManager.FLOW_ID_PARAMETER, "simpleFlow");
		manager.onEvent(new MockExternalContext(input));
		assertTrue("Listener not invoked", listener1.invoked);
		assertFalse("Listener invoked", listener2.invoked);
		assertFalse("Listener invoked", listener3.invoked);
	}

	public void testFlowExecutionListenerDoesNotApply() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
		SimpleFlowExecutionListener listener = new SimpleFlowExecutionListener();
		manager.addListenerCriteria(listener, FlowExecutionListenerCriteriaFactory.flow("not this one"));
		Map input = new HashMap(1);
		input.put(FlowExecutionManager.FLOW_ID_PARAMETER, "simpleFlow");
		manager.onEvent(new MockExternalContext(input));
		assertFalse("Listener invoked", listener.invoked);
	}

	public void testFlowExecutionConfiguration() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowExecutionManager manager = (FlowExecutionManager)ac.getBean("flowExecutionManager");
		assertEquals("Wrong number of listeners", 1, manager.getListenerMap().size());
		assertTrue(manager.getStorage() instanceof ContinuationDataStoreFlowExecutionStorage);
		assertTrue(manager.getTransactionSynchronizer() instanceof DataStoreTokenTransactionSynchronizer);
		assertTrue(manager.getFlowLocator() instanceof SimpleFlowLocator);
	}

	public static class SimpleFlowLocator implements FlowLocator {
		private SimpleFlow simpleFlow = new SimpleFlow();

		public Flow getFlow(String id) {
			if (simpleFlow.getId().equals(id)) {
				return simpleFlow;
			}
			else {
				throw new FlowArtifactLookupException(Flow.class, id);
			}
		}
	}
}