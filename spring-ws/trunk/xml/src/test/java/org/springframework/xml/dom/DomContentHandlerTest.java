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

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class DomContentHandlerTest extends XMLTestCase {

    private static final String XML_CONTENT_HANDLER = "<?xml version='1.0' encoding='UTF-8'?>" + "<?pi content?>" +
            "<root xmlns='namespace'>" +
            "<prefix:child xmlns:prefix='namespace2' xmlns:prefix2='namespace3' prefix2:attr='value'>content</prefix:child>" +
            "</root>";

    private Document expected;

    private DomContentHandler handler;

    private Document result;

    private XMLReader xmlReader;

    protected void setUp() throws Exception {
        xmlReader = XMLReaderFactory.createXMLReader();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        result = documentBuilder.newDocument();
        handler = new DomContentHandler(result);
        expected = documentBuilder.parse(new InputSource(new StringReader(XML_CONTENT_HANDLER)));
    }

    public void testContentHandler() throws Exception {
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(XML_CONTENT_HANDLER)));
        assertXMLEqual("Invalid result", expected, result);
    }
}