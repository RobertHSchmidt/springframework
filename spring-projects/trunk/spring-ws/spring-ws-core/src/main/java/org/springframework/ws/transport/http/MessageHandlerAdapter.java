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

package org.springframework.ws.transport.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapMessage;

/**
 * Adapter to use the <code>MessageHandler</code> interface with the generic <code>DispatcherServlet</code>.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.endpoint.MessageEndpoint
 */
public class MessageHandlerAdapter implements HandlerAdapter, InitializingBean {

    /**
     * Default content type. Normally retrieved from the <code>SoapVersion</code>.
     *
     * @see org.springframework.ws.soap.SoapVersion#getContentType()
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/xml";

    private static final Log logger = LogFactory.getLog(MessageHandlerAdapter.class);

    private MessageContextFactory messageContextFactory;

    public void setMessageContextFactory(MessageContextFactory messageContextFactory) {
        this.messageContextFactory = messageContextFactory;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1L;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("POST".equals(request.getMethod())) {
            try {
                MessageContext messageContext = messageContextFactory.createContext(request);
                ((MessageEndpoint) handler).invoke(messageContext);
                WebServiceMessage responseMessage = messageContext.getResponse();
                if (responseMessage != null) {
                    String contentType;
                    if (responseMessage instanceof SoapMessage) {
                        SoapMessage soapMessage = (SoapMessage) responseMessage;
                        if (!soapMessage.getSoapBody().hasFault()) {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }
                        else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                        contentType = soapMessage.getVersion().getContentType();
                    }
                    else {
                        response.setStatus(HttpServletResponse.SC_OK);
                        contentType = DEFAULT_CONTENT_TYPE;
                    }
                    response.setContentType(contentType + "; charset=" + responseMessage.getCharacterEncoding());
                    responseMessage.writeTo(response.getOutputStream());
                }
            }
            catch (NoEndpointFoundException ex) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType(null);
                response.setCharacterEncoding(null);
            }
        }
        return null;
    }

    public boolean supports(Object handler) {
        return (handler instanceof MessageEndpoint);
    }

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(messageContextFactory, "messageContextFactory is required");
        logger.info("Using message context factory " + messageContextFactory);
    }
}
