package org.springframework.webflow.execution;

import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.config.RedirectViewSelector;
import org.springframework.webflow.config.SimpleViewSelector;

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
		impl.start(new Event(this, "start"));
		Event event = new Event(this, "submit");
		FlowExecution execution = storage.load(storage.encode(impl), event);
		assertEquals("Not equal", impl, execution);
	}

	public void testClientContinuationStorageEncoding() throws IOException, ClassNotFoundException {
		ClientContinuationFlowExecutionStorage storage = new ClientContinuationFlowExecutionStorage();
		FlowExecutionImpl impl = new FlowExecutionImpl(new SimpleFlow());
		Event event = new Event(this, "start");
		impl.start(event);
		Serializable id = storage.save(null, impl, event);
		FlowExecution execution = storage.decode(id);
		assertEquals("Not equal", impl, execution);
	}
}