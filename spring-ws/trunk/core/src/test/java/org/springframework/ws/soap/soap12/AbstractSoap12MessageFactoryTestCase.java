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

package org.springframework.ws.soap.soap12;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.AbstractSoapMessageFactoryTestCase;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;

public abstract class AbstractSoap12MessageFactoryTestCase extends AbstractSoapMessageFactoryTestCase {

    public void testCreateEmptyMessage() throws Exception {
        WebServiceMessage message = messageFactory.createWebServiceMessage();
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, soapMessage.getVersion());
    }

    public void testCreateSoapMessageNoAttachment() throws Exception {
        InputStream is = AbstractSoap12MessageFactoryTestCase.class.getResourceAsStream("soap12.xml");
        final Properties headers = new Properties();
        headers.setProperty("Content-Type", "application/soap+xml");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, soapMessage.getVersion());
        assertFalse("Message a XOP pacakge", soapMessage.isXopPackage());
    }

    public void testCreateSoapMessageSwA() throws Exception {
        InputStream is = AbstractSoap12MessageFactoryTestCase.class.getResourceAsStream("soap12-attachment.bin");
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "multipart/related;" + "type=\"application/soap+xml\";" +
                "boundary=\"----=_Part_0_11416420.1149699787554\"");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, soapMessage.getVersion());
        assertFalse("Message a XOP pacakge", soapMessage.isXopPackage());
        Attachment attachment = soapMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }

    public void testCreateSoapMessageMtom() throws Exception {
        InputStream is = AbstractSoap12MessageFactoryTestCase.class.getResourceAsStream("soap12-mtom.bin");
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "multipart/related;" + "start-info=\"application/soap+xml\";" +
                "type=\"application/xop+xml\";" + "start=\"<0.urn:uuid:40864869929B855F971176851454456@apache.org>\";" +
                "boundary=\"MIMEBoundaryurn_uuid_40864869929B855F971176851454455\"");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, soapMessage.getVersion());
        assertTrue("Message not a XOP pacakge", soapMessage.isXopPackage());
        Iterator iter = soapMessage.getAttachments();
        assertTrue("No attachments read", iter.hasNext());

        Attachment attachment = soapMessage.getAttachment("1.urn:uuid:40864869929B855F971176851454452@apache.org");
        assertNotNull("No attachment read", attachment);
    }


}
