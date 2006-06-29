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

package org.springframework.xml.transform;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.sax.SAXResult;

import org.xml.sax.ContentHandler;

import org.springframework.xml.stream.StaxEventContentHandler;
import org.springframework.xml.stream.StaxStreamContentHandler;

/**
 * Implementation of the <code>Result</code> tagging interface for StAX writers. Can be constructed with a
 * <code>XMLEventConsumer</code> or a <code>XMLStreamWriter</code>.
 * <p/>
 * This class is necessary because there is no implementation of <code>Source</code> for StaxReaders in JAXP 1.3. There
 * will be a <code>StaxResult</code> in JAXP 1.4 (JDK 1.6), and by the time that version is available, this class will
 * probably be deprecated.
 * <p/>
 * Even though <code>StaxResult</code> extends from <code>SAXResult</code>, calling the methods of
 * <code>SAXResult</code> is <strong>not supported</strong>. In general, the only supported operation on this class is
 * to use the <code>ContentHandler</code> obtained via {@link #getHandler()} to parse an input source using an
 * <code>XMLReader</code>. Calling {@link #setHandler(org.xml.sax.ContentHandler)} will result in
 * <code>UnsupportedOperationException</code>s.
 *
 * @author Arjen Poutsma
 * @see XMLEventConsumer
 * @see XMLStreamWriter
 * @see javax.xml.transform.Transformer
 */
public class StaxResult extends SAXResult {

    private XMLEventWriter eventWriter;

    private XMLStreamWriter streamWriter;

    /**
     * Constructs a new instance of the <code>StaxResult</code> with the specified <code>XMLStreamWriter</code>.
     *
     * @param streamWriter the <code>XMLStreamWriter</code> to write to
     */
    public StaxResult(XMLStreamWriter streamWriter) {
        super.setHandler(new StaxStreamContentHandler(streamWriter));
        this.streamWriter = streamWriter;
    }

    /**
     * Constructs a new instance of the <code>StaxResult</code> with the specified <code>XMLEventWriter</code>.
     *
     * @param eventWriter the <code>XMLEventWriter</code> to write to
     */
    public StaxResult(XMLEventWriter eventWriter) {
        super.setHandler(new StaxEventContentHandler(eventWriter));
        this.eventWriter = eventWriter;
    }

    /**
     * Returns the <code>XMLEventWriter</code> used by this <code>StaxResult</code>. If this <code>StaxResult</code> was
     * created with an <code>XMLStreamWriter</code>, the result will be <code>null</code>.
     *
     * @return the StAX event writer used by this result
     * @see #StaxResult(javax.xml.stream.XMLEventWriter)
     */
    public XMLEventWriter getXMLEventWriter() {
        return eventWriter;
    }

    /**
     * Returns the <code>XMLStreamWriter</code> used by this <code>StaxResult</code>. If this <code>StaxResult</code>
     * was created with an <code>XMLEventConsumer</code>, the result will be <code>null</code>.
     *
     * @return the StAX stream writer used by this result
     * @see #StaxResult(javax.xml.stream.XMLStreamWriter)
     */
    public XMLStreamWriter getXMLStreamWriter() {
        return streamWriter;
    }

    /**
     * Throws a <code>UnsupportedOperationException</code>.
     *
     * @throws UnsupportedOperationException always
     */
    public void setHandler(ContentHandler handler) {
        throw new UnsupportedOperationException("setHandler is not supported");
    }
}
