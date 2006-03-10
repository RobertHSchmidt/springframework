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

package org.springframework.ws.soap.saaj;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>SaajSoapMessageContext</code>.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.SOAPMessage
 */
public class SaajSoapMessage extends AbstractSoapMessage {

    private final SOAPMessage saajMessage;

    /**
     * Create a new <code>SaajSoapMessage</code> based on the given SAAJ <code>SOAPMessage</code>.
     *
     * @param soapMessage the SAAJ SOAPMessage
     */
    public SaajSoapMessage(SOAPMessage soapMessage) {
        this.saajMessage = soapMessage;
    }

    /**
     * Return the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public final SOAPMessage getSaajMessage() {
        return this.saajMessage;
    }

    public SoapEnvelope getEnvelope() {
        try {
            return new SaajSoapEnvelope(saajMessage.getSOAPPart().getEnvelope());
        }
        catch (SOAPException ex) {
            throw new SaajSoapEnvelopeException(ex);
        }
    }

    public String getSoapAction() {
        String[] values = saajMessage.getMimeHeaders().getHeader("SOAPAction");
        return (ObjectUtils.isEmpty(values)) ? null : values[0];
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            this.saajMessage.writeTo(outputStream);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageNotWritableException(ex);
        }
    }

    /**
     * SAAJ-Specific version of <code>org.springframework.ws.soap.SoapEnvelope</code>.
     */
    private static class SaajSoapEnvelope implements SoapEnvelope {

        private final SOAPEnvelope saajEnvelope;

        private SaajSoapEnvelope(SOAPEnvelope saajEnvelope) {
            this.saajEnvelope = saajEnvelope;
        }

        public SoapHeader getHeader() {
            try {
                return (saajEnvelope.getHeader() != null) ? new SaajSoapHeader(saajEnvelope.getHeader()) : null;
            }
            catch (SOAPException ex) {
                throw new SaajSoapHeaderException(ex);
            }
        }

        public SoapBody getBody() {
            try {
                return new SaajSoapBody(saajEnvelope.getBody());
            }
            catch (SOAPException ex) {
                throw new SaajSoapBodyException(ex);
            }
        }

        public QName getName() {
            return SaajUtils.toQName(saajEnvelope.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajEnvelope);
        }
    }

    /**
     * SAAJ-Specific version of <code>org.springframework.ws.soap.SoapHeader</code>.
     */
    private static class SaajSoapHeader implements SoapHeader {

        private final SOAPHeader saajHeader;

        private SaajSoapHeader(SOAPHeader saajHeader) {
            this.saajHeader = saajHeader;
        }

        private SOAPEnvelope getEnvelope() {
            return (SOAPEnvelope) saajHeader.getParentElement();
        }

        public SoapHeaderElement addHeaderElement(QName name) {
            try {
                Name saajName = SaajUtils.toName(name, getEnvelope());
                SOAPHeaderElement saajHeaderElement = saajHeader.addHeaderElement(saajName);
                return new SaajSoapHeaderElement(saajHeaderElement);
            }
            catch (SOAPException ex) {
                throw new SaajSoapHeaderException(ex);
            }
        }

        public Iterator examineMustUnderstandHeaderElements(String role) {
            return new SaajSoapHeaderElementIterator(saajHeader.examineMustUnderstandHeaderElements(role));
        }

        public QName getName() {
            return SaajUtils.toQName(saajHeader.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajHeader);
        }

        private static class SaajSoapHeaderElementIterator implements Iterator {

            private final Iterator saajIterator;

            private SaajSoapHeaderElementIterator(Iterator saajIterator) {
                this.saajIterator = saajIterator;
            }

            public boolean hasNext() {
                return saajIterator.hasNext();
            }

            public Object next() {
                SOAPHeaderElement saajHeaderElement = (SOAPHeaderElement) saajIterator.next();
                return new SaajSoapHeaderElement(saajHeaderElement);
            }

            public void remove() {
                saajIterator.remove();
            }
        }
    }

    /**
     * SAAJ-Specific version of <code>org.springframework.ws.soap.SoapHeaderElement</code>.
     */
    private static class SaajSoapHeaderElement implements SoapHeaderElement {

        private final SOAPHeaderElement saajHeaderElement;

        private SaajSoapHeaderElement(SOAPHeaderElement saajHeaderElement) {
            this.saajHeaderElement = saajHeaderElement;
        }

        public QName getName() {
            return SaajUtils.toQName(saajHeaderElement.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajHeaderElement);
        }

        public Result getResult() {
            return new DOMResult(saajHeaderElement);
        }

        public String getRole() {
            return saajHeaderElement.getActor();
        }

        public void setRole(String role) {
            saajHeaderElement.setActor(role);
        }

        public boolean getMustUnderstand() {
            return saajHeaderElement.getMustUnderstand();
        }

        public void setMustUnderstand(boolean mustUnderstand) {
            saajHeaderElement.setMustUnderstand(mustUnderstand);
        }
    }

    /**
     * SAAJ-specific implementation of <code>org.springframework.ws.soap.SoapBody</code>.
     */
    private static class SaajSoapBody implements SoapBody {

