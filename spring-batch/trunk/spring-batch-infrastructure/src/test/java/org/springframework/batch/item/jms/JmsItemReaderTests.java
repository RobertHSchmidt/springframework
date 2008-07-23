/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.batch.item.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import javax.jms.Message;
import javax.jms.Queue;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.jms.core.JmsOperations;

public class JmsItemReaderTests {

	JmsItemReader<String> itemReader = new JmsItemReader<String>();

	@Test
	public void testNoItemTypeSunnyDay() {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);
		EasyMock.expect(jmsTemplate.receiveAndConvert()).andReturn("foo");
		EasyMock.replay(jmsTemplate);

		itemReader.setJmsTemplate(jmsTemplate);
		assertEquals("foo", itemReader.read());
		EasyMock.verify(jmsTemplate);
	}

	@Test
	public void testSetItemTypeSunnyDay() {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);
		EasyMock.expect(jmsTemplate.receiveAndConvert()).andReturn("foo");
		EasyMock.replay(jmsTemplate);

		itemReader.setJmsTemplate(jmsTemplate);
		itemReader.setItemType(String.class);
		assertEquals("foo", itemReader.read());
		EasyMock.verify(jmsTemplate);
	}

	@Test
	public void testSetItemSubclassTypeSunnyDay() {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);

		Date date = new java.sql.Date(0L);
		EasyMock.expect(jmsTemplate.receiveAndConvert()).andReturn(date);
		EasyMock.replay(jmsTemplate);

		JmsItemReader<Date> itemReader = new JmsItemReader<Date>();
		itemReader.setJmsTemplate(jmsTemplate);
		itemReader.setItemType(Date.class);
		assertEquals(date, itemReader.read());

		EasyMock.verify(jmsTemplate);
	}

	@Test
	public void testSetItemTypeMismatch() {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);
		EasyMock.expect(jmsTemplate.receiveAndConvert()).andReturn("foo");
		EasyMock.replay(jmsTemplate);

		JmsItemReader<Date> itemReader = new JmsItemReader<Date>();
		itemReader.setJmsTemplate(jmsTemplate);
		itemReader.setItemType(Date.class);
		try {
			itemReader.read();
			fail("Expected IllegalStateException");
		}
		catch (IllegalStateException e) {
			// expected
			assertTrue(e.getMessage().indexOf("wrong type") >= 0);
		}
		EasyMock.verify(jmsTemplate);
	}

	@Test
	public void testNextMessageSunnyDay() {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);
		Message message = EasyMock.createMock(Message.class);
		EasyMock.expect(jmsTemplate.receive()).andReturn(message);
		EasyMock.replay(jmsTemplate, message);

		JmsItemReader<Message> itemReader = new JmsItemReader<Message>();
		itemReader.setJmsTemplate(jmsTemplate);
		itemReader.setItemType(Message.class);
		assertEquals(message, itemReader.read());
		EasyMock.verify(jmsTemplate);
	}

	@Test
	public void testRecoverWithNoDestination() throws Exception {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);
		EasyMock.replay(jmsTemplate);

		itemReader.setJmsTemplate(jmsTemplate);
		itemReader.setItemType(String.class);
		itemReader.recover("foo", null);

		EasyMock.verify(jmsTemplate);
	}

	@Test
	public void testErrorQueueWithDestinationName() throws Exception {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);
		jmsTemplate.convertAndSend("queue", "foo");
		EasyMock.expectLastCall();
		EasyMock.replay(jmsTemplate);

		itemReader.setJmsTemplate(jmsTemplate);
		itemReader.setItemType(String.class);
		itemReader.setErrorDestinationName("queue");
		itemReader.recover("foo", null);

		EasyMock.verify(jmsTemplate);
	}

	@Test
	public void testErrorQueueWithDestination() throws Exception {
		JmsOperations jmsTemplate = EasyMock.createMock(JmsOperations.class);
		Queue queue = EasyMock.createMock(Queue.class);
		jmsTemplate.convertAndSend(queue, "foo");
		EasyMock.expectLastCall();
		EasyMock.replay(jmsTemplate, queue);

		itemReader.setJmsTemplate(jmsTemplate);
		itemReader.setItemType(String.class);
		itemReader.setErrorDestination(queue);
		itemReader.recover("foo", null);

		EasyMock.verify(jmsTemplate, queue);
	}

	@Test
	public void testGetKeyFromMessage() throws Exception {
		Message message = EasyMock.createMock(Message.class);
		EasyMock.expect(message.getJMSMessageID()).andReturn("foo");
		EasyMock.replay(message);

		JmsItemReader<Message> itemReader = new JmsItemReader<Message>();
		itemReader.setItemType(Message.class);
		assertEquals("foo", itemReader.getKey(message));

		EasyMock.verify(message);
	}

	@Test
	public void testGetKeyFromNonMessage() throws Exception {
		itemReader.setItemType(String.class);
		assertEquals("foo", itemReader.getKey("foo"));
	}

	@Test
	public void testIsNewForMessage() throws Exception {
		Message message = EasyMock.createMock(Message.class);
		EasyMock.expect(message.getJMSRedelivered()).andReturn(true);
		EasyMock.replay(message);

		JmsItemReader<Message> itemReader = new JmsItemReader<Message>();
		itemReader.setItemType(Message.class);
		assertEquals(false, itemReader.isNew(message));
		
		EasyMock.verify(message);
	}

	@Test
	public void testIsNewForNonMessage() throws Exception {
		itemReader.setItemType(String.class);
		assertEquals(false, itemReader.isNew("foo"));
	}
}
