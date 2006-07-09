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

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Defines the contract for Web service request that come in via a transport. Exposes headers and the inputstream to
 * read from.
 *
 * @author Arjen Poutsma
 */
public interface TransportRequest {

    /**
     * Returns an iteratoion over all the header names this request contains. Returns an empty <code>Iterator</code> if
     * the request has no headers, this method .
     */
    Iterator getHeaderNames();

    /**
     * Returns an iteration over all the string values of the specified request header. Returns an empty
     * <code>Iterator</code> if the request did not include any headers of the specified name.
     */
    Iterator getHeaders(String name);

    /**
     * Returns the contents of the request as a <code>InputStream</code>.
     */
    InputStream getInputStream() throws IOException;
}
