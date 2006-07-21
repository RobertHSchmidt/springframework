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

package org.springframework.ws.soap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.EndpointInterceptor;
import org.springframework.ws.EndpointInvocationChain;
import org.springframework.ws.MessageDispatcher;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.ws.soap.soap12.Soap12Header;

/**
 * SOAP-specific subclass of the <code>MessageDispatcher</code>. Adds functionality for adding actor roles to a endpoint
 * invocation chain, and endpoint interception using <code>SoapEndpointInterceptor</code>s.
 *
 * @author Arjen Poutsma
 * @see SoapMessage
 * @see SoapEndpointInterceptor
 */
public class SoapMessageDispatcher extends MessageDispatcher {

    /**
     * Default message used when creating a SOAP MustUnderstand fault.
     */
    public static final String DEFAULT_MUST_UNDERSTAND_FAULT =
            "One or more mandatory SOAP header blocks not understood";

    private String mustUnderstandFault = DEFAULT_MUST_UNDERSTAND_FAULT;

    private Locale mustUnderstandFaultLocale = Locale.ENGLISH;

    /**
     * Sets the message used for <code>MustUnderstand</code> fault. Default to <code>DEFAULT_MUST_UNDERSTAND_FAULT</code>.
     *
     * @see #DEFAULT_MUST_UNDERSTAND_FAULT
     */
    public void setMustUnderstandFault(String mustUnderstandFault) {
        this.mustUnderstandFault = mustUnderstandFault;
    }

    /**
     * Sets the locale of the message used for <code>MustUnderstand</code> fault. Default to
     * <code>Locale.ENGLISH</code>.
     */
    public void setMustUnderstandFaultLocale(Locale mustUnderstandFaultLocale) {
        this.mustUnderstandFaultLocale = mustUnderstandFaultLocale;
    }

