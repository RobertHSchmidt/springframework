/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.transport.mail;

import java.util.Properties;
import javax.jms.BytesMessage;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.mail.URLName;

import junit.framework.TestCase;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;

public class MailMessageSenderIntegrationTest extends TestCase {

    private MailMessageSender messageSender;

    private MessageFactory messageFactory;

    private static final String URI = "mailto:ajwpi21@xs4all.nl?subject=SOAP Test";

    private static final String SOAP_ACTION = "http://springframework.org/DoIt";

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "smtp.xs4all.nl");
        messageSender = new MailMessageSender();
        messageSender.setStoreUri("pop3://ajwpi21:sjantaL.@pop.xs4all.nl/INBOX");
        messageSender.setTransportUri("smtp://ajwpi21:sjantaL.@smtp.xs4all.nl");
        messageSender.setFrom("Arjen Poutsma <ajwp@xs4all.nl>");
        messageSender.setJavaMailProperties(properties);
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
    }

    public void testSendAndReceiveQueueNoResponse() throws Exception {
        WebServiceConnection connection = null;
        try {
            connection = messageSender.createConnection(URI);
            SOAPMessage saajMessage = messageFactory.createMessage();
            SoapMessage soapRequest = new SaajSoapMessage(saajMessage);
            soapRequest.setSoapAction(SOAP_ACTION);
            connection.send(soapRequest);
//            SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

}