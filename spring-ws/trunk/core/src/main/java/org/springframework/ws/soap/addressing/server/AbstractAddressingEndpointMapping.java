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

package org.springframework.ws.soap.addressing.server;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.RandomGuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.Addressing200408;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for {@link EndpointMapping} implementations that handle WS-Addressing. Besides the normal {@link
 * SoapEndpointMapping} properties, this mapping has a {@link #setVersions(org.springframework.ws.soap.addressing.version.AddressingVersion[])
 * versions} property, which defines the WS-Addressing specifications supported. By default, these are {@link
 * org.springframework.ws.soap.addressing.version.Addressing200408} and {@link org.springframework.ws.soap.addressing.version.Addressing10}.
 * <p/>
 * The {@link #setMessageIdStrategy(MessageIdStrategy) messageIdStrategy} property defines the strategy to use for
 * creating reply <code>MessageIDs</code>. By default, this is the {@link UuidMessageIdStrategy} on Java 5 and higher,
 * and the {@link RandomGuidMessageIdStrategy} on Java 1.4.
 * <p/>
 * The {@link #setMessageSenders(WebServiceMessageSender[]) messageSenders} are used to send out-of-band reply messages.
 * If a request messages defines a non-anonymous reply address, these senders will be used to send the message.
 * <p/>
 * This mapping (and all subclasses) uses an implicit WS-Addressing {@link EndpointInterceptor}, which is added in every
 * {@link EndpointInvocationChain} produced. As such, this mapping does not have the standard <code>interceptors</code>
 * property, but rather a {@link #setPreInterceptors(EndpointInterceptor[]) preInterceptors} and {@link
 * #setPostInterceptors(EndpointInterceptor[]) postInterceptors} property, which are added before and after the implicit
 * WS-Addressing interceptor, respectively.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractAddressingEndpointMapping extends TransformerObjectSupport
        implements SoapEndpointMapping, InitializingBean {

    private String[] actorsOrRoles;

    private boolean isUltimateReceiver = true;

    private MessageIdStrategy messageIdStrategy;

    private WebServiceMessageSender[] messageSenders = new WebServiceMessageSender[0];

    private AddressingVersion[] versions;

    private EndpointInterceptor[] preInterceptors = new EndpointInterceptor[0];

    private EndpointInterceptor[] postInterceptors = new EndpointInterceptor[0];

    /** Protected constructor. Initializes the default settings. */
    protected AbstractAddressingEndpointMapping() {
        initDefaultStrategies();
    }

    /**
     * Initializes the default implementation for this mapping's strategies: the {@link
     * org.springframework.ws.soap.addressing.version.Addressing200408} and {@link org.springframework.ws.soap.addressing.version.Addressing10}
     * versions of the specication, and the {@link UuidMessageIdStrategy} on Java 5 and higher; the {@link
     * RandomGuidMessageIdStrategy} on Java 1.4.
     */
    protected void initDefaultStrategies() {
        this.versions = new AddressingVersion[]{new Addressing200408(), new Addressing10()};
        if (JdkVersion.isAtLeastJava15()) {
            messageIdStrategy = new UuidMessageIdStrategy();
        }
        else {
            messageIdStrategy = new RandomGuidMessageIdStrategy();
        }
    }

    public final void setActorOrRole(String actorOrRole) {
        Assert.notNull(actorOrRole, "actorOrRole must not be null");
        actorsOrRoles = new String[]{actorOrRole};
    }

    public final void setActorsOrRoles(String[] actorsOrRoles) {
        Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
        this.actorsOrRoles = actorsOrRoles;
    }

    public final void setUltimateReceiver(boolean ultimateReceiver) {
        this.isUltimateReceiver = ultimateReceiver;
    }

    /**
     * Set additional interceptors to be applied before the implicit WS-Addressing interceptor, e.g.
     * <code>XwsSecurityInterceptor</code>.
     */
    public final void setPreInterceptors(EndpointInterceptor[] preInterceptors) {
        Assert.notNull(preInterceptors, "'preInterceptors' must not be null");
        this.preInterceptors = preInterceptors;
    }

    /**
     * Set additional interceptors to be applied after the implicit WS-Addressing interceptor, e.g.
     * <code>PayloadLoggingInterceptor</code>.
     */
    public final void setPostInterceptors(EndpointInterceptor[] postInterceptors) {
        Assert.notNull(postInterceptors, "'postInterceptors' must not be null");
        this.postInterceptors = postInterceptors;
    }

    /**
     * Sets the message id strategy used for creating WS-Addressing MessageIds.
     * <p/>
     * By default, the {@link UuidMessageIdStrategy} is used on Java 5 and higher, and the {@link
     * RandomGuidMessageIdStrategy} on Java 1.4.
     */
    public final void setMessageIdStrategy(MessageIdStrategy messageIdStrategy) {
        Assert.notNull(messageIdStrategy, "'messageIdStrategy' must not be null");
        this.messageIdStrategy = messageIdStrategy;
    }

    public final void setMessageSenders(WebServiceMessageSender[] messageSenders) {
        Assert.notNull(messageSenders, "'messageSenders' must not be null");
        this.messageSenders = messageSenders;
    }

    /**
     * Sets the WS-Addressing versions to be supported by this mapping.
     * <p/>
     * By default, this array is set to support {@link org.springframework.ws.soap.addressing.version.Addressing200408
     * the August 2004} and the {@link org.springframework.ws.soap.addressing.version.Addressing10 May 2006} versions of
     * the specification.
     */
    public final void setVersions(AddressingVersion[] versions) {
        this.versions = versions;
    }

    public void afterPropertiesSet() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Supporting " + Arrays.asList(versions));
        }
    }

    public final EndpointInvocationChain getEndpoint(MessageContext messageContext) throws TransformerException {
        Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        for (int i = 0; i < versions.length; i++) {
            if (supports(versions[i], request)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Request [" + request + "] uses [" + versions[i] + "]");
                }
                MessageAddressingProperties requestMap = versions[i].getMessageAddressingProperties(request);
                if (requestMap == null) {
                    return null;
                }
                Object endpoint = getEndpointInternal(requestMap);
                if (endpoint == null) {
                    return null;
                }
                return getEndpointInvocationChain(endpoint, versions[i]);
            }
        }
        return null;
    }

    /**
     * Creates a {@link SoapEndpointInvocationChain} based on the given endpoint and {@link
     * org.springframework.ws.soap.addressing.version.AddressingVersion}.
     */
    private EndpointInvocationChain getEndpointInvocationChain(Object endpoint, AddressingVersion version) {
        URI responseAction = getResponseAction(endpoint);
        URI faultAction = getFaultAction(endpoint);
        EndpointInterceptor[] interceptors =
                new EndpointInterceptor[preInterceptors.length + postInterceptors.length + 1];
        System.arraycopy(preInterceptors, 0, interceptors, 0, preInterceptors.length);
        AddressingEndpointInterceptor interceptor = new AddressingEndpointInterceptor(version, messageIdStrategy,
                messageSenders, responseAction, faultAction);
        interceptors[preInterceptors.length] = interceptor;
        System.arraycopy(postInterceptors, 0, interceptors, preInterceptors.length + 1, postInterceptors.length);
        return new SoapEndpointInvocationChain(endpoint, interceptors, actorsOrRoles, isUltimateReceiver);
    }

    private boolean supports(AddressingVersion version, SoapMessage request) {
        SoapHeader header = request.getSoapHeader();
        if (header != null) {
            for (Iterator iterator = header.examineAllHeaderElements(); iterator.hasNext();) {
                SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
                if (version.understands(headerElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Lookup an endpoint for the given  {@link MessageAddressingProperties}, returning <code>null</code> if no specific
     * one is found. This template method is called by {@link #getEndpoint(MessageContext)}.
     *
     * @param map the message addressing properties
     * @return the endpoint, or <code>null</code>
     */
    protected abstract Object getEndpointInternal(MessageAddressingProperties map);

    protected URI getResponseAction(Object endpoint) {
        return null;
    }

    protected URI getFaultAction(Object endpoint) {
        return null;
    }

}
