package org.springframework.webflow.samples.sellitem;

import org.springframework.webflow.test.AbstractFlowExecutionTests;

public class SellItemFlowExecutionTests extends AbstractFlowExecutionTests {

	protected String flowId() {
		return "sellItem";
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/webflow/samples/sellitem/applicationContext.xml" };
	}

	public void testStartFlow() {
		startFlow();
	}
}