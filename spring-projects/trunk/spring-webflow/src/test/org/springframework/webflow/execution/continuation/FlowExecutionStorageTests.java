package org.springframework.webflow.execution.continuation;

import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.webflow.Flow;
import org.springframework.webflow.SimpleFlow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionImpl;
import org.springframework.webflow.execution.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.FlowExecutionContinuationnKeyFormatter;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.LocalMapAccessor;
import org.springframework.webflow.execution.NoSuchConversationException;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionStorageTests extends TestCase {

	private LocalMapAccessor localMapAccessor = new LocalMapAccessor();

	public void testClientContinuationStorageEncoding() throws IOException {
		ClientContinuationFlowExecutionStorage storage = new ClientContinuationFlowExecutionStorage();

		storage.setConversationMapAccessor(localMapAccessor);
		final Flow flow = new SimpleFlow();
		FlowExecutionImpl impl = new FlowExecutionImpl(flow);
		Serializable conversationId = storage.getUidGenerator().generateId();
		localMapAccessor.source.put(ClientContinuationFlowExecutionStorage.CONVERSATION_ATTRIBUTE_PREFIX
				+ conversationId, Boolean.TRUE);
		Serializable encodedFlowExecution = storage.encode(impl);
		FlowExecutionContinuationKey key = new FlowExecutionContinuationKey(conversationId, encodedFlowExecution);
		Serializable id = new FlowExecutionContinuationnKeyFormatter().formatValue(key);
		MockExternalContext context = new MockExternalContext();
		FlowExecution execution = storage.load(id, context);
		execution.rehydrate(new FlowLocator() {
			public Flow getFlow(String id) {
				return flow;
			}
		}, null);
		assertNotNull(execution);
		assertSame(flow, execution.getRootFlow());
		storage.remove(id, context);
		try {
			storage.load(id, context);
			fail("Should have thrown no such flow execution exception");
		} catch (NoSuchConversationException e) {
			// expected
		}
	}

	public void testClientContinuationStorageDecoding() throws IOException, ClassNotFoundException {
		ClientContinuationFlowExecutionStorage storage = new ClientContinuationFlowExecutionStorage();
		FlowExecutionImpl impl = new FlowExecutionImpl(new SimpleFlow());
		Serializable id = storage.save(null, impl, new MockExternalContext());
		FlowExecutionContinuationKey key = (FlowExecutionContinuationKey)new FlowExecutionContinuationnKeyFormatter().parseValue((String)id, null);	
		FlowExecution execution = storage.decode(key.getContinuationId());
		assertNotNull(execution);
	}
}