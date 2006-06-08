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
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.MimeHeaders;

import org.springframework.ws.soap.security.WsSecurityInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.SoapMessage;

public class IntegrationTest extends org.springframework.test.AbstractDependencyInjectionSpringContextTests {

    private XwssMessageProcessor processor;

    private MessageFactory messageFactory;

    public void setProcessor(XwssMessageProcessor processor) {
        this.processor = processor;
    }

    protected void onSetUp() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }

    protected String[] getConfigLocations() {
        return new String[]{"classpath:org/springframework/ws/soap/security/xwss/applicationContext.xml"};
    }

    protected SoapMessage loadSoapMessage(String fileName) throws IOException, SOAPException {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml");
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(fileName);
            SOAPMessage saajMessage = messageFactory.createMessage(mimeHeaders, is);
            return new SaajSoapMessage(saajMessage);
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }
    public void testSecure() throws Exception {
        SoapMessage message = loadSoapMessage("signed-soap.xml");
        SoapMessage result = processor.validateMessage(message);
        assertNotNull(result);
        result.writeTo(System.out);

    }
}
