/*
 * Copyright 2005, 2006 the original author or authors.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.endpoint.MessageEndpointAdapter;
import org.springframework.ws.endpoint.PayloadEndpointAdapter;

/**
 * Central dispatcher for use withing Spring-WS. Dispatches Web service messages to registered endoints.
 * <p/>
 * This dispatcher is quite similar to Spring MVCs <code>DispatcherServlet</code>. Just like it's counterpart, this
 * dispatcher is very flexible. <ul> <li>It can use any <code>EndpointMapping</code> implementation - whether standard,
 * or provided as part of an application - to control the routing of request messages to endpoint objects. Endpoint
 * mappings can be registerd using the <code>endpointMappings</code> property.</li> <li>It can use any
 * <code>EndpointAdapter</code>; this allows to use any endpoint interface or form. Default are
 * <code>MessageEndpointAdapter</code> and <code>PayloadEndpointAdapter</code>, for <code>MessageEndpoint</code> and
 * <code>PayloadEndpoint</code>, respectively. Additional endpoint adapters can be added through the
 * <code>endpointAdapters</code> property.</li> <li>Its exception resolution strategy can be specified via a
 * <code>EndpointExceptionResolver</code>, for example mapping certain exceptions to SOAP Faults. Default is none.
 * Additional exception resolvers can be added through the <code>endpointExceptionResolvers</code> property.</li> </ul>
 * A web application can use any number of <code>MessageDispatcher</code>s.</b> Since a <code>MessageDispatcher</code>
 * also implements <code>MessageEndpoint</code>, it is also possible to chain them: one dispatcher can be registered as
 * the endpoint of another, though the <code>MessageEndpointAdapter</code>.
 *
 * @author Arjen Poutsma
 * @see EndpointMapping
 * @see EndpointAdapter
 * @see EndpointExceptionResolver
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public class MessageDispatcher implements MessageEndpoint, BeanNameAware {

    /**
     * Log category to use when no mapped endpoint is found for a request.
     */
    public static final String ENDPOINT_NOT_FOUND_LOG_CATEGORY = "org.springframework.ws.EndpointNotFound";

    /**
     * Additional logger to use when no mapped endpoint is found for a request.
     */
    protected static final Log endpointNotFoundLogger = LogFactory.getLog(ENDPOINT_NOT_FOUND_LOG_CATEGORY);

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * The registered bean name for this dispatcher.
     */
    private String beanName;

    /**
     * List of EndpointAdapters used in this dispatcher
     */
    private List endpointAdapters;

    /**
     * List of EndpointExceptionResolvers used in this dispatcher
     */
    private List endpointExceptionResolvers;

    /**
     * List of EndpointMappings used in this dispatcher
     */
    private List endpointMappings;

    /**
     * Initializes a new instance of the <code>MessageDispatcher</code>.
     */
    public MessageDispatcher() {
        initDefaultStrategies();
    }

    /**
     * Returns the <code>EndpointAdapter</code>s to use by this <code>MessageDispatcher</code>.
     */
    public List getEndpointAdapters() {
        return endpointAdapters;
    }

    /**
     * Sets the <code>EndpointAdapter</code>s to use by this <code>MessageDispatcher</code>.
     */
    public void setEndpointAdapters(List endpointAdapters) {
        this.endpointAdapters = endpointAdapters;
    }

    /**
     * Returns the <code>EndpointExceptionResolver</code>s to use by this <code>MessageDispatcher</code>.
     */
    public List getEndpointExceptionResolvers() {
        return endpointExceptionResolvers;
    }

    /**
     * Sets the <code>EndpointExceptionResolver</code>s to use by this <code>MessageDispatcher</code>.
     */
    public void setEndpointExceptionResolvers(List endpointExceptionResolvers) {
        this.endpointExceptionResolvers = endpointExceptionResolvers;
    }

    /**
     * Returns the <code>EndpointMapping</code>s to use by this <code>MessageDispatcher</code>.
     */
    public List getEndpointMappings() {
        return endpointMappings;
    }

    /**
     * Sets the <code>EndpointMapping</code>s to use by this <code>MessageDispatcher</code>.
     */
    public void setEndpointMappings(List endpointMappings) {
        this.endpointMappings = endpointMappings;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void invoke(MessageContext messageContext) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("MessageDispatcher with name '" + beanName + "' received request [" +
                    messageContext.getRequest() + "]");
        }
        dispatch(messageContext);
        if (logger.isDebugEnabled() && messageContext.hasResponse()) {
            logger.debug("MessageDispatcher with name '" + beanName + "' sends response [" +
                    messageContext.getResponse() + "]");
        }
    }

    /**
     * Dispatches the request in the given MessageContext according to the configuration.
     *
     * @param messageContext the message context
     * @throws NoEndpointFoundException thrown when an endpoint cannot be resolved for the incoming message
     */
    protected final void dispatch(MessageContext messageContext) throws Exception {
        EndpointInvocationChain mappedEndpoint = null;
        int interceptorIndex = -1;
        try {
            // Determine endpoint for the current context
            mappedEndpoint = getEndpoint(messageContext);
            if (mappedEndpoint == null || mappedEndpoint.getEndpoint() == null) {
                throw new NoEndpointFoundException(messageContext.getRequest());
            }
            if (!handleRequest(mappedEndpoint, messageContext)) {
                return;
            }
            // Apply handleRequest of registered interceptors
            if (!ObjectUtils.isEmpty(mappedEndpoint.getInterceptors())) {
                for (int i = 0; i < mappedEndpoint.getInterceptors().length; i++) {
                    EndpointInterceptor interceptor = mappedEndpoint.getInterceptors()[i];
                    interceptorIndex = i;
                    if (!interceptor.handleRequest(messageContext, mappedEndpoint.getEndpoint())) {
                        triggerHandleResponse(mappedEndpoint, interceptorIndex, messageContext);
                        return;
                    }
                }
            }
            // Acutally invoke the endpoint
            EndpointAdapter endpointAdapter = getEndpointAdapter(mappedEndpoint.getEndpoint());
            endpointAdapter.invoke(messageContext, mappedEndpoint.getEndpoint());

            // Apply handleResponse methods of registered interceptors
            triggerHandleResponse(mappedEndpoint, interceptorIndex, messageContext);
        }
        catch (NoEndpointFoundException ex) {
            // No triggering of interceptors if no endpoint is found
            if (endpointNotFoundLogger.isWarnEnabled()) {
                endpointNotFoundLogger.warn("No endpoint mapping found for [" + messageContext.getRequest() + "]");
            }
            throw ex;
        }
        catch (Exception ex) {
            Object endpoint = mappedEndpoint != null ? mappedEndpoint.getEndpoint() : null;
            processEndpointException(messageContext, endpoint, ex);
            triggerHandleResponse(mappedEndpoint, interceptorIndex, messageContext);
        }
    }

    /**
     * Returns the endpoint for this request. All endpoint mappings are tried, in order.
     *
     * @return the <code>EndpointInvocationChain</code>, or <code>null</code> if no endpoint could be found.
     */
    protected EndpointInvocationChain getEndpoint(MessageContext messageContext) throws Exception {
        for (Iterator iterator = endpointMappings.iterator(); iterator.hasNext();) {
            EndpointMapping endpointMapping = (EndpointMapping) iterator.next();
            EndpointInvocationChain endpoint = endpointMapping.getEndpoint(messageContext);
            if (endpoint != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Endpoint mapping [" + endpointMapping + "] maps request to endpoint [" +
                            endpoint.getEndpoint() + "]");
                }
                return endpoint;
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("Endpoint mapping [" + endpointMapping + "] has no mapping for request");
            }
        }
        return null;
    }

    /**
     * Returns the <code>EndpointAdapter</code> for the given endpoint.
     *
     * @param endpoint the endpoint to find an adapter for
     * @return the adapter
     */
    protected EndpointAdapter getEndpointAdapter(Object endpoint) {
        for (Iterator iterator = endpointAdapters.iterator(); iterator.hasNext();) {
            EndpointAdapter endpointAdapter = (EndpointAdapter) iterator.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Testing endpoint adapter [" + endpointAdapter + "]");
            }
            if (endpointAdapter.supports(endpoint)) {
                return endpointAdapter;
            }
        }
        throw new IllegalStateException("No adapter for endpoint [" + endpoint + "]: Does your endpoint implement a " +
                "supported interface like MessageHandler or PayloadEndpoint?");
    }

    /**
     * Callback for pre-processing of given invocation chain and message context. Gets called before invocation of
     * <code>handleRequest</code> on the interceptors.
     * <p/>
     * Default implementation does nothing, and returns <code>true</code>.
     *
     * @param mappedEndpoint the mapped <code>EndpointInvocationChain</code>
     * @param messageContext the message context
     * @return <code>true</code> if processing should continue; <code>false</code> otherwise
     */
    protected boolean handleRequest(EndpointInvocationChain mappedEndpoint, MessageContext messageContext) {
        return true;
    }

    /**
     * Initialize the default implementations for the dispatcher's strategies: <code>MessageEndpointAdapter</code> and
     * <code>PayloadEndpointAdapter</code>.
     *
     * @see #setEndpointAdapters(java.util.List)
     * @see org.springframework.ws.endpoint.MessageEndpointAdapter
     * @see org.springframework.ws.endpoint.PayloadEndpointAdapter
     */
    protected void initDefaultStrategies() {
        List defaultEndpointAdapters = new ArrayList();
        defaultEndpointAdapters.add(new MessageEndpointAdapter());
        defaultEndpointAdapters.add(new PayloadEndpointAdapter());
        setEndpointAdapters(defaultEndpointAdapters);
    }

    /**
     * Determine an error <code>SOAPMessage</code> respone via the registered <code>EndpointExceptionResolvers</code>.
     * Most likely, the response contains a <code>SOAPFault</code>. If no suitable resolver was found, the exception is
     * rethrown.
     *
     * @param messageContext current SOAPMessage request
     * @param endpoint       the executed endpoint, or null if none chosen at the time of the exception
     * @param ex             the exception that got thrown during handler execution
     * @throws Exception if no suitable resolver is found
     */
    protected void processEndpointException(MessageContext messageContext, Object endpoint, Exception ex)
            throws Exception {
        for (Iterator iterator = endpointExceptionResolvers.iterator(); iterator.hasNext();) {
            EndpointExceptionResolver resolver = (EndpointExceptionResolver) iterator.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Testing endpoint exception resolver [" + resolver + "]");
            }
            if (resolver.resolveException(messageContext, endpoint, ex)) {
                logger.warn("Endpoint invocation resulted in exception - responding with SOAP Fault", ex);
                return;
            }
        }
        // exception not resolved
        throw ex;
    }

    /**
     * Trigger handleResponse or handleFault on the mapped EndpointInterceptors. Will just invoke said method on all
     * interceptors whose handleRequest invocation returned <code>true</code>, in addition to the last interceptor who
     * returned <code>false</code>.
     *
     * @param mappedEndpoint   the mapped EndpointInvocationChain
     * @param interceptorIndex index of last interceptor that was called
     * @param messageContext   the message context, whose request and response are filled
     * @see EndpointInterceptor#handleResponse(MessageContext,Object)
     */
    protected void triggerHandleResponse(EndpointInvocationChain mappedEndpoint,
                                         int interceptorIndex,
                                         MessageContext messageContext) throws Exception {
        if (mappedEndpoint != null && messageContext.hasResponse() &&
                !ObjectUtils.isEmpty(mappedEndpoint.getInterceptors())) {
            boolean resume = true;
            for (int i = interceptorIndex; resume && i >= 0; i--) {
                EndpointInterceptor interceptor = mappedEndpoint.getInterceptors()[i];
                resume = interceptor.handleResponse(messageContext, mappedEndpoint.getEndpoint());
            }
        }
    }
}
