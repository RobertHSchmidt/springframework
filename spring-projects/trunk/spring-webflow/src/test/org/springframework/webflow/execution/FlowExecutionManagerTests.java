package org.springframework.webflow.execution;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.config.SimpleViewDescriptorCreator;

public class FlowExecutionManagerTests extends TestCase {
	public static class MapDataStoreAccessor implements DataStoreAccessor {
		private MapAttributeSource source = new MapAttributeSource();

		public MutableAttributeSource getDataStore(Event sourceEvent) {
			return source;
		}
	}

	public static class SimpleFlowExecutionListener extends FlowExecutionListenerAdapter {
		
	}
	
	public static class SimpleFlow extends Flow {
		public SimpleFlow() {
			super("simpleFlow");
			add(new ViewState(this, "view", new SimpleViewDescriptorCreator("view"), new Transition[] { new Transition(
					"end") }));
			add(new EndState(this, "end", new SimpleViewDescriptorCreator("confirm")));
			resolveStateTransitionsTargetStates();
		}
	}

	public void testLaunchNewFlow() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
		Map input = new HashMap(1);
		input.put(FlowExecutionManager.FLOW_ID_PARAMETER, "simpleFlow");
		ViewDescriptor view = manager.onEvent(new Event(this, "start", input));
		assertEquals("Wrong view name", "view", view.getViewName());
	}

	public void testPartcipateInExistingFlow() {
		FlowExecutionManager manager = new FlowExecutionManager(new SimpleFlowLocator());
		manager.setStorage(new DataStoreFlowExecutionStorage(new MapDataStoreAccessor()));
		Map input = new HashMap(2);
		input.put(FlowExecutionManager.FLOW_ID_PARAMETER, "simpleFlow");
		ViewDescriptor view = manager.onEvent(new Event(this, "start", input));
		input.put(FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER, view.getModel().get(
				FlowExecutionManager.FLOW_EXECUTION_ID_ATTRIBUTE));
		view = manager.onEvent(new Event(this, "submit", input));
		assertEquals("Wrong view name", "confirm", view.getViewName());
	}

	public void testFlowExecutionConfiguration() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowExecutionManager manager = (FlowExecutionManager)ac.getBean("flowExecutionManager");
		assertEquals("Wrong number of listeners", 1, manager.getListenerMap().size());
	}
	
	public static class SimpleFlowLocator implements FlowLocator {
		private SimpleFlow simpleFlow = new SimpleFlow();
		
		public Flow getFlow(String id) {
			return simpleFlow;
		}
	}
}