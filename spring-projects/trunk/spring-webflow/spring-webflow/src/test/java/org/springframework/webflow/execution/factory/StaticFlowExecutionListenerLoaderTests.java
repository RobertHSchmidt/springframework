package org.springframework.webflow.execution.factory;

import junit.framework.TestCase;

import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;

public class StaticFlowExecutionListenerLoaderTests extends TestCase {

	private StaticFlowExecutionListenerLoader loader = new StaticFlowExecutionListenerLoader();

	protected void setUp() throws Exception {
	}

	public void testEmptyListenerArray() {
		assertEquals(0, loader.getListeners(new Flow("foo")).length);
		assertEquals(0, loader.getListeners(null).length);
	}

	public void testStaticListener() {
		final FlowExecutionListener listener1 = new FlowExecutionListenerAdapter() {
		};
		loader = new StaticFlowExecutionListenerLoader(listener1);
		assertEquals(listener1, loader.getListeners(new Flow("foo"))[0]);
	}

	public void testStaticListeners() {
		final FlowExecutionListener listener1 = new FlowExecutionListenerAdapter() {
		};
		final FlowExecutionListener listener2 = new FlowExecutionListenerAdapter() {
		};

		loader = new StaticFlowExecutionListenerLoader(new FlowExecutionListener[] { listener1, listener2 });
		assertEquals(listener1, loader.getListeners(new Flow("foo"))[0]);
		assertEquals(listener2, loader.getListeners(new Flow("foo"))[1]);
	}

}