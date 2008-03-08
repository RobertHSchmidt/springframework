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

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Abstract base class for {@link WebServiceConnection} implementations used for receiving requests.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractReceiverConnection extends AbstractWebServiceConnection {

    private TransportInputStream requestInputStream;

    private TransportOutputStream responseOutputStream;

    protected final TransportInputStream createTransportInputStream() throws IOException {
        if (requestInputStream == null) {
            requestInputStream = new RequestTransportInputStream();
        }
        return requestInputStream;
    }

    protected final TransportOutputStream createTransportOutputStream() throws IOException {
        if (responseOutputStream == null) {
            responseOutputStream = new ResponseTransportOutputStream();
        }
        return responseOutputStream;
    }

    public final void close() throws IOException {
        try {
            if (requestInputStream != null) {
                requestInputStream.close();
            }
        }
        finally {
            onClose();
        }
    }

    /**
     * Template method invoked from {@link #close()}. Default implementation is empty.
     *
     * @throws IOException if an I/O error occurs when closing this connection
     */
    protected void onClose() throws IOException {
    }

    /**
     * Returns an iteration over all the header names this request contains. Returns an empty <code>Iterator</code> if
     * there areno headers.
     */
    protected abstract Iterator getRequestHeaderNames() throws IOException;

    /**
     * Returns an iteration over all the string values of the specified header. Returns an empty <code>Iterator</code>
     * if there are no headers of the specified name.
     */
    protected abstract Iterator getRequestHeaders(String name) throws IOException;

    /** Returns the input stream to read the response from. */
    protected abstract InputStream getRequestInputStream() throws IOException;

    /**
     * Adds a response header with the given name and value. This method can be called multiple times, to allow for
     * headers with multiple values.
     *
     * @param name  the name of the header
     * @param value the value of the header
     */
    protected abstract void addResponseHeader(String name, String value) throws IOException;

    /** Returns the output stream to write the request to. */
    protected abstract OutputStream getResponseOutputStream() throws IOException;

    /** Implementation of <code>TransportInputStream</code> for receiving-side connections. */
    private class RequestTransportInputStream extends TransportInputStream {

        protected InputStream createInputStream() throws IOException {
            return getRequestInputStream();
        }

        public Iterator getHeaderNames() throws IOException {
            return getRequestHeaderNames();
        }

        public Iterator getHeaders(String name) throws IOException {
            return getRequestHeaders(name);
        }

        public void close() throws IOException {
            // defer close, some SoapMessage implementations (Axis) lazy-initialize the SOAPMessage
        }


    }

    /** Implementation of <code>TransportOutputStream</code> for sending-side connections. */
    private class ResponseTransportOutputStream extends TransportOutputStream {

        public void addHeader(String name, String value) throws IOException {
            addResponseHeader(name, value);
        }

        protected OutputStream createOutputStream() throws IOException {
            return getResponseOutputStream();
        }

    }


}
