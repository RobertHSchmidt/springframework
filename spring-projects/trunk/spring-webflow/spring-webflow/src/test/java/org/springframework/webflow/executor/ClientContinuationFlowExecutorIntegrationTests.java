package org.springframework.webflow.executor;

public class ClientContinuationFlowExecutorIntegrationTests extends FlowExecutorIntegrationTests {
	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/webflow/executor/context.xml",
				"org/springframework/webflow/executor/repository-client.xml" };
	}
}