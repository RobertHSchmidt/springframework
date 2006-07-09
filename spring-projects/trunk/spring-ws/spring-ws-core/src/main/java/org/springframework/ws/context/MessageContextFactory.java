/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.context;

import java.io.IOException;

import org.springframework.ws.transport.TransportRequest;

/**
 * The <code>MessageContextFactory</code> serves as factory for <code>MessageContext</code>s. Allows creation of
 * contexts based on <code>TransportRequest</code>.
 *
 * @author Arjen Poutsma
 */
public interface MessageContextFactory {

    /**
     * Creates a <code>MessageContext</code> based on the given transport request. Implementations use the request's
     * input stream to create a request message, and possibly copy the request headers to the message.
     *
     * @param request the incoming request
     * @return the created message context
     * @throws IOException if an I/O exception occurs
     */
    MessageContext createContext(TransportRequest request) throws IOException;

}
