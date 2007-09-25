package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutorBeanDefinitionParserTests extends TestCase {
	private ClassPathXmlApplicationContext context;
	private FlowExecutor executor;

	public void setUp() {
		context = new ClassPathXmlApplicationContext("org/springframework/webflow/config/flow-executor.xml");
		executor = (FlowExecutor) context.getBean("flowExecutor");
	}

	public void testExecute() {
		MockExternalContext context = new MockExternalContext();
		context.setFlowId("flow");
		executor.execute(context);
	}

}
