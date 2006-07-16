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

package org.springframework.ws.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import org.springframework.ws.transport.TransportRequest;

/**
 * Mock implementation of the <code>TransportRequest</code> interface.
 *
 * @author Arjen Poutsma
 */
public class MockTransportRequest implements TransportRequest {

    private Properties headers;

    private byte[] contents;

    public MockTransportRequest() {
        headers = new Properties();
        contents = new byte[0];
    }

    public MockTransportRequest(Properties headers, byte[] contents) {
        this.headers = headers;
        this.contents = contents;
    }

    public Iterator getHeaderNames() {
        return headers.keySet().iterator();
    }

    public Iterator getHeaders(String name) {
        String value = headers.getProperty(name);
        return value != null ? Collections.singletonList(value).iterator() : Collections.EMPTY_LIST.iterator();
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(contents);
    }
}
