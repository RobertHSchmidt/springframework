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

package org.springframework.ws.soap;

import java.util.Locale;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSoapBodyTestCase extends XMLTestCase {

    protected SoapBody soapBody;

    protected Transformer transformer;

    protected final void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        soapBody = createSoapBody();
    }

    protected abstract SoapBody createSoapBody() throws Exception;

    public void testPayload() throws Exception {
        String payload = "<payload/>";
        StringSource contents = new StringSource(payload);
        transformer.transform(contents, soapBody.getPayloadResult());
        StringResult result = new StringResult();
        transformer.transform(soapBody.getPayloadSource(), result);
        assertXMLEqual("Invalid payload", payload, result.toString());
    }

    public void testNoFault() throws Exception {
        assertFalse("body has fault", soapBody.hasFault());
    }

    public void testAddFaultWithExistingPayload() throws Exception {
        StringSource contents = new StringSource("<payload/>");
        transformer.transform(contents, soapBody.getPayloadResult());
        soapBody.addMustUnderstandFault("faultString", Locale.ENGLISH);
        assertTrue("Body has no fault", soapBody.hasFault());
    }

}
