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

package org.springframework.ws.soap.security.xwss;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;
import org.springframework.ws.soap.saaj.SaajSoapMessageContext;
import org.springframework.ws.transport.TransportRequest;

public class XwsSecurityInterceptorTest extends TestCase {

    private MessageFactory messageFactory;

    protected void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }

    public void testhandleRequest() throws Exception {
        final SOAPMessage request = messageFactory.createMessage();
        final SOAPMessage validatedRequest = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {
            protected SOAPMessage secureMessage(SOAPMessage message) throws XwsSecuritySecurementException {
                fail("secure not expected");
                return null;
            }

            protected SOAPMessage validateMessage(SOAPMessage message) throws XwsSecurityValidationException {
                assertEquals("Invalid message", request, message);
                return validatedRequest;
            }

        };
        SaajSoapMessageContext context =
                new SaajSoapMessageContext(request, new DummyTransportRequest(), messageFactory);
        interceptor.handleRequest(context, null);
        assertEquals("Invalid request", validatedRequest, context.getSaajRequest());
    }

    public void testhandleResponse() throws Exception {
        final SOAPMessage response = messageFactory.createMessage();
        final SOAPMessage securedResponse = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {
            protected SOAPMessage secureMessage(SOAPMessage message) throws XwsSecuritySecurementException {
                assertEquals("Invalid message", response, message);
                return securedResponse;
            }

            protected SOAPMessage validateMessage(SOAPMessage message) throws XwsSecurityValidationException {
                fail("validate not expected");
                return null;
            }

        };
        SOAPMessage request = messageFactory.createMessage();
        SaajSoapMessageContext context =
                new SaajSoapMessageContext(request, new DummyTransportRequest(), messageFactory);
        context.setSaajResponse(response);
        interceptor.handleResponse(context, null);
        assertEquals("Invalid response", securedResponse, context.getSaajResponse());
    }

    private static class DummyTransportRequest implements TransportRequest {

        public Iterator getHeaderNames() {
            return Collections.EMPTY_LIST.iterator();
        }

        public Iterator getHeaders(String name) {
            return Collections.EMPTY_LIST.iterator();
        }

        public InputStream getInputStream() throws IOException {
            return null;
        }
    }
}