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

package org.springframework.ws.soap.soap11;

import java.io.ByteArrayOutputStream;

import junit.framework.Assert;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.soap.AbstractSoapMessageTestCase;
import org.springframework.ws.soap.SoapVersion;

/**
 * @author Arjen Poutsma
 */
public abstract class AbstractSoap11MessageTestCase extends AbstractSoapMessageTestCase {

    public void testGetVersion() throws Exception {
        Assert.assertEquals("Invalid SOAP version", SoapVersion.SOAP_11, soapMessage.getVersion());
    }

    public void testWriteToOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        soapMessage.writeTo(outputStream);
        assertXMLEqual("<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body/></Envelope>",
                new String(outputStream.toByteArray(), "UTF-8"));
    }

    public void testWriteToTransportResponse() throws Exception {
        MockTransportResponse response = new MockTransportResponse();
        soapMessage.writeTo(response);
        assertXMLEqual("<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body/></Envelope>",
                response.getContents());
        assertTrue("Invalid Content-Type set",
                response.getHeaders().getProperty("Content-Type").indexOf(SoapVersion.SOAP_11.getContentType()) != -1);
    }

    public void testWriteToTransportResponseAttachment() throws Exception {
        InputStreamSource inputStreamSource = new ByteArrayResource("contents".getBytes("UTF-8"));
        soapMessage.addAttachment(inputStreamSource, "text/plain");
        MockTransportResponse response = new MockTransportResponse();
        soapMessage.writeTo(response);
        assertTrue("Invalid Content-Type set",
                response.getHeaders().getProperty("Content-Type").indexOf("multipart/related") != -1);
        assertTrue("Invalid Content-Type set",
                response.getHeaders().getProperty("Content-Type").indexOf(SoapVersion.SOAP_11.getContentType()) != -1);
    }

}
