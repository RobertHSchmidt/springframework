/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.execution.continuation;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.binding.format.Formatter;
import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.FlowExecutionContinuationnKeyFormatter;
import org.springframework.webflow.execution.FlowExecutionStorage;
import org.springframework.webflow.execution.FlowExecutionStorageException;
import org.springframework.webflow.execution.MapAccessor;
import org.springframework.webflow.execution.NoSuchConversationException;
import org.springframework.webflow.execution.SessionMapAccessor;
import org.springframework.webflow.util.UidGenerator;
import org.springframework.webflow.util.RandomGuidUidGenerator;

/**
 * Flow execution storage implementation that will store a flow execution as a
 * <i>continuation</i> on the client side. It will actually encode the state of
 * the flow execution in the unique id that is returned from the
 * {@link #save(Serializable, FlowExecution, ExternalContext) save} method. The
 * load method just decodes the incoming id and restores the
 * <code>FlowExecution</code> object.
 * <p>
 * Note that all clients in a web flow based application need to include the
 * unique flow execution id in each event they signal to the system. For HTTP
 * based clients, the flow execution id is sent using a request parameter. If
 * you're using client side continuations, you should make sure to use the HTTP
 * POST method to send the request parameters to the server. This is required
 * because there are limitations on the amount of data you can send using an
 * HTTP GET request and a client side continuation easily surpasses that
 * threshold.
 * <p>
 * <b>Warning:</b> storing state (a flow execution continuation) on the client
 * entails a certain security risk. This implementation does not provide a
 * secure way of storing state on the client, so a malicious client could
 * reverse engineer a continuation and get access to possible sensitive data
 * stored in the flow execution. If you need more security and still want to
 * store continuations on the client, subclass this class and override the
 * methods {@link #encode(FlowExecution)} and {@link #decode(Serializable)},
 * implementing them with a secure encoding/decoding algorithm, e.g. based on
 * public/private key encryption.
 * <p>
 * This class depends on the <a href="http://jakarta.apache.org/commons/codec/">
 * Jakarta Commons Codec</a> library to do BASE64 encoding.
 * 
 * @author Erwin Vervaet
 */
public class ClientContinuationFlowExecutionStorage implements FlowExecutionStorage {

	protected static final String CONVERSATION_ATTRIBUTE_PREFIX = ClientContinuationFlowExecutionStorage.class
			.getName()
			+ ".conversation.";

	private MapAccessor conversationMapAccessor = new SessionMapAccessor();

	private Formatter keyFormatter = new FlowExecutionContinuationnKeyFormatter();

	private UidGenerator uidGenerator = new RandomGuidUidGenerator();

	private boolean compress;

	public MapAccessor getConversationMapAccessor() {
		return conversationMapAccessor;
	}

	public void setConversationMapAccessor(MapAccessor mapAccessor) {
		this.conversationMapAccessor = mapAccessor;
	}

	public Formatter getKeyFormatter() {
		return keyFormatter;
	}

	public void setKeyFormatter(Formatter keyFormatter) {
		this.keyFormatter = keyFormatter;
	}

	public UidGenerator getUidGenerator() {
		return uidGenerator;
	}

	public void setUidGenerator(UidGenerator keyGenerator) {
		this.uidGenerator = keyGenerator;
	}

	public boolean getCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public FlowExecution load(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		try {
			FlowExecutionContinuationKey key = parseKey(id);
			assertConversationActive(key.getConversationId(), context);
			return decode(key.getContinuationId());
		}
		catch (IOException e) {
			throw new FlowExecutionSerializationException(id, null,
					"IOException thrown decoding the flow execution -- this should not happen!", e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionSerializationException(id, null,
					"ClassNotFoundException thrown decoding the flow execution -- "
							+ "This should not happen! Make sure there are no classloader issues."
							+ "For example, perhaps the Web Flow system is being loaded by a classloader "
							+ "that is a parent of the classloader loading application classes?", e);
		}
	}

	private void assertConversationActive(Serializable conversationId, ExternalContext context) {
		if (getConversationKeyMap(context).get(CONVERSATION_ATTRIBUTE_PREFIX + conversationId) == null) {
			throw new NoSuchConversationException(conversationId);
		}
	}

	public Serializable save(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException {
		Serializable conversationId = getConversationId(id);
		Serializable encodedFlowExecution;
		try {
			encodedFlowExecution = encode(flowExecution);
		}
		catch (NotSerializableException e) {
			throw new FlowExecutionSerializationException(null, flowExecution,
					"Could not encode flow execution; make sure all objects in flowScope are serializable", e);
		}
		catch (IOException e) {
			throw new FlowExecutionSerializationException(null, flowExecution,
					"IOException thrown encoding flow execution -- this should not happen", e);
		}
		if (id == null) {
			getConversationKeyMap(context).put(CONVERSATION_ATTRIBUTE_PREFIX + conversationId, Boolean.TRUE);
		}
		FlowExecutionContinuationKey key = new FlowExecutionContinuationKey(conversationId, encodedFlowExecution);
		return keyFormatter.formatValue(key);
	}

	public void remove(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		getConversationKeyMap(context).remove(CONVERSATION_ATTRIBUTE_PREFIX + parseKey(id).getConversationId());
	}

	private FlowExecutionContinuationKey parseKey(Serializable id) {
		return (FlowExecutionContinuationKey)keyFormatter.parseValue((String)id, FlowExecutionContinuationKey.class);
	}

	private Map getConversationKeyMap(ExternalContext context) {
		return conversationMapAccessor.getMap(context);
	}

	private Serializable getConversationId(Serializable id) {
		if (id == null) {
			return uidGenerator.generateId();
		}
		else {
			return parseKey(id).getConversationId();
		}
	}

	/**
	 * Decode given data, received from the client, and return the corresponding
	 * flow execution object.
	 * <p>
	 * Subclasses can override this to change the decoding algorithm. This class
	 * just does a BASE64 decoding and then deserializes the flow execution.
	 * @param data the encode flow execution data
	 * @return the decoded flow execution instance
	 */
	protected FlowExecution decode(Serializable data) throws IOException, ClassNotFoundException {
		Assert.notNull(data, "The flow execution data to decode cannot be null");
		byte[] bytes = Base64.decodeBase64(String.valueOf(data).getBytes());
		return new FlowExecutionByteArray(bytes, getCompress()).deserializeFlowExecution();
	}

	/**
	 * Encode given flow execution object into data that can be stored on the
	 * client.
	 * <p>
	 * Subclasses can override this to change the encoding algorithm. This class
	 * just does a BASE64 encoding of the serialized flow execution.
	 * @param flowExecution the flow execution instance
	 * @return the encoded representation
	 */
	protected Serializable encode(FlowExecution flowExecution) throws IOException {
		FlowExecutionByteArray byteArray = new FlowExecutionByteArray(flowExecution, getCompress());
		return new String(Base64.encodeBase64(byteArray.getData(false)));
	}

	/* not supported */

	public boolean supportsTwoPhaseSave() {
		return false;
	}

	public Serializable generateId(Serializable previousId, ExternalContext context)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("This storage strategy does not support pre-generation of storage IDs");
	}

	public void saveWithGeneratedId(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("This storage strategy does not support pre-generation of storage IDs");
	}
}