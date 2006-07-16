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

package org.springframework.ws.transport.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestMethodNotSupportedException;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapMessage;

/**
 * Adapter to use the <code>MessageEndpoint</code> interface with the generic <code>DispatcherServlet</code>.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.endpoint.MessageEndpoint
 */
public class MessageEndpointHandlerAdapter implements HandlerAdapter, InitializingBean {

    private static final Log logger = LogFactory.getLog(MessageEndpointHandlerAdapter.class);

    private MessageContextFactory messageContextFactory;

    public void setMessageContextFactory(MessageContextFactory messageContextFactory) {
        this.messageContextFactory = messageContextFactory;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1L;
    }

    public ModelAndView handle(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               Object handler) throws Exception {
        if ("POST".equals(httpServletRequest.getMethod())) {
            handlePost(httpServletRequest, (MessageEndpoint) handler, httpServletResponse);
            return null;
        }
        else {
            throw new RequestMethodNotSupportedException(
                    "Request method '" + httpServletRequest.getMethod() + "' not supported");
        }
    }

    public boolean supports(Object handler) {
        return handler instanceof MessageEndpoint;
    }

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(messageContextFactory, "messageContextFactory is required");
        MessageEndpointHandlerAdapter.logger.info("Using message context factory " + messageContextFactory);
    }

    private void handlePost(HttpServletRequest httpServletRequest,
                            MessageEndpoint endpoint,
                            HttpServletResponse httpServletResponse) throws Exception {
        HttpTransportContext transportContext = new HttpTransportContext(httpServletRequest, httpServletResponse);
        MessageContext messageContext = messageContextFactory.createContext(transportContext);
        try {
            endpoint.invoke(messageContext);
            if (!messageContext.hasResponse()) {
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            else {
                WebServiceMessage webServiceResponse = messageContext.getResponse();
                if (webServiceResponse instanceof SoapMessage &&
                        ((SoapMessage) webServiceResponse).getSoapBody().hasFault()) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                else {
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                }
                messageContext.sendResponse(new HttpTransportResponse(httpServletResponse));
            }
        }
        catch (NoEndpointFoundException ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