    /**
     * Process the <code>MustUnderstand</code> headers in the incoming SOAP request message. Iterates over all SOAP
     * headers which should be understood, and determines whether these are supported. Generates a SOAP MustUnderstand
     * fault if a header is not understood.
     *
     * @param mappedEndpoint the mapped EndpointInvocationChain
     * @param messageContext the message context
     * @return <code>true</code> if all necessary headers are understood; <code>false</code> otherwise
     * @see SoapEndpointInvocationChain#getRoles()
     * @see SoapHeader#examineMustUnderstandHeaderElements(String)
     */
    protected boolean handleRequest(EndpointInvocationChain mappedEndpoint, MessageContext messageContext) {
        if (mappedEndpoint instanceof SoapEndpointInvocationChain && messageContext instanceof SoapMessageContext) {
            SoapEndpointInvocationChain mappedSoapEndpoint = (SoapEndpointInvocationChain) mappedEndpoint;
            SoapMessageContext soapContext = (SoapMessageContext) messageContext;
            if (soapContext.getSoapRequest().getSoapHeader() == null) {
                // no headers to process
                return true;
            }
            String[] roles = getRoles(mappedSoapEndpoint.getRoles(), soapContext.getSoapRequest().getVersion());
            for (int i = 0; i < roles.length; i++) {
                if (!handleRequestForRole(mappedSoapEndpoint, soapContext, roles[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines the roles for a specific SOAP invocation chain. Gets the roles specified on the chain, and adds the
     * SOAP-version specific 'next' role to it.
     *
     * @see SoapVersion#getNextActorOrRoleUri()
     */
    protected String[] getRoles(String[] mappedRoles, SoapVersion version) {
        if (mappedRoles == null) {
            mappedRoles = new String[0];
        }
        return StringUtils.addStringToArray(mappedRoles, version.getNextActorOrRoleUri());
    }

    /**
     * Handles the request for a single SOAP role. Iterates over all <code>MustUnderstand</code> headers for a specific
     * role, and determines whether these are understood by any of the registered <code>SoapEndpointInterceptor</code>.
     * If they are, returns <code>true</code>. If they are not, a SOAP fault is created, and false is returned.
     *
     * @see SoapEndpointInterceptor#understands(SoapHeaderElement)
     */
    private boolean handleRequestForRole(SoapEndpointInvocationChain mappedEndpoint,
                                         SoapMessageContext messageContext,
                                         String role) {
        SoapHeader requestHeader = messageContext.getSoapRequest().getSoapHeader();
        List notUnderstoodHeaderNames = new ArrayList();
        for (Iterator iterator = requestHeader.examineMustUnderstandHeaderElements(role); iterator.hasNext();) {
            SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
            QName headerName = headerElement.getName();
            if (logger.isDebugEnabled()) {
                logger.debug("Received mustUnderstand header with name: " + headerName);
            }
            boolean understood = false;
            for (int i = 0; i < mappedEndpoint.getInterceptors().length; i++) {
                EndpointInterceptor interceptor = mappedEndpoint.getInterceptors()[i];
                if (interceptor instanceof SoapEndpointInterceptor &&
                        ((SoapEndpointInterceptor) interceptor).understands(headerElement)) {
                    understood = true;
                    break;
                }
            }
            if (!understood) {
                notUnderstoodHeaderNames.add(headerName);
            }
        }
        if (notUnderstoodHeaderNames.isEmpty()) {
            return true;
        }
        else {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not handle mustUnderstand headers: " +
                        StringUtils.collectionToCommaDelimitedString(notUnderstoodHeaderNames) + ". Returning fault");
            }
            SoapBody responseBody = messageContext.getSoapResponse().getSoapBody();
            SoapFault fault = responseBody.addMustUnderstandFault(mustUnderstandFault, mustUnderstandFaultLocale);
            fault.setFaultActorOrRole(role);
            SoapHeader header = messageContext.getSoapResponse().getSoapHeader();
            if (header instanceof Soap12Header) {
                Soap12Header soap12Header = (Soap12Header) header;
                for (Iterator iterator = notUnderstoodHeaderNames.iterator(); iterator.hasNext();) {
                    QName headerName = (QName) iterator.next();
                    soap12Header.addNotUnderstoodHeaderElement(headerName);
                }
            }
            return false;
        }
    }

    /**
     * Trigger handleResponse or handleFault on the mapped EndpointInterceptors. Will just invoke said method on all
     * interceptors whose handleRequest invocation returned <code>true</code>, in addition to the last interceptor who
     * returned <code>false</code>.
     *
     * @param mappedEndpoint   the mapped EndpointInvocationChain
     * @param interceptorIndex index of last interceptor that was called
     * @param messageContext   the message context, whose request and response are filled
     * @see org.springframework.ws.EndpointInterceptor#handleResponse(org.springframework.ws.context.MessageContext,
     *      Object)
     */
    protected void triggerHandleResponse(EndpointInvocationChain mappedEndpoint,
                                         int interceptorIndex,
                                         MessageContext messageContext) throws Exception {
        if (mappedEndpoint != null && messageContext.hasResponse() &&
                !ObjectUtils.isEmpty(mappedEndpoint.getInterceptors())) {
            boolean hasFault = false;
            if (messageContext instanceof SoapMessageContext) {
                SoapMessageContext soapMessageContext = (SoapMessageContext) messageContext;
                hasFault = soapMessageContext.getSoapResponse().getSoapBody().hasFault();
            }
            boolean resume = true;
            for (int i = interceptorIndex; resume && i >= 0; i--) {
                EndpointInterceptor interceptor = mappedEndpoint.getInterceptors()[i];
                if (hasFault) {
                    if (interceptor instanceof SoapEndpointInterceptor) {
                        SoapEndpointInterceptor soapEndpointInterceptor = (SoapEndpointInterceptor) interceptor;
                        resume = soapEndpointInterceptor.handleFault(messageContext, mappedEndpoint.getEndpoint());
                    }
                }
                else {
                    resume = interceptor.handleResponse(messageContext, mappedEndpoint.getEndpoint());
                }
            }
        }
    }
}
