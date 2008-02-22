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

package org.springframework.ws.soap.addressing;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * {@link SoapEndpointInterceptor} implementation that deals with WS-Addressing headers. Stateful, and instatiated by
 * the {@link AbstractAddressingEndpointMapping}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class AddressingEndpointInterceptor implements SoapEndpointInterceptor {

    private static final Log logger = LogFactory.getLog(AddressingEndpointInterceptor.class);

    private final WsAddressingVersion version;

    private final MessageIdStrategy messageIdStrategy;

    private final WebServiceMessageSender[] messageSenders;

    private URI replyAction;

    private URI faultAction;

    AddressingEndpointInterceptor(WsAddressingVersion version,
                                  MessageIdStrategy messageIdStrategy,
                                  WebServiceMessageSender[] messageSenders,
                                  URI replyAction,
                                  URI faultAction) {
        Assert.notNull(version, "version must not be null");
        Assert.notNull(messageIdStrategy, "messageIdStrategy must not be null");
        if (messageSenders == null) {
            messageSenders = new WebServiceMessageSender[0];
        }
        this.version = version;
        this.messageIdStrategy = messageIdStrategy;
        this.messageSenders = messageSenders;
        this.replyAction = replyAction;
        this.faultAction = faultAction;
    }

    public final boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
        SoapMessage request = (SoapMessage) messageContext.getRequest();
        MessageAddressingProperties requestMap = version.getMessageAddressingProperties(request);
        if (!requestMap.hasRequiredProperties()) {
            version.addMessageAddressingHeaderRequiredFault((SoapMessage) messageContext.getResponse());
            return false;
        }
        if (!requestMap.isValid() || messageIdStrategy.isDuplicate(requestMap.getMessageId())) {
            version.addInvalidAddressingHeaderFault((SoapMessage) messageContext.getResponse());
            return false;
        }
        return true;
    }

    public final boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return handleResponseOrFault(messageContext, false);
    }

    public final boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return handleResponseOrFault(messageContext, true);
    }

    private boolean handleResponseOrFault(MessageContext messageContext, boolean isFault) throws Exception {
        Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
        Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse());
        MessageAddressingProperties requestMap =
                version.getMessageAddressingProperties((SoapMessage) messageContext.getRequest());
        EndpointReference replyEpr = !isFault ? requestMap.getReplyTo() : requestMap.getFaultTo();
        if (handleNoneAddress(messageContext, replyEpr)) {
            return false;
        }
        URI responseMessageId = getMessageId(messageContext);
        URI action = !isFault ? replyAction : faultAction;
        MessageAddressingProperties replyMap = requestMap.getReplyProperties(replyEpr, action, responseMessageId);
        version.addAddressingHeaders((SoapMessage) messageContext.getResponse(), replyMap);
        if (handleAnonymousAddress(messageContext, replyEpr)) {
            return true;
        }
        else {
            sendOutOfBand(messageContext, replyEpr);
            return false;
        }
    }

    private boolean handleNoneAddress(MessageContext messageContext, EndpointReference replyEpr) {
        if (replyEpr == null || version.hasNoneAddress(replyEpr)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Request " + messageContext.getRequest() + "] has [" + replyEpr +
                        "] reply address; reply [" + messageContext.getResponse() + "] discarded");
            }
            messageContext.clearResponse();
            return true;
        }
        return false;
    }

    private boolean handleAnonymousAddress(MessageContext messageContext, EndpointReference replyEpr) {
        if (version.hasAnonymousAddress(replyEpr)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Request " + messageContext.getRequest() + "] has [" + replyEpr +
                        "] reply address; sending in-band reply [" + messageContext.getResponse() + "]");
            }
            return true;
        }
        return false;
    }

    private void sendOutOfBand(MessageContext messageContext, EndpointReference replyEpr) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Request " + messageContext.getRequest() + "] has [" + replyEpr +
                    "] reply address; sending out-of-band reply [" + messageContext.getResponse() + "]");
        }

        boolean supported = false;
        for (int i = 0; i < messageSenders.length; i++) {
            if (messageSenders[i].supports(replyEpr.getAddress())) {
                supported = true;
                WebServiceConnection connection = null;
                try {
                    connection = messageSenders[i].createConnection(replyEpr.getAddress());
                    connection.send(messageContext.getResponse());
                    break;
                }
                finally {
                    messageContext.clearResponse();
                    if (connection != null) {
                        connection.close();
                    }
                }
            }
        }
        if (!supported) {
            logger.warn("Could not send out-of-band response to [" + replyEpr.getAddress() + "]. " +
                    "Configure WebServiceMessageSenders which support this uri.");
        }
    }

    private URI getMessageId(MessageContext messageContext) {
        URI responseMessageId = messageIdStrategy.newMessageId(messageContext);
        if (logger.isTraceEnabled()) {
            logger.trace("Generated reply MessageID [" + responseMessageId + "] for [" + messageContext + "]");
        }
        return responseMessageId;
    }

    public boolean understands(SoapHeaderElement header) {
        return version.understands(header);
    }
}
