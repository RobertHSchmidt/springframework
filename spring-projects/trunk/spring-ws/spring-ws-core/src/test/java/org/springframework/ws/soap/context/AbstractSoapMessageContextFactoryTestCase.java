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

package org.springframework.ws.soap.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.TransportRequest;

public abstract class AbstractSoapMessageContextFactoryTestCase extends TestCase {

    private MessageContextFactory contextFactory;

    protected void setUp() throws Exception {
        contextFactory = createSoapMessageContextFactory();
        if (contextFactory instanceof InitializingBean) {
            ((InitializingBean) contextFactory).afterPropertiesSet();
        }
    }

    protected abstract MessageContextFactory createSoapMessageContextFactory();

    public void testCreateMessageFromHttpServletRequest11() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "text/xml");
        headers.setProperty("SOAPAction", "\"Some-URI\"");
        MockTransportRequest request = new MockTransportRequest(headers, "soap11.xml");

        MessageContext messageContext = contextFactory.createContext(request);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, requestMessage.getVersion());
        assertEquals("Invalid soap action", "\"Some-URI\"", requestMessage.getSoapAction());
    }

    public void testCreateMessageFromHttpServletRequest11WithAttachment() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type",
                "multipart/related; type=\"text/xml\"; boundary=\"----=_Part_0_11416420.1149699787554\"");
        MockTransportRequest request = new MockTransportRequest(headers, "soap11-attachment.bin");

        MessageContext messageContext = contextFactory.createContext(request);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, requestMessage.getVersion());
        Attachment attachment = requestMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }

    public void testCreateMessageFromHttpServletRequest12() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "application/soap+xml");
        headers.setProperty("SOAPAction", "\"Some-URI\"");
        MockTransportRequest request = new MockTransportRequest(headers, "soap12.xml");

        MessageContext messageContext = contextFactory.createContext(request);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
        assertEquals("Invalid soap action", "\"Some-URI\"", requestMessage.getSoapAction());
    }

    public void testCreateMessageFromHttpServletRequest12WithAttachment() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type",
                "multipart/related; type=\"application/soap+xml\"; boundary=\"----=_Part_0_11416420.1149699787554\"");
        MockTransportRequest request = new MockTransportRequest(headers, "soap12-attachment.bin");

        MessageContext messageContext = contextFactory.createContext(request);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
        Attachment attachment = requestMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }

    private static class MockTransportRequest implements TransportRequest {

        private Properties headers;

        private byte[] contents;

        private MockTransportRequest(Properties headers, String fileName) throws IOException {
            this.headers = headers;
            this.contents = FileCopyUtils
                    .copyToByteArray(AbstractSoapMessageContextFactoryTestCase.class.getResourceAsStream(fileName));
        }

        public Iterator getHeaderNames() {
            return headers.keySet().iterator();
        }

        public Iterator getHeaders(String name) {
            String value = headers.getProperty(name);
            return value != null ? Collections.singletonList(value).iterator() : Collections.EMPTY_LIST.iterator();
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contents);
        }
    }
}
