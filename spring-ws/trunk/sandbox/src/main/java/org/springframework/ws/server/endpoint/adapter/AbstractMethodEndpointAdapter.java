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

package org.springframework.ws.server.endpoint.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for {@link EndpointAdapter} implementations that support {@link
 * org.springframework.ws.server.endpoint.MethodEndpoint}s. Contains template methods for handling method endpoints.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractMethodEndpointAdapter extends TransformerObjectSupport implements EndpointAdapter {

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Delegates to {@link #supportsInternal(org.springframework.ws.server.endpoint.MethodEndpoint)}.
     *
     * @param endpoint endpoint object to check
     * @return whether or not this adapter can adapt the given endpoint
     */
    public final boolean supports(Object endpoint) {
        return endpoint instanceof MethodEndpoint && supportsInternal((MethodEndpoint) endpoint);
    }

    /**
     * Delegates to {@link #invokeInternal(org.springframework.ws.context.MessageContext,MethodEndpoint)}.
     *
     * @param messageContext the current message context
     * @param endpoint       the endpoint to use. This object must have previously been passed to the
     *                       <code>supportsInternal</code> method of this interface, which must have returned
     *                       <code>true</code>
     * @throws Exception in case of errors
     */
    public final void invoke(MessageContext messageContext, Object endpoint) throws Exception {
        invokeInternal(messageContext, (MethodEndpoint) endpoint);
    }

    /**
     * Given a method endpoint, return whether or not this adapter can support it.
     *
     * @param methodEndpoint method endpoint to check
     * @return whether or not this adapter can adapt the given method
     */
    protected abstract boolean supportsInternal(MethodEndpoint methodEndpoint);

    /**
     * Use the given method endpoint to handle the request.
     *
     * @param messageContext the current message context
     * @param methodEndpoint the method endpoint to use
     * @throws Exception in case of errors
     */
    protected abstract void invokeInternal(MessageContext messageContext, MethodEndpoint methodEndpoint)
            throws Exception;

}
