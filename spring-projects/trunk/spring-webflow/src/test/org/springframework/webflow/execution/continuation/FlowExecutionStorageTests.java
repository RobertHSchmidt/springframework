package org.springframework.webflow.execution.continuation;

import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.webflow.SimpleFlow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionImpl;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionStorageTests extends TestCase {

	public void testClientContinuationStorageDecoding() throws IOException {
		ClientContinuationFlowExecutionStorage storage = new ClientContinuationFlowExecutionStorage();
		FlowExecutionImpl impl = new FlowExecutionImpl(new SimpleFlow());
		FlowExecution execution = storage.load(storage.encode(impl), new MockExternalContext());
		//assertEquals("Not equal", impl, execution);
	}

	public void testClientContinuationStorageEncoding() throws IOException, ClassNotFoundException {
		ClientContinuationFlowExecutionStorage storage = new ClientContinuationFlowExecutionStorage();
		FlowExecutionImpl impl = new FlowExecutionImpl(new SimpleFlow());
		Serializable id = storage.save(null, impl, new MockExternalContext());
		FlowExecution execution = storage.decode(id);
		//assertEquals("Not equal", impl, execution);
	}
}