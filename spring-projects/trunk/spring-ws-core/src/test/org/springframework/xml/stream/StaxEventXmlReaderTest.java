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
import java.io.StringReader;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class StaxEventXmlReaderTest extends TestCase {

    private static final String XML_DTD_HANDLER =
            "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE beans PUBLIC '-//SPRING//DTD BEAN//EN' 'http://www.springframework.org/dtd/spring-beans.dtd'><beans />";

    private static final String XML_CONTENT_HANDLER =
            "<?xml version='1.0' encoding='UTF-8'?><?pi content?><root xmlns='namespace'><prefix:child xmlns:prefix='namespace2'>content</prefix:child></root>";

    private XMLInputFactory inputFactory;

    protected void setUp() throws Exception {
        inputFactory = XMLInputFactory.newInstance();
    }

    public void testContentHandler() throws SAXException, IOException, XMLStreamException {
        // record the callbacks by parsing the XML with a regular SAX parser
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MockControl control = MockControl.createStrictControl(ContentHandler.class);
        control.setDefaultMatcher(new SaxArgumentMatcher());
        ContentHandler mock = (ContentHandler) control.getMock();
        reader.setContentHandler(mock);
        reader.parse(new InputSource(new StringReader(XML_CONTENT_HANDLER)));
        control.replay();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(XML_CONTENT_HANDLER));
        StaxEventXmlReader staxEventXMLReader = new StaxEventXmlReader(eventReader);
        staxEventXMLReader.setContentHandler(mock);
        staxEventXMLReader.parse("");
        control.verify();
    }

    public void testDtdHandler() throws IOException, SAXException, XMLStreamException {
        // record the callbacks by parsing the XML with a regular SAX parser
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MockControl control = MockControl.createStrictControl(DTDHandler.class);
        control.setDefaultMatcher(new SaxArgumentMatcher());
        DTDHandler mock = (DTDHandler) control.getMock();
        reader.setDTDHandler(mock);
        reader.parse(new InputSource(new StringReader(XML_DTD_HANDLER)));
        control.replay();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(XML_DTD_HANDLER));
        StaxEventXmlReader staxEventXMLReader = new StaxEventXmlReader(eventReader);
        staxEventXMLReader.setDTDHandler(mock);
        staxEventXMLReader.parse("");
        control.verify();
    }

}

