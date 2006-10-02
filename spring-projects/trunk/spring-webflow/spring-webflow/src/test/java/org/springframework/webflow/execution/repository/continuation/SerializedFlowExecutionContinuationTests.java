package org.springframework.webflow.execution.repository.continuation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.SimpleFlow;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.test.MockExternalContext;

public class SerializedFlowExecutionContinuationTests extends TestCase {
	public void testCreate() throws Exception {
		FlowDefinition flow = new SimpleFlow();
		FlowExecution execution = new FlowExecutionImplFactory().createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		SerializedFlowExecutionContinuation c = new SerializedFlowExecutionContinuation(execution, true);
		assertTrue(c.isCompressed());
		byte[] array = c.toByteArray();
		System.out.println(array.length);
		execution = c.unmarshal();
		
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(array));
        try {
            c = (SerializedFlowExecutionContinuation)ois.readObject();
            assertTrue(c.isCompressed());
            execution = c.unmarshal();
        }
        finally {
            ois.close();
        }
	}

}
