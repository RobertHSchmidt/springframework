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

import org.dom4j.Document;
import org.dom4j.Element;

public class Dom4JPayloadEndpointTest extends AbstractPayloadEndpointTestCase {

    protected PayloadEndpoint createResponseEndpoint() {
        return new AbstractDom4JPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement, Document responseDocument) throws Exception {
                assertNotNull("No requestElement passed", requestElement);
                assertNotNull("No responseDocument passed", responseDocument);
                assertEquals("Invalid request element", REQUEST_ELEMENT, requestElement.getName());
                assertEquals("Invalid request element", NAMESPACE_URI, requestElement.getNamespaceURI());
                return responseDocument.addElement(RESPONSE_ELEMENT, NAMESPACE_URI);
            }
        };
    }

    protected PayloadEndpoint createNoResponseEndpoint() throws Exception {
        return new AbstractDom4JPayloadEndpoint() {

            protected Element invokeInternal(Element requestElement, Document responseDocument) throws Exception {
                return null;
            }
        };
    }


}
