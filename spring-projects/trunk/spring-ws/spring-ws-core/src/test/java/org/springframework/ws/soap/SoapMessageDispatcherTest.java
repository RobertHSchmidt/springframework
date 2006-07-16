/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap;

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.soap.saaj.SaajSoapMessageContext;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;

public class SoapMessageDispatcherTest extends TestCase {

    private SoapMessageDispatcher dispatcher;

    private MockControl interceptorControl;

    private SoapEndpointInterceptor interceptorMock;

    protected void setUp() throws Exception {
        interceptorControl = MockControl.createControl(SoapEndpointInterceptor.class);
        interceptorMock = (SoapEndpointInterceptor) interceptorControl.getMock();
        dispatcher = new SoapMessageDispatcher();
    }

    public void testProcessMustUnderstandHeadersUnderstoodSoap11() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header =
                request.getSOAPHeader().addHeaderElement(new QName("http://www.springframework.org", "Header"));
        header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
        header.setMustUnderstand(true);
        SaajSoapMessageContext context = new SaajSoapMessageContext(request, messageFactory);
        interceptorMock.understands(null);
        interceptorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        interceptorControl.setReturnValue(true);
        interceptorControl.replay();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        assertTrue("Header not understood", result);
        interceptorControl.verify();
    }

    public void testProcessMustUnderstandHeadersUnderstoodSoap12() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header =
                request.getSOAPHeader().addHeaderElement(new QName("http://www.springframework.org", "Header"));
        header.setMustUnderstand(true);
        header.setRole(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
        SaajSoapMessageContext context = new SaajSoapMessageContext(request, messageFactory);
        interceptorMock.understands(null);
        interceptorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        interceptorControl.setReturnValue(true);
        interceptorControl.replay();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        assertTrue("Header not understood", result);
        interceptorControl.verify();
    }

    public void testProcessMustUnderstandHeadersNotUnderstoodSoap11() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
        header.setMustUnderstand(true);
        SaajSoapMessageContext context = new SaajSoapMessageContext(request, messageFactory);
        interceptorMock.understands(null);
        interceptorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        interceptorControl.setReturnValue(false);
        interceptorControl.replay();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        assertFalse("Header understood", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapBody responseBody = context.getSoapResponse().getSoapBody();
        assertTrue("Response body has no fault", responseBody.hasFault());
        Soap11Fault fault = (Soap11Fault) responseBody.getFault();
        assertEquals("Invalid fault code",
                new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "MustUnderstand"),
                fault.getFaultCode());
        assertEquals("Invalid fault string",
                SoapMessageDispatcher.DEFAULT_MUST_UNDERSTAND_FAULT,
                fault.getFaultString());
        assertEquals("Invalid fault string locale", Locale.ENGLISH, fault.getFaultStringLocale());
        assertEquals("Invalid fault actor", SOAPConstants.URI_SOAP_ACTOR_NEXT, fault.getFaultActorOrRole());
        interceptorControl.verify();
    }

    public void testProcessMustUnderstandHeadersNotUnderstoodSoap12() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        header.setMustUnderstand(true);
        header.setRole(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
        SaajSoapMessageContext context = new SaajSoapMessageContext(request, messageFactory);
        interceptorMock.understands(null);
        interceptorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        interceptorControl.setReturnValue(false);
        interceptorControl.replay();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        assertFalse("Header understood", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapBody responseBody = context.getSoapResponse().getSoapBody();
        assertTrue("Response body has no fault", responseBody.hasFault());
        Soap12Fault fault = (Soap12Fault) responseBody.getFault();
        assertEquals("Invalid fault code",
                new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "MustUnderstand"),
                fault.getFaultCode());
        assertEquals("Invalid fault string",
                SoapMessageDispatcher.DEFAULT_MUST_UNDERSTAND_FAULT,
                fault.getFaultReasonText(Locale.ENGLISH));
        assertEquals("Invalid fault actor", SOAPConstants.URI_SOAP_1_2_ROLE_NEXT, fault.getFaultActorOrRole());
        SoapHeader responseHeader = context.getSoapResponse().getSoapHeader();
        Iterator iterator = responseHeader.examineAllHeaderElements();
        assertTrue("Response header has no elements", iterator.hasNext());
        SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
        assertEquals("No NotUnderstood header",
                new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "NotUnderstood"),
                headerElement.getName());
        interceptorControl.verify();
        context.getSoapResponse().writeTo(System.out);
    }

    public void testProcessMustUnderstandHeadersForActorSoap11() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        String headerActor = "http://www/springframework.org/role";
        header.setActor(headerActor);
        header.setMustUnderstand(true);
        SaajSoapMessageContext context = new SaajSoapMessageContext(request, messageFactory);
        interceptorMock.understands(null);
        interceptorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        interceptorControl.setReturnValue(true);
        interceptorControl.replay();

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock},
                new String[]{headerActor});

        boolean result = dispatcher.handleRequest(chain, context);
        assertTrue("actor-specific header not understood", result);
        interceptorControl.verify();
    }

    public void testProcessMustUnderstandHeadersForRoleSoap11() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        String headerRole = "http://www/springframework.org/role";
        header.setRole(headerRole);
        header.setMustUnderstand(true);
        SaajSoapMessageContext context = new SaajSoapMessageContext(request, messageFactory);
        interceptorMock.understands(null);
        interceptorControl.setMatcher(MockControl.ALWAYS_MATCHER);
        interceptorControl.setReturnValue(true);
        interceptorControl.replay();

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock},
                new String[]{headerRole});

        boolean result = dispatcher.handleRequest(chain, context);
        assertTrue("role-specific header not understood", result);
        interceptorControl.verify();
    }

    public void testProcessNoHeader() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        request.getSOAPHeader().detachNode();
        SaajSoapMessageContext context = new SaajSoapMessageContext(request, messageFactory);
        interceptorControl.replay();

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock},
                new String[]{"role"});

        boolean result = dispatcher.handleRequest(chain, context);
        assertTrue("Invalid result", result);
        interceptorControl.verify();
    }

    public void testGetRoles() throws Exception {
        String[] roles = dispatcher.getRoles(null, SoapVersion.SOAP_11);
        assertEquals("Invalid amount of roles", 1, roles.length);
        assertEquals("Invalid role", SoapVersion.SOAP_11.getNextActorOrRoleUri(), roles[0]);
        roles = dispatcher.getRoles(new String[]{"role"}, SoapVersion.SOAP_11);
        assertEquals("Invalid amount of roles", 2, roles.length);
        assertEquals("Invalid role", "role", roles[0]);
        assertEquals("Invalid role", SoapVersion.SOAP_11.getNextActorOrRoleUri(), roles[1]);
    }

}