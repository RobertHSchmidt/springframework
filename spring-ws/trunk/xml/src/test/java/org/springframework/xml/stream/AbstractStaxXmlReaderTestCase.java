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

package org.springframework.xml.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;

public abstract class AbstractStaxXmlReaderTestCase extends TestCase {

    protected static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private Resource testContentHandler;

    private XMLReader standardReader;

    private MockControl contentHandlerControl;

    private ContentHandler contentHandler;

    protected void setUp() throws Exception {
        standardReader = XMLReaderFactory.createXMLReader();
        contentHandlerControl = MockControl.createStrictControl(ContentHandler.class);
        contentHandlerControl.setDefaultMatcher(new SaxArgumentMatcher());
        ContentHandler contentHandlerMock = (ContentHandler) contentHandlerControl.getMock();
        contentHandler = new CopyingContentHandler(contentHandlerMock);
        standardReader.setContentHandler(contentHandler);

        testContentHandler = new ClassPathResource("testContentHandler.xml", getClass());
    }

    public void testContentHandlerNamespacesNoPrefixes() throws SAXException, IOException, XMLStreamException {
        standardReader.setFeature("http://xml.org/sax/features/namespaces", true);
        standardReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);

        standardReader.parse(SaxUtils.createInputSource(testContentHandler));
        contentHandlerControl.replay();

