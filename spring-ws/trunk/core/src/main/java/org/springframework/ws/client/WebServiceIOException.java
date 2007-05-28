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

package org.springframework.ws.client;

import java.io.IOException;

/**
 * Exception thrown whenever an I/O error occurs on the client-side.
 *
 * @author Arjen Poutsma
 */
public class WebServiceIOException extends WebServiceClientException {

    /**
     * Create a new instance of the <code>WebServiceIOException</code> class.
     * @param msg the detail message
     */
    public WebServiceIOException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of the <code>WebServiceIOException</code> class.
     * @param msg the detail message
     * @param ex  the root {@link IOException}
     */
    public WebServiceIOException(String msg, IOException ex) {
        super(msg, ex);
    }

}
