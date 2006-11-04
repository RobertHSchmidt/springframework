/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.transport.jms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.util.Assert;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * @author Arjen Poutsma
 */
public class JmsTransportOutputStream extends TransportOutputStream {

    private TextMessage textMessage;

    private Session session;

    public JmsTransportOutputStream(Session session) {
        Assert.notNull(session, "session must not be null");
        this.session = session;
    }

    private TextMessage getTextMessage() throws IOException {
        if (textMessage == null) {
            try {
                textMessage = session.createTextMessage();
            }
            catch (JMSException ex) {
                throw new IOException("Could not create text message: " + ex.getMessage());
            }
        }
        return textMessage;
    }

    protected OutputStream getOutputStream() throws IOException {
        return new TextMessageOutputStream(getTextMessage());
    }

    public void addHeader(String name, String value) throws IOException {
        try {
            getTextMessage().setStringProperty(name, value);
        }
        catch (JMSException ex) {
            throw new IOException("Could not set property " + ex.getMessage());
        }
    }

    private static class TextMessageOutputStream extends ByteArrayOutputStream {

        private final TextMessage textMessage;

        public TextMessageOutputStream(TextMessage textMessage) {
            this.textMessage = textMessage;
        }

        public void close() throws IOException {
            try {
                textMessage.setText(new String(toString("UTF-8")));
            }
            catch (JMSException ex) {
                throw new IOException("Could not set message text: " + ex.getMessage());
            }
        }
    }
}