        AbstractStaxXmlReader staxXmlReader = createStaxXmlReader(testContentHandler.getInputStream());
        staxXmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        staxXmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);

        staxXmlReader.setContentHandler(contentHandler);
        staxXmlReader.parse(new InputSource());
        contentHandlerControl.verify();
    }

    public void testContentHandlerNamespacesPrefixes() throws SAXException, IOException, XMLStreamException {
        standardReader.setFeature("http://xml.org/sax/features/namespaces", true);
        standardReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        standardReader.parse(SaxUtils.createInputSource(testContentHandler));
        contentHandlerControl.replay();

        AbstractStaxXmlReader staxXmlReader = createStaxXmlReader(testContentHandler.getInputStream());
        staxXmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        staxXmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        staxXmlReader.setContentHandler(contentHandler);
        staxXmlReader.parse(new InputSource());
        contentHandlerControl.verify();
    }

    public void testContentHandlerNoNamespacesPrefixes() throws SAXException, IOException, XMLStreamException {
        standardReader.setFeature("http://xml.org/sax/features/namespaces", false);
        standardReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        standardReader.parse(SaxUtils.createInputSource(testContentHandler));
        contentHandlerControl.replay();

        AbstractStaxXmlReader staxXmlReader = createStaxXmlReader(testContentHandler.getInputStream());
        staxXmlReader.setFeature("http://xml.org/sax/features/namespaces", false);
        staxXmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        staxXmlReader.setContentHandler(contentHandler);
        staxXmlReader.parse(new InputSource());
        contentHandlerControl.verify();
    }

    protected abstract AbstractStaxXmlReader createStaxXmlReader(InputStream inputStream) throws XMLStreamException;

    /** Easymock <code>ArgumentMatcher</code> implementation that matches SAX arguments. */
    protected static class SaxArgumentMatcher extends AbstractMatcher {

        public boolean matches(Object[] expected, Object[] actual) {
            if (expected == actual) {
                return true;
            }
            if (expected == null || actual == null) {
                return false;
            }
            if (expected.length != actual.length) {
                throw new IllegalArgumentException("Expected and actual arguments must have the same size");
            }
            if (expected.length == 3 && expected[0] instanceof char[] && expected[1] instanceof Integer &&
                    expected[2] instanceof Integer) {
                // handling of the character(char[], int, int) methods
                String expectedString = new String((char[]) expected[0], ((Integer) expected[1]).intValue(),
                        ((Integer) expected[2]).intValue());
                String actualString = new String((char[]) actual[0], ((Integer) actual[1]).intValue(),
                        ((Integer) actual[2]).intValue());
                return expectedString.equals(actualString);
            }
            else if (expected.length == 1 && (expected[0] instanceof Locator)) {
                return true;
            }
            else {
                return super.matches(expected, actual);
            }
        }

        protected boolean argumentMatches(Object expected, Object actual) {
            if (expected instanceof char[]) {
                return Arrays.equals((char[]) expected, (char[]) actual);
            }
            else if (expected instanceof Attributes) {
                Attributes expectedAttributes = (Attributes) expected;
                Attributes actualAttributes = (Attributes) actual;
                if (expectedAttributes.getLength() != actualAttributes.getLength()) {
                    return false;
                }
                for (int i = 0; i < expectedAttributes.getLength(); i++) {
                    boolean found = false;
                    for (int j = 0; j < actualAttributes.getLength(); j++) {
                        if (expectedAttributes.getURI(i).equals(actualAttributes.getURI(j)) &&
                                expectedAttributes.getQName(i).equals(actualAttributes.getQName(j)) &&
//                                expectedAttributes.getLocalName(i).equals(actualAttributes.getLocalName(j)) &&
                                expectedAttributes.getType(i).equals(actualAttributes.getType(j)) &&
                                expectedAttributes.getValue(i).equals(actualAttributes.getValue(j))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return false;
                    }
                }
                return true;
            }
            else {
                return super.argumentMatches(expected, actual);
            }
        }

        public String toString(Object[] arguments) {
            if (arguments != null && arguments.length == 3 && arguments[0] instanceof char[] &&
                    arguments[1] instanceof Integer && arguments[2] instanceof Integer) {
                return new String((char[]) arguments[0], ((Integer) arguments[1]).intValue(),
                        ((Integer) arguments[2]).intValue());
            }
            else {
                return super.toString(arguments);
            }
        }

        protected String argumentToString(Object argument) {
            if (argument instanceof char[]) {
                char[] array = (char[]) argument;
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < array.length; i++) {
                    buffer.append(array[i]);
                }
                return buffer.toString();
            }
            else if (argument instanceof Attributes) {
                Attributes attributes = (Attributes) argument;
                StringBuffer buffer = new StringBuffer("[");
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.getURI(i).length() != 0) {
                        buffer.append('{');
                        buffer.append(attributes.getURI(i));
                        buffer.append('}');
                    }
//                    if (attributes.getLocalName(i).length() != 0) {
//                        buffer.append('[');
//                        buffer.append(attributes.getLocalName(i));
//                        buffer.append(']');
//                    }
                    if (attributes.getQName(i).length() != 0) {
                        buffer.append(attributes.getQName(i));
                    }
                    buffer.append('=');
                    buffer.append(attributes.getValue(i));
                    if (i < attributes.getLength() - 1) {
                        buffer.append(", ");
                    }
                }
                buffer.append(']');
                return buffer.toString();
            }
            else if (argument instanceof Locator) {
                Locator locator = (Locator) argument;
                StringBuffer buffer = new StringBuffer("[");
                buffer.append(locator.getLineNumber());
                buffer.append(',');
                buffer.append(locator.getColumnNumber());
                buffer.append(']');
                return buffer.toString();
            }
            else {
                return super.argumentToString(argument);
            }
        }
    }

    private static class CopyingContentHandler implements ContentHandler {

        private ContentHandler wrappee;

        private CopyingContentHandler(ContentHandler wrappee) {
            this.wrappee = wrappee;
        }

        public void setDocumentLocator(Locator locator) {
            wrappee.setDocumentLocator(locator);
        }

        public void startDocument() throws SAXException {
            wrappee.startDocument();
        }

        public void endDocument() throws SAXException {
            wrappee.endDocument();
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            wrappee.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            wrappee.endPrefixMapping(prefix);
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            wrappee.startElement(uri, localName, qName, new AttributesImpl(attributes));
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            wrappee.endElement(uri, localName, qName);
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            wrappee.characters(copy(ch), start, length);
        }

        public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
            wrappee.processingInstruction(target, data);
        }

        public void skippedEntity(String name) throws SAXException {
            wrappee.skippedEntity(name);
        }

    }

    private static char[] copy(char[] ch) {
        char[] copy = new char[ch.length];
        System.arraycopy(ch, 0, copy, 0, ch.length);
        return copy;
    }

}
