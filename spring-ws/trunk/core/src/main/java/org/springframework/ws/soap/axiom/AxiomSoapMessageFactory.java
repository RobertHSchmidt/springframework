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
import java.util.Iterator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportInputStream;

/**
 * Axiom-specific implementation of the {@link org.springframework.ws.WebServiceMessageFactory WebServiceMessageFactory}
 * interface. Creates {@link org.springframework.ws.soap.axiom.AxiomSoapMessage AxiomSoapMessages}.
 * <p/>
 * To increase reading performance on the the SOAP request created by this message context factory, you can set the
 * <code>payloadCaching</code> property to <code>false</code> (default is <code>true</code>). This this will read the
 * contents of the body directly from the stream. However, <strong>when this setting is enabled, the payload can only be
 * read once</strong>. This means that any endpoint mappings or interceptors which are based on the message payload
 * (such as the <code>PayloadRootQNameEndpointMapping</code>, the <code>PayloadValidatingInterceptor</code>, or the
 * <code>PayloadLoggingInterceptor</code>) cannot be used. Instead, use an endpoint mapping that does not consume the
 * payload (i.e. the <code>SoapActionEndpointMapping</code>).
 * <p/>
 * Mostly derived from <code>org.apache.axis2.transport.http.HTTPTransportUtils</code> and
 * <code>org.apache.axis2.transport.TransportUtils</code>, which we cannot use since they are not part of the Axiom
 * distribution.
 *
 * @author Arjen Poutsma
 * @see AxiomSoapMessage
 * @see #setPayloadCaching(boolean)
 * @since 1.0
 */
public class AxiomSoapMessageFactory implements SoapMessageFactory, InitializingBean {

    private static final String CHAR_SET_ENCODING = "charset";

    private static final String DEFAULT_CHAR_SET_ENCODING = "UTF-8";

    private static final String MULTI_PART_RELATED_CONTENT_TYPE = "multipart/related";

    private static final Log logger = LogFactory.getLog(AxiomSoapMessageFactory.class);

    private XMLInputFactory inputFactory;

    private boolean payloadCaching = true;

    // use SOAP 1.1 by default
    private SOAPFactory soapFactory = new SOAP11Factory();

    /** Default constructor. */
    public AxiomSoapMessageFactory() {
        inputFactory = XMLInputFactory.newInstance();
    }

    /**
     * Indicates whether the SOAP Body payload should be cached or not. Default is <code>true</code>. Setting this to
     * <code>false</code> will increase performance, but also result in the fact that the message payload can only be
     * read once.
     */
    public void setPayloadCaching(boolean payloadCaching) {
        this.payloadCaching = payloadCaching;
    }

    public void setSoapVersion(SoapVersion version) {
        if (SoapVersion.SOAP_11 == version) {
            soapFactory = new SOAP11Factory();
        }
        else if (SoapVersion.SOAP_12 == version) {
            soapFactory = new SOAP12Factory();
        }
        else {
            throw new IllegalArgumentException(
                    "Invalid version [" + version + "]. " + "Expected the SOAP_11 or SOAP_12 constant");
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(payloadCaching ? "Enabled payload caching" : "Disabled payload caching");
        }
    }

    public WebServiceMessage createWebServiceMessage() {
        return new AxiomSoapMessage(soapFactory);
    }

    public WebServiceMessage createWebServiceMessage(InputStream inputStream) throws IOException {
        if (!(inputStream instanceof TransportInputStream)) {
            throw new IllegalArgumentException("AxiomSoapMessageFactory requires a TransportInputStream");
        }
        TransportInputStream transportInputStream = (TransportInputStream) inputStream;
        String contentType = getHeaderValue(transportInputStream, TransportConstants.HEADER_CONTENT_TYPE);
        if (!StringUtils.hasLength(contentType)) {
            throw new IllegalArgumentException("TransportInputStream contains no Content-Type header");
        }
        String soapAction = getHeaderValue(transportInputStream, TransportConstants.HEADER_SOAP_ACTION);
        try {
            if (isMultiPartRelated(contentType)) {
                return createMultiPartAxiomSoapMessage(inputStream, contentType, soapAction);
            }
            else {
                return createAxiomSoapMessage(inputStream, contentType, soapAction);
            }
        }
        catch (XMLStreamException ex) {
            throw new AxiomSoapMessageCreationException("Could not parse request: " + ex.getMessage(), ex);
        }
        catch (OMException ex) {
            throw new AxiomSoapMessageCreationException("Could not create message: " + ex.getMessage(), ex);
        }
    }

