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

package org.springframework.ws.soap.saaj;

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>Soap12Body</code> interface. Used by
 * <code>Saaj13SoapEnvelope</code>.
 *
 * @author Arjen Poutsma
 */
class Saaj13Soap12Body implements Soap12Body {

    private final SOAPBody saajBody;

    Saaj13Soap12Body(SOAPBody saajBody) {
        Assert.notNull(saajBody, "No saajBody given");
        this.saajBody = saajBody;
    }

    public Source getPayloadSource() {
        SOAPBodyElement payloadElement = getPayloadElement();
        return payloadElement != null ? new DOMSource(payloadElement) : null;
    }

    public Result getPayloadResult() {
        saajBody.removeContents();
        return new DOMResult(saajBody);
    }

    private Soap12Fault addFault(QName code, String reason, Locale locale) {
        Assert.notNull(code, "No code given");
        Assert.hasLength(reason, "reason cannot be empty");
        Assert.notNull(locale, "No locale given");
        if (!StringUtils.hasLength(code.getNamespaceURI())) {
            throw new IllegalArgumentException(
                    "A code with namespace and local part must be specific for a custom fault code");
        }
        try {
            saajBody.removeContents();
            SOAPFault saajFault = saajBody.addFault(code, reason, locale);
            return new Saaj13Soap12Fault(saajFault);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public SoapFault addMustUnderstandFault(String reason, Locale locale) {
        return addFault(SOAPConstants.SOAP_MUSTUNDERSTAND_FAULT, reason, locale);
    }

    public SoapFault addClientOrSenderFault(String reason, Locale locale) {
        return addFault(SOAPConstants.SOAP_SENDER_FAULT, reason, locale);
    }

    public SoapFault addServerOrReceiverFault(String reason, Locale reasonLocale) {
        return addFault(SOAPConstants.SOAP_RECEIVER_FAULT, reason, reasonLocale);
    }

    public SoapFault addVersionMismatchFault(String reason, Locale locale) {
        return addFault(SOAPConstants.SOAP_VERSIONMISMATCH_FAULT, reason, locale);
    }

    public Soap12Fault addDataEncodingUnknownFault(QName[] subcodes, String reason, Locale reasonLocale) {
        return addFault(SOAPConstants.SOAP_DATAENCODINGUNKNOWN_FAULT, reason, reasonLocale);
    }

    public boolean hasFault() {
        return saajBody.hasFault();
    }

    public SoapFault getFault() {
        return new Saaj13Soap12Fault(saajBody.getFault());
    }

    public QName getName() {
        return saajBody.getElementQName();
    }

    public Source getSource() {
        return new DOMSource(saajBody);
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

}
