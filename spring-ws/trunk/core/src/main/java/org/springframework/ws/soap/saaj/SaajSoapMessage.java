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

package org.springframework.ws.soap.saaj;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.AttachmentException;
import org.springframework.ws.soap.SoapEnvelope;

/**
 * SAAJ-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>SaajSoapMessageContext</code>.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.SOAPMessage
 */
public class SaajSoapMessage extends AbstractSoapMessage {

    private SOAPMessage saajMessage;

    private SoapEnvelope envelope;

    /**
     * Create a new <code>SaajSoapMessage</code> based on the given SAAJ <code>SOAPMessage</code>.
     *
     * @param soapMessage the SAAJ SOAPMessage
     */
    public SaajSoapMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        saajMessage = soapMessage;
    }

    /**
     * Return the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public SOAPMessage getSaajMessage() {
        return saajMessage;
    }

    /**
     * Sets the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public void setSaajMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        saajMessage = soapMessage;
    }

    public SoapEnvelope getEnvelope() {
        if (envelope == null) {
            try {
                SOAPEnvelope saajEnvelope = getImplementation().getEnvelope(saajMessage);
                envelope = new SaajSoapEnvelope(saajEnvelope);
            }
            catch (SOAPException ex) {
                throw new SaajSoapEnvelopeException(ex);
            }
        }
        return envelope;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            getImplementation().writeTo(saajMessage, outputStream);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
    }

    public Iterator getAttachments() throws AttachmentException {
        Iterator iterator = getImplementation().getAttachments(saajMessage);
        return new SaajAttachmentIterator(iterator);
    }

    public Attachment getAttachment(String contentId) {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Id", contentId);
        Iterator iterator = getImplementation().getAttachment(saajMessage, mimeHeaders);
        if (!iterator.hasNext()) {
            return null;
        }
        else {
            AttachmentPart saajAttachment = (AttachmentPart) iterator.next();
            return new SaajAttachment(saajAttachment);
        }
    }

    public Attachment addAttachment(File file) throws AttachmentException {
        Assert.notNull(file, "File must not be null");
        DataSource dataSource = new FileDataSource(file);
        AttachmentPart attachmentPart = getImplementation().addAttachmentPart(saajMessage, dataSource);
        return new SaajAttachment(attachmentPart);
    }

    public Attachment addAttachment(InputStreamSource inputStreamSource, String contentType) {
        Assert.notNull(inputStreamSource, "InputStreamSource must not be null");
        if (inputStreamSource instanceof Resource && ((Resource) inputStreamSource).isOpen()) {
            throw new IllegalArgumentException("Passed-in Resource contains an open stream: invalid argument. " +
                    "SAAJ requires an InputStreamSource that creates a fresh stream for every call.");
        }
        DataSource dataSource = new InputStreamSourceDataSource(inputStreamSource, contentType);
        AttachmentPart saajAttachment = getImplementation().addAttachmentPart(saajMessage, dataSource);
        return new SaajAttachment(saajAttachment);
    }

    protected SaajImplementation getImplementation() {
        return SaajImplementation.getImplementation();
    }

    private static class SaajAttachmentIterator implements Iterator {

        private final Iterator saajIterator;

        public SaajAttachmentIterator(Iterator saajIterator) {
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            AttachmentPart saajAttachment = (AttachmentPart) saajIterator.next();
            return new SaajAttachment(saajAttachment);
        }

        public void remove() {
            saajIterator.remove();
        }
    }

}
