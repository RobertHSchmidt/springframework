/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.support.ApplicationViewSelector;
import org.springframework.webflow.execution.factory.ConditionalFlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.continuation.ClientContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.ContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.AbstractConversationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.SimpleFlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutorImpl;

/**
 * Unit tests for the WebFlowConfigNamespaceHandler and its BeanDefinitionParsers.
 * 
 * @author Ben Hale
 * @author Erwin Vervaet
 * @author Christian Dupuis
 */
public class WebFlowConfigNamespaceHandlerTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	protected void setUp() throws Exception {
		super.setUp();
		this.beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.loadBeanDefinitions(new ClassPathResource(
				"org/springframework/webflow/config/webflow-config-namespace.xml"));
	}

	public void testRegistryWithPath() {
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry) this.beanFactory.getBean("withPath");
		assertEquals("Incorrect number of flows loaded", 1, registry.getFlowDefinitionCount());
	}

	public void testRegistryWithoutPath() {
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry) this.beanFactory.getBean("withoutPath");
		assertEquals("Incorrect number of flows loaded", 0, registry.getFlowDefinitionCount());
	}

	public void testRegistryWithPathWithWildcards() {
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry) this.beanFactory.getBean("withPathWithWildcards");
		assertEquals("Incorrect number of flows loaded", 0, registry.getFlowDefinitionCount());
	}

	public void testDefaultExecutor() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("defaultExecutor");
		assertTrue(flowExecutor.getExecutionRepository() instanceof ContinuationFlowExecutionRepository);
		assertSame(this.beanFactory.getBean("withPathWithWildcards"), flowExecutor.getDefinitionLocator());
		AttributeMap attribs = ((FlowExecutionImplFactory) flowExecutor.getExecutionFactory()).getExecutionAttributes();
		assertEquals(1, attribs.size()); // defaults have been applied
		assertEquals(new Boolean(true), attribs.get(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE));
	}

	public void testSimpleExecutor() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("simpleExecutor");
		assertSame(this.beanFactory.getBean("withPathWithWildcards"), flowExecutor.getDefinitionLocator());
		assertTrue(flowExecutor.getExecutionRepository() instanceof SimpleFlowExecutionRepository);
		assertTrue(((SimpleFlowExecutionRepository) flowExecutor.getExecutionRepository()).isAlwaysGenerateNewNextKey());
		AttributeMap attribs = ((FlowExecutionImplFactory) flowExecutor.getExecutionFactory()).getExecutionAttributes();
		assertEquals(3, attribs.size());
		assertEquals(new Boolean(true), attribs.get(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE));
		assertEquals("test", attribs.get("test"));
		assertEquals(new Integer(1), attribs.get("test1"));
		assertSame(StaticFlowExecutionListenerLoader.EMPTY_INSTANCE, ((FlowExecutionImplFactory) flowExecutor
				.getExecutionFactory()).getExecutionListenerLoader());
	}

	public void testContinuationExecutor() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("continuationExecutor");
		assertSame(this.beanFactory.getBean("withPathWithWildcards"), flowExecutor.getDefinitionLocator());
		assertTrue(flowExecutor.getExecutionRepository() instanceof ContinuationFlowExecutionRepository);
		AttributeMap attribs = ((FlowExecutionImplFactory) flowExecutor.getExecutionFactory()).getExecutionAttributes();
		assertEquals(1, attribs.size());
		assertEquals(new Boolean(false), attribs.get(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE));
		ConditionalFlowExecutionListenerLoader ll = (ConditionalFlowExecutionListenerLoader) ((FlowExecutionImplFactory) flowExecutor
				.getExecutionFactory()).getExecutionListenerLoader();
		assertEquals(1, ll.getListeners(new Flow("test")).length);
		assertSame(this.beanFactory.getBean("listener1"), ll.getListeners(new Flow("test"))[0]);
	}

	public void testClientExecutor() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("clientExecutor");
		assertSame(this.beanFactory.getBean("withPathWithWildcards"), flowExecutor.getDefinitionLocator());
		assertTrue(flowExecutor.getExecutionRepository() instanceof ClientContinuationFlowExecutionRepository);
		AttributeMap attribs = ((FlowExecutionImplFactory) flowExecutor.getExecutionFactory()).getExecutionAttributes();
		assertEquals(1, attribs.size());
		assertEquals(new Boolean(true), attribs.get(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE));
		ConditionalFlowExecutionListenerLoader ll = (ConditionalFlowExecutionListenerLoader) ((FlowExecutionImplFactory) flowExecutor
				.getExecutionFactory()).getExecutionListenerLoader();
		assertEquals(2, ll.getListeners(new Flow("flow1")).length);
		assertSame(this.beanFactory.getBean("listener1"), ll.getListeners(new Flow("flow1"))[0]);
		assertSame(this.beanFactory.getBean("listener2"), ll.getListeners(new Flow("flow1"))[1]);
		assertEquals(1, ll.getListeners(new Flow("flow2")).length);
		assertSame(this.beanFactory.getBean("listener2"), ll.getListeners(new Flow("flow2"))[0]);
		assertEquals(1, ll.getListeners(new Flow("flow3")).length);
		assertSame(this.beanFactory.getBean("listener2"), ll.getListeners(new Flow("flow3"))[0]);
	}

	public void testSingleKeyExecutor() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("singleKeyExecutor");
		assertSame(this.beanFactory.getBean("withPathWithWildcards"), flowExecutor.getDefinitionLocator());
		assertTrue(flowExecutor.getExecutionRepository() instanceof SimpleFlowExecutionRepository);
		assertFalse(((SimpleFlowExecutionRepository) flowExecutor.getExecutionRepository())
				.isAlwaysGenerateNewNextKey());
		AttributeMap attribs = ((FlowExecutionImplFactory) flowExecutor.getExecutionFactory()).getExecutionAttributes();
		assertEquals(1, attribs.size());
		assertEquals(new Boolean(true), attribs.get(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE));
		assertSame(StaticFlowExecutionListenerLoader.EMPTY_INSTANCE, ((FlowExecutionImplFactory) flowExecutor
				.getExecutionFactory()).getExecutionListenerLoader());
	}

	public void testDuplicateRepositoryType() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
		DefaultProblemReporter problemReporter = new DefaultProblemReporter();
		reader.setProblemReporter(problemReporter);
		reader.loadBeanDefinitions(new ClassPathResource("org/springframework/webflow/config/namespace-error-1.xml"));
		assertNotNull("Should have created an error", problemReporter.getLastProblem());
		assertTrue(problemReporter.getLastProblem().getMessage().contains("repositoryType"));
	}

	public void testConversationManagerRef() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
		DefaultProblemReporter problemReporter = new DefaultProblemReporter();
		reader.setProblemReporter(problemReporter);
		reader.loadBeanDefinitions(new ClassPathResource("org/springframework/webflow/config/namespace-error-2.xml"));
		assertNotNull("Should have created an error", problemReporter.getLastProblem());
		assertTrue(problemReporter.getLastProblem().getMessage().contains("conversation"));
	}

	public void testMaxContinuation() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
		DefaultProblemReporter problemReporter = new DefaultProblemReporter();
		reader.setProblemReporter(problemReporter);
		reader.loadBeanDefinitions(new ClassPathResource("org/springframework/webflow/config/namespace-error-3.xml"));
		assertNotNull("Should have created an error", problemReporter.getLastProblem());
		assertTrue(problemReporter.getLastProblem().getMessage().contains("continuation"));
	}

	public void testContinuationExtended() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("continuationExtended");
		assertTrue("Repository type should be ContinuationFlowExecutionRepository", flowExecutor
				.getExecutionRepository() instanceof ContinuationFlowExecutionRepository);
	}

	public void testClientExtended() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("clientExtended");
		assertTrue("Repository type should be ClientContinuationFlowExecutionRepository", flowExecutor
				.getExecutionRepository() instanceof ClientContinuationFlowExecutionRepository);
	}

	public void testSimpleExtended() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("simpleExtended");
		assertTrue("Repository type should be SimpleFlowExecutionRepository",
				flowExecutor.getExecutionRepository() instanceof SimpleFlowExecutionRepository);
	}

	public void testSinglekeyExtended() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("singlekeyExtended");
		assertTrue("Repository type should be SimpleFlowExecutionRepository",
				flowExecutor.getExecutionRepository() instanceof SimpleFlowExecutionRepository);
	}

	public void testConversationManagerExtended() {
		FlowExecutorImpl flowExecutor = (FlowExecutorImpl) this.beanFactory.getBean("conversationManagerExtended");
		assertTrue("Repository type should be SimpleFlowExecutionRepository",
				flowExecutor.getExecutionRepository() instanceof SimpleFlowExecutionRepository);
		AbstractConversationFlowExecutionRepository repository = (AbstractConversationFlowExecutionRepository) flowExecutor
				.getExecutionRepository();
		ConversationManager conversationManager = (ConversationManager) this.beanFactory.getBean("conversationManager");
		assertSame("The conversation manager in the repository should be the one explicitly wired",
				conversationManager, repository.getConversationManager());
	}

	/**
	 * {@link ProblemReporter} implementation that simply stores the last reported error
	 */
	static class DefaultProblemReporter implements ProblemReporter {

		private Problem problem;

		public Problem getLastProblem() {
			return this.problem;
		}

		public void error(Problem problem) {
			this.problem = problem;
		}

		public void fatal(Problem problem) {
			this.problem = problem;

		}

		public void warning(Problem problem) {
			this.problem = problem;
		}
	}

}