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

package org.springframework.xml.dom;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.springframework.util.Assert;

/**
 * SAX <code>ContentHandler</code> that transforms callback calls to DOM <code>Node</code>s.
 *
 * @author Arjen Poutsma
 * @see org.w3c.dom.Node
 * @since 1.0.0
 */
public class DomContentHandler implements ContentHandler {

    private final Document document;

    private final List elements = new ArrayList();

    private final Node node;

    /**
     * Creates a new instance of the <code>DomContentHandler</code> with the given node.
     *
     * @param node the node to publish events to
     */
    public DomContentHandler(Node node) {
        Assert.notNull(node, "node must not be null");
        this.node = node;
        if (node instanceof Document) {
            document = (Document) node;
        }
        else {
            document = node.getOwnerDocument();
        }
        Assert.notNull(document, "document must not be null");
    }

    private Node getParent() {
        if (!elements.isEmpty()) {
            return (Node) elements.get(elements.size() - 1);
        }
        else {
            return node;
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Node parent = getParent();
        Element element = document.createElementNS(uri, qName);
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrUri = attributes.getURI(i);
            String attrQname = attributes.getQName(i);
            String value = attributes.getValue(i);
            if (!attrQname.startsWith("xmlns")) {
                element.setAttributeNS(attrUri, attrQname, value);
            }
        }
        element = (Element) parent.appendChild(element);
        elements.add(element);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        elements.remove(elements.size() - 1);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        String data = new String(ch, start, length);
        Node parent = getParent();
        Node lastChild = parent.getLastChild();
        if (lastChild != null && lastChild.getNodeType() == Node.TEXT_NODE) {
            ((Text) lastChild).appendData(data);
        }
        else {
            Text text = document.createTextNode(data);
            parent.appendChild(text);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        Node parent = getParent();
        ProcessingInstruction pi = document.createProcessingInstruction(target, data);
        parent.appendChild(pi);
    }

    /*
     * Unsupported
     */

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
