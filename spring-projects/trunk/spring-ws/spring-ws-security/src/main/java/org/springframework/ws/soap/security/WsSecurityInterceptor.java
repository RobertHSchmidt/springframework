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

package org.springframework.ws.soap.security;

import javax.xml.transform.TransformerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.util.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Endpoint interceptor that handles WS-Security. Delegates to a <code>WsSecurityMessageProcessor</code>.
 * <p/>
 * This interceptor can be configured to validate incoming and secure outgoing messages. By default, both are on.
 *
 * @author Arjen Poutsma
 * @see WsSecurityMessageProcessor
 * @see #setMessageProcessor(WsSecurityMessageProcessor)
 */
public class WsSecurityInterceptor implements SoapEndpointInterceptor, InitializingBean {

    /**
     * Log category to which all <code>WsSecurityValidationException</code>s are logged.
     */
    public static final String VALIDATION_LOG_CATEGORY = "org.springframework.ws.soap.security.Validation";

    /**
     * Log category to which all <code>WsSecuritySecurementException</code>s are logged.
     */
    public static final String SECUREMENT_LOG_CATEGORY = "org.springframework.ws.soap.security.Securement";

    /**
     * Logger to use when no mapped handler is found for a request.
     */
    protected static final Log validationLogger = LogFactory.getLog(VALIDATION_LOG_CATEGORY);

    /**
     * Logger to use when no mapped handler is found for a request.
     */
    protected static final Log securementLogger = LogFactory.getLog(SECUREMENT_LOG_CATEGORY);

    private WsSecurityMessageProcessor messageProcessor;

    private boolean validateRequest = true;

    private boolean secureResponse = true;

    /**
     * Sets the <code>WsSecurityMessageProcessor</code> to use.
     */
    public void setMessageProcessor(WsSecurityMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    /**
     * Indicates whether outgoing responsed are to be secured. Defaults to <code>true</code>.
     *
     * @param secureResponse
     */
    public void setSecureResponse(boolean secureResponse) {
        this.secureResponse = secureResponse;
    }

    /**
     * Indicates whether incoming request are to be validated. Defaults to <code>true</code>.
     */
    public void setValidateRequest(boolean validateRequest) {
        this.validateRequest = validateRequest;
    }

    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (validateRequest) {
            SoapMessageContext soapMessageContext = (SoapMessageContext) messageContext;
            try {
                messageProcessor.validateMessage(soapMessageContext.getSoapRequest());
                return true;
            }
            catch (WsSecurityValidationException ex) {
                validationLogger.warn("Could not validate request: " + ex.getMessage(), ex);
                return false;
            }
        }
        else {
            return true;
        }
    }

    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (secureResponse) {
            SoapMessageContext soapMessageContext = (SoapMessageContext) messageContext;
            try {
                messageProcessor.secureMessage(soapMessageContext.getSoapResponse());
                return true;
            }
            catch (WsSecuritySecurementException ex) {
                securementLogger.error("Could not secure response: " + ex.getMessage(), ex);
                return false;
            }
        }
        else {
            return true;
        }
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(messageProcessor, "messageProcessor is required");
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    public boolean understands(SoapHeaderElement header) {
        return messageProcessor.understands(header);
    }
}
