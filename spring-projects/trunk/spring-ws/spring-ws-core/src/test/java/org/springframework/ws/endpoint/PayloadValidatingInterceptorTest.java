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

package org.springframework.ws.endpoint;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.mock.MockMessageContext;
import org.springframework.ws.mock.MockWebServiceMessage;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageContext;
import org.springframework.ws.soap.saaj.support.SaajUtils;

public class PayloadValidatingInterceptorTest extends TestCase {

    private PayloadValidatingInterceptor interceptor;

    private MockWebServiceMessage request;

    private MockMessageContext messageContext;

    protected void setUp() throws Exception {
        interceptor = new PayloadValidatingInterceptor();
        interceptor.setSchema(new ClassPathResource("schema.xsd", getClass()));
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        interceptor.afterPropertiesSet();
        request = new MockWebServiceMessage();
        messageContext = new MockMessageContext(request);
    }

    public void testHandleInvalidRequest() throws Exception {
        MockControl messageControl = MockControl.createControl(SoapMessage.class);
        MockControl bodyControl = MockControl.createControl(SoapBody.class);
        SoapBody bodyMock = (SoapBody) bodyControl.getMock();
        MockControl faultControl = MockControl.createControl(SoapFault.class);
        SoapFault faultMock = (SoapFault) faultControl.getMock();
        MockControl faultDetailControl = MockControl.createControl(SoapFaultDetail.class);
        SoapFaultDetail faultDetailMock = (SoapFaultDetail) faultDetailControl.getMock();
        MockControl faultDetailElementControl = MockControl.createControl(SoapFaultDetailElement.class);
        SoapFaultDetailElement faultDetailElementMock = (SoapFaultDetailElement) faultDetailElementControl.getMock();

        final SoapMessage requestMock = (SoapMessage) messageControl.getMock();
        final SoapMessage responseMock = (SoapMessage) messageControl.getMock();
        SoapMessageContext soapMessageContext = new AbstractSoapMessageContext(requestMock) {

            protected SoapMessage createSoapMessage() {
                return responseMock;
            }
        };
        InputStream is = getClass().getResourceAsStream("invalidMessage.xml");
        messageControl.expectAndReturn(requestMock.getPayloadSource(), new StreamSource(is));
        messageControl.expectAndReturn(responseMock.getSoapBody(), bodyMock);
        messageControl.expectAndReturn(responseMock.getVersion(), SoapVersion.SOAP_11);
        bodyControl.expectAndReturn(bodyMock.addFault(SoapVersion.SOAP_11.getSenderFaultName(),
                PayloadValidatingInterceptor.DEFAULT_VALIDATION_ERROR_FAULT_STRING), faultMock);
        faultControl.expectAndReturn(faultMock.addFaultDetail(), faultDetailMock);
        faultDetailControl.expectAndReturn(faultDetailMock.addFaultDetailElement(
                PayloadValidatingInterceptor.DEFAULT_VALIDATION_ERROR_DETAIL_ELEMENT_NAME), faultDetailElementMock, 3);
        faultDetailElementMock.addText(null);
        faultDetailElementControl.setMatcher(MockControl.ALWAYS_MATCHER);
        faultDetailElementControl.setVoidCallable(3);

        messageControl.replay();
        bodyControl.replay();
        faultControl.replay();
        faultDetailControl.replay();
        faultDetailElementControl.replay();

        boolean result = interceptor.handleRequest(soapMessageContext, null);
        assertFalse("Invalid response from interceptor", result);

        messageControl.verify();
        bodyControl.verify();
        faultControl.verify();
        faultDetailControl.verify();
        faultDetailElementControl.verify();
    }

    public void testHandleInvalidRequestOverridenProperties() throws Exception {
        String faultString = "faultString";
        interceptor.setValidationErrorFaultString(faultString);
        interceptor.setAddValidationErrorDetail(false);
        MockControl messageControl = MockControl.createControl(SoapMessage.class);
        MockControl bodyControl = MockControl.createControl(SoapBody.class);
        SoapBody bodyMock = (SoapBody) bodyControl.getMock();
        MockControl faultControl = MockControl.createControl(SoapFault.class);
        SoapFault faultMock = (SoapFault) faultControl.getMock();

        final SoapMessage requestMock = (SoapMessage) messageControl.getMock();
        final SoapMessage responseMock = (SoapMessage) messageControl.getMock();
        SoapMessageContext soapMessageContext = new AbstractSoapMessageContext(requestMock) {

            protected SoapMessage createSoapMessage() {
                return responseMock;
            }
        };
        InputStream is = getClass().getResourceAsStream("invalidMessage.xml");
        messageControl.expectAndReturn(requestMock.getPayloadSource(), new StreamSource(is));
        messageControl.expectAndReturn(responseMock.getSoapBody(), bodyMock);
        messageControl.expectAndReturn(responseMock.getVersion(), SoapVersion.SOAP_11);
        bodyControl
                .expectAndReturn(bodyMock.addFault(SoapVersion.SOAP_11.getSenderFaultName(), faultString), faultMock);

        messageControl.replay();
        bodyControl.replay();
        faultControl.replay();

        boolean result = interceptor.handleRequest(soapMessageContext, null);
        assertFalse("Invalid response from interceptor", result);

        messageControl.verify();
        bodyControl.verify();
        faultControl.verify();
    }

    public void testHandleInvalidResponse() throws Exception {
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.createResponse();
        response.setPayload(new ClassPathResource("invalidMessage.xml", getClass()));
        boolean result = interceptor.handleResponse(messageContext, null);
        assertFalse("Invalid response from interceptor", result);
    }

    public void testHandleValidRequest() throws Exception {
        request.setPayload(new ClassPathResource("validMessage.xml", getClass()));
        boolean result = interceptor.handleRequest(messageContext, null);
        assertTrue("Invalid response from interceptor", result);
        assertFalse("Response set", messageContext.hasResponse());
    }

    public void testHandleValidResponse() throws Exception {
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.createResponse();
        response.setPayload(new ClassPathResource("validMessage.xml", getClass()));
        boolean result = interceptor.handleResponse(messageContext, null);
        assertTrue("Invalid response from interceptor", result);
    }

    public void testNamespacesInType() throws Exception {
        // Make sure we use Xerces for this testcase: the JAXP implementation used internally by JDK 1.5 has a bug
        // See http://opensource.atlassian.com/projects/spring/browse/SWS-35
        System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            interceptor.setSchema(new ClassPathResource("schema2.xsd", PayloadValidatingInterceptorTest.class));
            interceptor.afterPropertiesSet();
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage saajMessage =
                    SaajUtils.loadMessage(new ClassPathResource("validSoapMessage.xml", getClass()), messageFactory);
            SaajSoapMessageContext messageContext = new SaajSoapMessageContext(saajMessage, messageFactory);
            boolean result = interceptor.handleRequest(messageContext, null);
            assertTrue("Invalid response from interceptor", result);
            assertFalse("Response set", messageContext.hasResponse());
        }
        finally {
            // Reset the property
            System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI, "");
        }
    }

    public void testNonExistingSchema() throws Exception {
        interceptor.setSchema(new ClassPathResource("invalid"));
        try {
            interceptor.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }
}