    private String getHeaderValue(TransportInputStream transportInputStream, String header) throws IOException {
        String contentType = null;
        Iterator iterator = transportInputStream.getHeaders(header);
        if (iterator.hasNext()) {
            contentType = (String) iterator.next();
        }
        return contentType;
    }

    private boolean isMultiPartRelated(String contentType) {
        return contentType.indexOf(MULTI_PART_RELATED_CONTENT_TYPE) != -1;
    }

    /** Creates an AxiomSoapMessage without attachments. */
    private WebServiceMessage createAxiomSoapMessage(InputStream inputStream, String contentType, String soapAction)
            throws XMLStreamException {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream, getCharSetEncoding(contentType));
        String envelopeNamespace = getSoapEnvelopeNamespace(contentType);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, soapFactory, envelopeNamespace);
        SOAPMessage soapMessage = builder.getSoapMessage();
        return new AxiomSoapMessage(soapMessage, soapAction, payloadCaching);
    }

    /** Creates an AxiomSoapMessage with attachments. */
    private AxiomSoapMessage createMultiPartAxiomSoapMessage(InputStream inputStream,
                                                             String contentType,
                                                             String soapAction) throws XMLStreamException {
        Attachments attachments = new Attachments(inputStream, contentType);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(attachments.getSOAPPartInputStream(),
                getCharSetEncoding(attachments.getSOAPPartContentType()));
        StAXSOAPModelBuilder builder = null;
        String envelopeNamespace = getSoapEnvelopeNamespace(contentType);
        if (MTOMConstants.SWA_TYPE.equals(attachments.getAttachmentSpecType()) ||
                MTOMConstants.SWA_TYPE_12.equals(attachments.getAttachmentSpecType())) {
            builder = new StAXSOAPModelBuilder(reader, soapFactory, envelopeNamespace);
        }
        else if (MTOMConstants.MTOM_TYPE.equals(attachments.getAttachmentSpecType())) {
            builder = new MTOMStAXSOAPModelBuilder(reader, attachments, envelopeNamespace);
        }
        else {
            throw new AxiomSoapMessageCreationException(
                    "Unknown attachment type: [" + attachments.getAttachmentSpecType() + "]");
        }
        return new AxiomSoapMessage(builder.getSoapMessage(), attachments, soapAction, payloadCaching);
    }

    private String getSoapEnvelopeNamespace(String contentType) {
        if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) != -1) {
            return SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
        else if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) != -1) {
            return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
        else {
            throw new AxiomSoapMessageCreationException("Unknown content type '" + contentType + "'");
        }

    }

    /**
     * Returns the character set from the given content type. Mostly copied
     *
     * @return the character set encoding
     */
    protected String getCharSetEncoding(String contentType) {
        int index = contentType.indexOf(CHAR_SET_ENCODING);
        if (index == -1) {
            return DEFAULT_CHAR_SET_ENCODING;
        }
        int idx = contentType.indexOf("=", index);

        int indexOfSemiColon = contentType.indexOf(";", idx);
        String value;

        if (indexOfSemiColon > 0) {
            value = contentType.substring(idx + 1, indexOfSemiColon);
        }
        else {
            value = contentType.substring(idx + 1, contentType.length()).trim();
        }
        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        if ("null".equalsIgnoreCase(value)) {
            return DEFAULT_CHAR_SET_ENCODING;
        }
        else {
            return value.trim();
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("AxiomSoapMessageFactory[");
        if (soapFactory instanceof SOAP11Factory) {
            buffer.append("SOAP 1.1");
        }
        else if (soapFactory instanceof SOAP12Factory) {
            buffer.append("SOAP 1.2");
        }
        buffer.append(']');
        return buffer.toString();
    }
}
