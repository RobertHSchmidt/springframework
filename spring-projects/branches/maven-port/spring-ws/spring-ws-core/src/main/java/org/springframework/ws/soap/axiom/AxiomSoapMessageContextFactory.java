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

package org.springframework.ws.soap.axiom;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.SoapMessageCreationException;

/**
 * SAAJ-specific implementation of the <code>MessageContextFactory</code> interface. Creates a
 * <code>AxiomSoapMessageContext</code>.
 * <p/>
 * Mostly copied from <code>org.apache.axis2.transport.http.HTTPTransportUtils</code> and
 * <code>org.apache.axis2.transport.TransportUtils</code>, since they are not part of the AXIOM distribution.
 *
 * @author Arjen Poutsma
 * @see AxiomSoapMessageContext
 */
public class AxiomSoapMessageContextFactory implements MessageContextFactory, InitializingBean {

    private static final String CHAR_SET_ENCODING = "charset";

    private static final String DEFAULT_CHAR_SET_ENCODING = "UTF-8";

    private static final String SOAP_ACTION_HEADER = "SOAPAction";

    private XMLInputFactory inputFactory;

    public void afterPropertiesSet() throws Exception {
        inputFactory = createXmlInputFactory();
    }

    public MessageContext createContext(HttpServletRequest httpRequest) throws IOException {
        Assert.isTrue("POST".equals(httpRequest.getMethod()), "HttpServletRequest does not have POST method");
        String contentType = httpRequest.getContentType();
        InputStream inputStream = httpRequest.getInputStream();
        String soapAction = httpRequest.getHeader(SOAP_ACTION_HEADER);
        try {
            if (contentType == null) {
                throw new SoapMessageCreationException("No content type set on HttpServletRequest");
            }
            String charSetEncoding = getCharSetEncoding(contentType);
            if (charSetEncoding == null) {
                charSetEncoding = DEFAULT_CHAR_SET_ENCODING;
            }
            XMLStreamReader xmlreader = inputFactory.createXMLStreamReader(inputStream, charSetEncoding);
            StAXSOAPModelBuilder builder;
            SOAPFactory soapFactory;
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) != -1) {
                soapFactory = new SOAP12Factory();
                builder = new StAXSOAPModelBuilder(xmlreader, soapFactory, SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            }
            else if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) != -1) {
                soapFactory = new SOAP11Factory();
                builder = new StAXSOAPModelBuilder(xmlreader, soapFactory, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            }
            else {
                throw new SoapMessageCreationException("Unknown content type '" + contentType + "'");
            }
            AxiomSoapMessage request = new AxiomSoapMessage(builder.getSoapMessage(), soapFactory, soapAction);
            return new AxiomSoapMessageContext(request, soapFactory);
        }
        catch (XMLStreamException ex) {
            throw new SoapMessageCreationException("Could not create message: " + ex.toString(), ex);
        }
        catch (OMException ex) {
            throw new SoapMessageCreationException("Could not create message: " + ex.toString(), ex);
        }
    }

    /**
     * Create a <code>XMLInputFactory</code> that this context factory will use to create <code>XMLStreamReader</code>s.
     * Can be overridden in subclasses, adding further initialization of the factory. The resulting
     * <code>XMLInputFactory</code> is cached, so this method will only be called once.
     *
     * @return the created <code>XMLInputFactory</code>
     */
    protected XMLInputFactory createXmlInputFactory() {
        return XMLInputFactory.newInstance();
    }

    /**
     * Returns the character set from the given content type. Mostly copied
     *
     * @param contentType
     * @return the character set encoding
     */
    protected String getCharSetEncoding(String contentType) {
        int index = contentType.indexOf(CHAR_SET_ENCODING);
        if (index == -1) {
            return DEFAULT_CHAR_SET_ENCODING;
        }
        int indexOfEq = contentType.indexOf("=", index);

        int indexOfSemiColon = contentType.indexOf(";", indexOfEq);
        String value;

        if (indexOfSemiColon > 0) {
            value = (contentType.substring(indexOfEq + 1, indexOfSemiColon));
        }
        else {
            value = (contentType.substring(indexOfEq + 1, contentType.length())).trim();
        }
        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }
        else {
            return value.trim();
        }
    }
}
