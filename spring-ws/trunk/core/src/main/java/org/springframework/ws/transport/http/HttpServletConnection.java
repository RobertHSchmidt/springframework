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

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.EndpointAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * Implementation of {@link WebServiceConnection} that is based on the Servlet API.
 *
 * @author Arjen Poutsma
 */
public class HttpServletConnection extends AbstractReceiverConnection implements EndpointAwareWebServiceConnection {

    private final HttpServletRequest httpServletRequest;

    private final HttpServletResponse httpServletResponse;

    private boolean sentResponse = false;

    private boolean endpointFound = true;

    /**
     * Constructs a new servlet connection with the given <code>HttpServletRequest</code> and
     * <code>HttpServletResponse</code>.
     */
    public HttpServletConnection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    /** Returns the <code>HttpServletRequest</code> for this connection. */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    /** Returns the <code>HttpServletResponse</code> for this connection. */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void endpointNotFound() {
        endpointFound = false;
        getHttpServletResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    public boolean hasError() throws IOException {
        return false;
    }

    public String getErrorMessage() throws IOException {
        return null;
    }

    public void close() throws IOException {
        if (!sentResponse && endpointFound) {
            getHttpServletResponse().setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    /*
     * Receiving request
     */

    protected Iterator getRequestHeaderNames() throws IOException {
        return new EnumerationIterator(getHttpServletRequest().getHeaderNames());
    }

    protected Iterator getRequestHeaders(String name) throws IOException {
        return new EnumerationIterator(getHttpServletRequest().getHeaders(name));
    }

    protected InputStream getRequestInputStream() throws IOException {
        return getHttpServletRequest().getInputStream();
    }

    /*
    * Sending response
    */

    protected void addResponseHeader(String name, String value) throws IOException {
        getHttpServletResponse().addHeader(name, value);
    }

    protected OutputStream getResponseOutputStream() throws IOException {
        return getHttpServletResponse().getOutputStream();
    }

    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        sentResponse = true;
        if (!message.hasFault()) {
            getHttpServletResponse().setStatus(HttpServletResponse.SC_OK);
        }
        else {
            getHttpServletResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
