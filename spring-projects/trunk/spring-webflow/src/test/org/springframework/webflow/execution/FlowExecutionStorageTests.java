package org.springframework.webflow.execution;

import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.config.support.RedirectViewSelector;
import org.springframework.webflow.config.support.SimpleViewSelector;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionStorageTests extends TestCase {

	public static class SimpleFlow extends Flow {
		public SimpleFlow() {
			super("simpleFlow");
			add(new ViewState(this, "view", new SimpleViewSelector("view"), new Transition[] { new Transition("end") }));
			add(new EndState(this, "end", new RedirectViewSelector(new StaticExpression("confirm"))));
			resolveStateTransitionsTargetStates();
		}
	}

	public void testClientContinuationStorageDecoding() throws IOException {
		ClientContinuationFlowExecutionStorage storage = new ClientContinuationFlowExecutionStorage();
		FlowExecutionImpl impl = new FlowExecutionImpl(new SimpleFlow());
		FlowExecution execution = storage.load(storage.encode(impl), new MockExternalContext());
		assertEquals("Not equal", impl, execution);
	}

	public void testClientContinuationStorageEncoding() throws IOException, ClassNotFoundException {
		ClientContinuationFlowExecutionStorage storage = new ClientContinuationFlowExecutionStorage();
		FlowExecutionImpl impl = new FlowExecutionImpl(new SimpleFlow());
		Serializable id = storage.save(null, impl, new MockExternalContext());
		FlowExecution execution = storage.decode(id);
		assertEquals("Not equal", impl, execution);
	}
}