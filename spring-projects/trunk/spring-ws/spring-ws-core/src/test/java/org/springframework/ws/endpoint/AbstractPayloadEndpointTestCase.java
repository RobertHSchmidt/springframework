/*
 * Copyright (c) 2006, Your Corporation. All Rights Reserved.
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

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractPayloadEndpointTestCase extends AbstractEndpointTestCase {

    private PayloadEndpoint endpoint;

    private Transformer transformer;

    protected final void setUp() throws Exception {
        endpoint = createResponseEndpoint();
        transformer = TransformerFactory.newInstance().newTransformer();
    }

    public void testNoResponse() throws Exception {
        endpoint = createNoResponseEndpoint();
        StringSource requestSource = new StringSource(REQUEST);
        Source resultSource = endpoint.invoke(requestSource);
        assertNull("Response source returned", resultSource);
    }

    protected final void testSource(Source requestSource) throws Exception {
        Source responseSource = endpoint.invoke(requestSource);
        assertNotNull("No response source returned", responseSource);
        StringResult result = new StringResult();
        transformer.transform(responseSource, result);
        assertXMLEqual(RESPONSE, result.toString());
    }

    protected abstract PayloadEndpoint createNoResponseEndpoint() throws Exception;

    protected abstract PayloadEndpoint createResponseEndpoint() throws Exception;
}
