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

package org.springframework.oxm.jaxb;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Collections;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.easymock.MockControl;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb2.FlightType;
import org.springframework.oxm.jaxb2.Flights;
import org.springframework.xml.transform.StaxResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;

public class Jaxb2MarshallerTest extends XMLTestCase {

    private static final String CONTEXT_PATH = "org.springframework.oxm.jaxb2";

    private static final String EXPECTED_STRING =
            "<tns:flights xmlns:tns=\"http://samples.springframework.org/flight\">" +
                    "<tns:flight><tns:number>42</tns:number></tns:flight></tns:flights>";

    private Jaxb2Marshaller marshaller;

    private Flights flights;

    protected void setUp() throws Exception {
        marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CONTEXT_PATH);
        marshaller.afterPropertiesSet();
        FlightType flight = new FlightType();
        flight.setNumber(42L);
        flights = new Flights();
        flights.getFlight().add(flight);
    }

    public void testMarshalDOMResult() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document document = builder.newDocument();
        DOMResult domResult = new DOMResult(document);
        marshaller.marshal(flights, domResult);
        Document expected = builder.newDocument();
        Element flightsElement = expected.createElementNS("http://samples.springframework.org/flight", "tns:flights");
        expected.appendChild(flightsElement);
        Element flightElement = expected.createElementNS("http://samples.springframework.org/flight", "tns:flight");
        flightsElement.appendChild(flightElement);
        Element numberElement = expected.createElementNS("http://samples.springframework.org/flight", "tns:number");
        flightElement.appendChild(numberElement);
        Text text = expected.createTextNode("42");
        numberElement.appendChild(text);
        assertXMLEqual("Marshaller writes invalid DOMResult", expected, document);
    }

    public void testMarshalStreamResultWriter() throws Exception {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        marshaller.marshal(flights, result);
        assertXMLEqual("Marshaller writes invalid StreamResult", EXPECTED_STRING, writer.toString());
    }

    public void testMarshalStreamResultOutputStream() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(os);
        marshaller.marshal(flights, result);
        assertXMLEqual("Marshaller writes invalid StreamResult", EXPECTED_STRING,
                new String(os.toByteArray(), "UTF-8"));
    }

    public void testMarshalStaxResultXMLStreamWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        StringWriter writer = new StringWriter();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(writer);
        StaxResult result = new StaxResult(streamWriter);
        marshaller.marshal(flights, result);
        assertXMLEqual("Marshaller writes invalid StreamResult", EXPECTED_STRING, writer.toString());
    }

    public void testMarshalStaxResultXMLEventWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        StringWriter writer = new StringWriter();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(writer);
        StaxResult result = new StaxResult(eventWriter);
        marshaller.marshal(flights, result);
        assertXMLEqual("Marshaller writes invalid StreamResult", EXPECTED_STRING, writer.toString());
    }

    public void testProperties() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CONTEXT_PATH);
        marshaller.setMarshallerProperties(
                Collections.singletonMap(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE));
        marshaller.afterPropertiesSet();
    }

    public void testNoContextPathOrClassesToBeBound() throws Exception {
        try {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.afterPropertiesSet();
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void testInvalidContextPath() throws Exception {
        try {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setContextPath("ab");
            marshaller.afterPropertiesSet();
            fail("Should have thrown an XmlMappingException");
        }
        catch (XmlMappingException ex) {
        }
    }

    public void testMarshalInvalidClass() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(new Class[]{FlightType.class});
        marshaller.afterPropertiesSet();
        Result result = new StreamResult(new StringWriter());
        Flights flights = new Flights();
        try {
            marshaller.marshal(flights, result);
            fail("Should have thrown an MarshallingFailureException");
        }
        catch (XmlMappingException ex) {
            // expected
        }
    }

    public void testMarshalSaxResult() throws Exception {
        MockControl handlerControl = MockControl.createStrictControl(ContentHandler.class);
        ContentHandler handlerMock = (ContentHandler) handlerControl.getMock();
        handlerMock.setDocumentLocator(null);
        handlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        handlerMock.startDocument();
        handlerMock.startPrefixMapping("", "http://samples.springframework.org/flight");
        handlerMock.startElement("http://samples.springframework.org/flight", "flights", "flights", null);
        handlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        handlerMock.startElement("http://samples.springframework.org/flight", "flight", "flight", null);
        handlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        handlerMock.startElement("http://samples.springframework.org/flight", "number", "number", null);
        handlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        handlerMock.characters(new char[]{'4', '2'}, 0, 2);
        handlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        handlerMock.endElement("http://samples.springframework.org/flight", "number", "number");
        handlerMock.endElement("http://samples.springframework.org/flight", "flight", "flight");
        handlerMock.endElement("http://samples.springframework.org/flight", "flights", "flights");
        handlerMock.endPrefixMapping("");
        handlerMock.endDocument();

        handlerControl.replay();
        SAXResult result = new SAXResult(handlerMock);
        marshaller.marshal(flights, result);
        handlerControl.verify();
    }

    public void testSupports() throws Exception {
        assertTrue("Jaxb2Marshaller does not support Flights", marshaller.supports(Flights.class));
        assertTrue("Jaxb2Marshaller does not support JAXBElement", marshaller.supports(JAXBElement.class));
    }
}
