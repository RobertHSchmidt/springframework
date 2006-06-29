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

package org.springframework.ws.endpoint;

import nu.xom.Element;

public class XomPayloadEndpointTest extends AbstractPayloadEndpointTestCase {

    protected PayloadEndpoint createNoResponseEndpoint() throws Exception {
        return new AbstractXomPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement) throws Exception {
                return null;
            }
        };
    }

    protected PayloadEndpoint createResponseEndpoint() throws Exception {
        return new AbstractXomPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement) throws Exception {
                assertNotNull("No requestElement passed", requestElement);
                assertEquals("Invalid request element", REQUEST_ELEMENT, requestElement.getLocalName());
                assertEquals("Invalid request element", NAMESPACE_URI, requestElement.getNamespaceURI());
                return new Element(RESPONSE_ELEMENT, NAMESPACE_URI);
            }
        };
    }

    public void testStaxSourceEventReader() throws Exception {
        // Unfortutately, XOM does not support these, hence the override here
    }

    public void testStaxSourceStreamReader() throws Exception {
        // Unfortutately, XOM does not support these, hence the override here
    }
}
