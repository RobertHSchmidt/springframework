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
package org.springframework.integration.batch.item;

import org.springframework.batch.item.AbstractItemWriter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.message.GenericMessage;

/**
 * @author Dave Syer
 * 
 */
public class MessageChannelItemWriter extends AbstractItemWriter {

	private MessageChannel channel;

	/**
	 * Public setter for the channel.
	 * @param channel the channel to set
	 */
	@Required
	public void setChannel(MessageChannel channel) {
		this.channel = channel;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemWriter#write(java.lang.Object)
	 */
	public void write(Object item) throws Exception {
		channel.send(new GenericMessage<Object>(item));
	}

}
