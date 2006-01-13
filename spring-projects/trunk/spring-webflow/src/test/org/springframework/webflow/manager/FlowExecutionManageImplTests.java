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
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.SharedMapFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.LocalMapLocator;
import org.springframework.webflow.execution.repository.NoSuchConversationException;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionManageImplTests extends TestCase {
	public static class SimpleFlowExecutionListener extends FlowExecutionListenerAdapter {
		private boolean invoked;

		public void requestSubmitted(RequestContext context) {
			invoked = true;
		}
	}

	private FlowExecutionManagerImpl manager = new FlowExecutionManagerImpl(new SimpleFlowLocator());

	private LocalMapLocator mapLocator = new LocalMapLocator();

	public void setUp() {
		SharedMapFlowExecutionRepositoryFactory repositoryLocator = new SharedMapFlowExecutionRepositoryFactory();
		repositoryLocator.setSharedMapLocator(mapLocator);
		manager.setRepositoryFactory(repositoryLocator);
	}

	public void testLaunchNewFlow() {
	}

	public void testLaunchNewFlowNoSuchFlowDefinition() {
	}

	public void testParticipateInExistingFlowExecution() {
	}

	public void testParticipateInExistingFlowExecutionNoSuchFlowExecution() {
	}


	public void testFlowExecutionManagerConfiguration() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowExecutionManagerImpl manager = (FlowExecutionManagerImpl)ac.getBean("flowExecutionManager");
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