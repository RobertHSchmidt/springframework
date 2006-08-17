package org.springframework.webflow.executor;

public class ContinuationFlowExecutorIntegrationTests extends FlowExecutorIntegrationTests {
	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/webflow/executor/context.xml",
				"org/springframework/webflow/executor/repository-continuation.xml" };
	}
}