        private final SOAPBody saajBody;

        private SaajSoapBody(SOAPBody saajBody) {
            this.saajBody = saajBody;
        }

        private SOAPEnvelope getEnvelope() {
            return (SOAPEnvelope) saajBody.getParentElement();
        }

        public Source getPayloadSource() {
            SOAPBodyElement payloadElement = getPayloadElement();
            return (payloadElement != null) ? new DOMSource(payloadElement) : null;
        }

        /**
         * Retrieves the payload of the wrapped SAAJ message as a single DOM element. The payload of a message is the
         * contents of the SOAP body.
         *
         * @return the message payload, or <code>null</code> if none is set.
         */
        private SOAPBodyElement getPayloadElement() {
            for (Iterator iterator = saajBody.getChildElements(); iterator.hasNext();) {
                Object child = iterator.next();
                if (child instanceof SOAPBodyElement) {
                    return (SOAPBodyElement) child;
                }
            }
            return null;
        }

        public Result getPayloadResult() {
            return new DOMResult(saajBody);
        }

        public SoapFault addMustUnderstandFault(QName[] headers) {
            try {
                Name name = getEnvelope()
                        .createName(SaajSoapFault.MUST_UNDERSTAND, null, SOAPConstants.URI_NS_SOAP_ENVELOPE);
                return addFault(name, "Mandatory Header error.");
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public SoapFault addSenderFault(String faultString) {
            Assert.hasLength("faultString cannot be empty", faultString);
            try {
                Name name = getEnvelope().createName(SaajSoapFault.CLIENT, null, SOAPConstants.URI_NS_SOAP_ENVELOPE);
                return addFault(name, faultString);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public SoapFault addReceiverFault(String faultString) {
            Assert.hasLength("faultString cannot be empty", faultString);
            try {
                Name name = getEnvelope().createName(SaajSoapFault.SERVER, null, SOAPConstants.URI_NS_SOAP_ENVELOPE);
                return addFault(name, faultString);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public SoapFault addFault(QName faultCode, String faultString) {
            Assert.hasLength("faultString cannot be empty", faultString);
            if (!StringUtils.hasLength(faultCode.getNamespaceURI()) || (!StringUtils.hasLength(faultCode.getPrefix())))
            {
                throw new IllegalArgumentException("A fully qualified fault code (namespace, prefix, and local part) " +
                        "must be specific for a custom fault code");
            }
            try {
                Name name = SaajUtils.toName(faultCode, getEnvelope());
                return addFault(name, faultString);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        private SoapFault addFault(Name name, String faultString) throws SOAPException {
            for (Iterator iterator = saajBody.getChildElements(); iterator.hasNext();) {
                SOAPBodyElement bodyElement = (SOAPBodyElement) iterator.next();
                bodyElement.detachNode();
                bodyElement.recycleNode();
            }
            SOAPFault saajFault = saajBody.addFault(name, faultString);
            return new SaajSoapFault(saajFault);
        }

        public boolean hasFault() {
            return saajBody.hasFault();
        }

        public SoapFault getFault() {
            return new SaajSoapFault(saajBody.getFault());
        }

        public QName getName() {
            return SaajUtils.toQName(saajBody.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajBody);
        }
    }

    /**
     * SAAJ-specific implementation of <code>org.springframework.ws.soap.SoapFault</code>.
     */
    private static class SaajSoapFault implements SoapFault {

        private final SOAPFault saajFault;

        private static final String MUST_UNDERSTAND = "MustUnderstand";

        private static final String SERVER = "Server";

        private static final String CLIENT = "Client";

        private SaajSoapFault(SOAPFault saajFault) {
            this.saajFault = saajFault;
        }

        public QName getFaultCode() {
            return SaajUtils.toQName(saajFault.getFaultCodeAsName());
        }

        public String getFaultString() {
            return saajFault.getFaultString();
        }

        public void setFaultString(String faultString, Locale locale) {
            try {
                saajFault.setFaultString(faultString, locale);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public Locale getFaultStringLocale() {
            return saajFault.getFaultStringLocale();
        }

        public String getFaultRole() {
            return saajFault.getFaultActor();
        }

        public void setFaultRole(String role) {
            try {
                saajFault.setFaultActor(role);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public boolean isMustUnderstandFault() {
            return (MUST_UNDERSTAND.equals(saajFault.getFaultCodeAsName().getLocalName()) &&
                    SOAPConstants.URI_NS_SOAP_ENVELOPE.equals(saajFault.getFaultCodeAsName().getURI()));
        }

        public boolean isSenderFault() {
            return (CLIENT.equals(saajFault.getFaultCodeAsName().getLocalName()) &&
                    SOAPConstants.URI_NS_SOAP_ENVELOPE.equals(saajFault.getFaultCodeAsName().getURI()));
        }

        public boolean isReceiverFault() {
            return (SERVER.equals(saajFault.getFaultCodeAsName().getLocalName()) &&
                    SOAPConstants.URI_NS_SOAP_ENVELOPE.equals(saajFault.getFaultCodeAsName().getURI()));
        }

        public QName getName() {
            return SaajUtils.toQName(saajFault.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajFault);
        }
    }
}
