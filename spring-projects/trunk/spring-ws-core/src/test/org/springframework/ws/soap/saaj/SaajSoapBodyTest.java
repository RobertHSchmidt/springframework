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

import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;

public class SaajSoapBodyTest extends TestCase {

    private SOAPBody saajBody;

    private SoapBody body;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        this.saajBody = saajMessage.getSOAPBody();
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(saajMessage);
        this.body = saajSoapMessage.getSoapBody();
    }

    public void testAddMustUnderstandFault() {
        SoapFault fault = body.addMustUnderstandFault(null);
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "MustUnderstand"),
                fault.getFaultCode());
        assertTrue("Fault not a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Sender fault", fault.isSenderFault());
        assertFalse("Fault is a Receiver fault", fault.isReceiverFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", saajBody.getPrefix() + ":MustUnderstand", saajFault.getFaultCode());
    }

    public void testAddSenderFault() {
        SoapFault fault = body.addSenderFault();
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"),
                fault.getFaultCode());
        assertTrue("Fault not a Sender fault", fault.isSenderFault());
        assertFalse("Fault is a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Receiver fault", fault.isReceiverFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", saajBody.getPrefix() + ":Client", saajFault.getFaultCode());
    }

    public void testAddReceiverFault() {
        SoapFault fault = body.addReceiverFault();
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"),
                fault.getFaultCode());
        assertTrue("Fault not a Receiver fault", fault.isReceiverFault());
        assertFalse("Fault is a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Sender fault", fault.isSenderFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", saajBody.getPrefix() + ":Server", saajFault.getFaultCode());
    }

    public void testAddFault() {
        QName faultCode = new QName("namespace", "localPart", "prefix");
        SoapFault fault = body.addFault(faultCode);
        fault.setFaultString("Fault", Locale.ENGLISH);
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", faultCode, fault.getFaultCode());
        assertEquals("Invalid fault string", "Fault", fault.getFaultString());
        assertEquals("Invalid fault local", Locale.ENGLISH, fault.getFaultStringLocale());
        assertFalse("Fault is a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Sender fault", fault.isSenderFault());
        assertFalse("Fault is a Receiver fault", fault.isReceiverFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", "prefix:localPart", saajFault.getFaultCode());
    }


}
