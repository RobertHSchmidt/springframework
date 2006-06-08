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
import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageContext;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

public abstract class XwssMessageProcessorTestCase extends TestCase {

    protected XwsSecurityInterceptor interceptor;

    private MessageFactory messageFactory;

    private Map namespaces;

    protected void assertXpathEvaluatesTo(String message,
                                          String expectedValue,
                                          String xpathExpression,
                                          SoapMessage soapMessage) {
        XPathExpression expression = XPathExpressionFactory.createXPathExpression(xpathExpression, namespaces);
        Document document = ((SaajSoapMessage) soapMessage).getSaajMessage().getSOAPPart();
        String actualValue = expression.evaluateAsString(document);
        assertEquals(message, expectedValue, actualValue);
    }

    protected void assertXpathExists(String message, String xpathExpression, SoapMessage soapMessage) {
        XPathExpression expression = XPathExpressionFactory.createXPathExpression(xpathExpression, namespaces);
        Document document = ((SaajSoapMessage) soapMessage).getSaajMessage().getSOAPPart();
        Node node = expression.evaluateAsNode(document);
        assertNotNull(message, node);
    }

    protected void assertXpathNotExists(String message, String xpathExpression, SoapMessage soapMessage) {
        XPathExpression expression = XPathExpressionFactory.createXPathExpression(xpathExpression, namespaces);
        Document document = ((SaajSoapMessage) soapMessage).getSaajMessage().getSOAPPart();
        Node node = expression.evaluateAsNode(document);
        assertNull(message, node);
    }

    protected SaajSoapMessageContext loadSoapMessageResponseContext(String fileName) throws IOException, SOAPException {
        SOAPMessage request = messageFactory.createMessage();
        SaajSoapMessageContext messageContext = new SaajSoapMessageContext(request, messageFactory);
        messageContext.setSaajResponse(loadSaajMessage(fileName));
        return messageContext;
    }

    private SOAPMessage loadSaajMessage(String fileName) throws SOAPException, IOException {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml");
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(fileName);
            return messageFactory.createMessage(mimeHeaders, is);
        }
        finally {
            if (is != null) {
                is.close();
            }
        }

    }

    protected SaajSoapMessageContext loadSoapMessageRequestContext(String fileName) throws IOException, SOAPException {
        SOAPMessage request = loadSaajMessage(fileName);
        return new SaajSoapMessageContext(request, messageFactory);
    }

    protected final void setUp() throws Exception {
        interceptor = new XwsSecurityInterceptor();
        messageFactory = MessageFactory.newInstance();
        namespaces = new HashMap();
        namespaces.put("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        namespaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
        namespaces.put("xenc", "http://www.w3.org/2001/04/xmlenc#");
        onSetup();
    }

    protected void onSetup() throws Exception {
    }
}
