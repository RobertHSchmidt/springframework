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

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;
import org.springframework.ws.transport.TransportResponse;

/**
 * AXIOM-specific implementation of the <code>SoapMessageContext</code> interface. Created by the
 * <code>AxiomSoapMessageContextFactory</code>.
 *
 * @author Arjen Poutsma
 * @see AxiomSoapMessageContextFactory
 */
public class AxiomSoapMessageContext extends AbstractSoapMessageContext {

    private final SOAPFactory soapFactory;

    /**
     * Creates a new instance based on the given Axiom request message, and a SOAP factory.
     *
     * @param request     the request message
     * @param soapFactory the SOAP factory used for creating a response
     */
    public AxiomSoapMessageContext(AxiomSoapMessage request, SOAPFactory soapFactory) {
        super(request);
        Assert.notNull(soapFactory, "No soapFactory given");
        this.soapFactory = soapFactory;
    }

    protected SoapMessage createSoapMessage() {
        return new AxiomSoapMessage(soapFactory);
    }

    /**
     * Returns the request as an Axiom SOAP message.
     */
    public SOAPMessage getAxiomRequest() {
        return ((AxiomSoapMessage) getSoapRequest()).getAxiomMessage();
    }

    /**
     * Returns the response as an Axiom SOAP message.
     */
    public SOAPMessage getAxiomResponse() {
        return ((AxiomSoapMessage) getSoapResponse()).getAxiomMessage();
    }

    public void sendResponse(TransportResponse transportResponse) throws IOException {
        if (hasResponse()) {
            AxiomSoapMessage response = (AxiomSoapMessage) getSoapResponse();
            String contentType = response.getVersion().getContentType();
            SOAPMessage axiomResponse = response.getAxiomMessage();
            contentType += "; charset=\"" + axiomResponse.getCharsetEncoding() + "\"";
            transportResponse.addHeader("Content-Type", contentType);
            response.writeTo(transportResponse.getOutputStream());
        }
    }
}
