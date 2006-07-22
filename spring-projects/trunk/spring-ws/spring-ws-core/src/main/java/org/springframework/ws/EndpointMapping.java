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

package org.springframework.ws;

/**
 * Interface to be implemented by objects that define a mapping between message requests and endpoint objects.
 * <p/>
 * This class can be implemented by application developers, although this is not necessary, as
 * <code>PayloadRootQNameEndpointMapping</code> and <code>SoapActionEndpointMapping</code> are included.
 * <p/>
 * HandlerMapping implementations can support mapped interceptors but do not have to. A handler will always be wrapped
 * in a <code>EndpointExecutionChain</code> instance, optionally accompanied by some <code>EndpointInterceptor</code>
 * instances. The <code>MessageDispacher</code> will first call each <code>EndpointInterceptor</code>'s
 * <code>handlerRequest</code> method in the given order, finally invoking the endpoint itself if all
 * <code>handlerRequest</code> methods have returned <code>true</code>.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.endpoint.AbstractEndpointMapping
 * @see org.springframework.ws.soap.endpoint.PayloadRootQNameEndpointMapping
 * @see org.springframework.ws.soap.SoapActionEndpointMapping
 */
public interface EndpointMapping {

    /**
     * Returns an endpoint and any interceptors for this request. The choice may be made on message contents, a routing
     * table, or any factor the implementing class chooses.
     * <p/>
     * The returned <code>EndpointExecutionChain</code> contains a handler Object, rather than even a tag interface, so
     * that handlers are not constrained in any way. For example, a <code>EndpointAdapter</code> could be written to
     * allow another framework's handler objects to be used.
     * <p/>
     * Returns <code>null</code> if no match was found. This is not an error. The <code>MessageDispatcher</code> will
     * query all registered <code>EndpointMapping</code> beans to find a match, and only decide there is an error if
     * none can find a handler.
     *
     * @param request current HTTP request
     * @return a HandlerExecutionChain instance containing handler object and any interceptors, or <code>null</code>  if
     *         no mapping found
     * @throws Exception if there is an internal error
     */
    EndpointInvocationChain getEndpoint(WebServiceMessage request) throws Exception;

}
