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

package org.springframework.ws.transport.http;

import org.springframework.ws.transport.TransportException;

/**
 * Exception that is thrown when an error occurs in the HTTP transport.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
public class HttpTransportException extends TransportException {

    public HttpTransportException(String msg) {
        super(msg);
    }
}
