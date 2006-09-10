package org.springframework.webflow.engine.builder.xml;

import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.DefaultFlowServiceLocator;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.test.MockExternalContext;

public class MessageSourceAwareTests extends TestCase {

	private Flow flow;
	
	protected void setUp() throws Exception {
		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(FooMessageSource.class);
		context.registerBeanDefinition("messageSource", builder.getBeanDefinition());
		context.refresh();
		DefaultFlowServiceLocator locator = new DefaultFlowServiceLocator(new FlowDefinitionRegistryImpl(), context);
		XmlFlowBuilder flowBuilder = new XmlFlowBuilder(new ClassPathResource("messageSourceAwareFlow.xml", getClass()), locator);
		flow = new FlowAssembler("flow", flowBuilder).assembleFlow();
	}
	
	private static class FooMessageSource extends StaticMessageSource {
		public FooMessageSource() {
			addMessage("foo", Locale.getDefault(), "bar");
		}		
	}
	
	public void testAwareAction() {
		FlowExecution execution = new FlowExecutionImplFactory().createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		assertFalse(execution.isActive());
	}

}
