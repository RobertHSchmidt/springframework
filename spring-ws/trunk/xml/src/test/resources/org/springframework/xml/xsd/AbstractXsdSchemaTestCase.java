/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.xml.xsd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;

public abstract class AbstractXsdSchemaTestCase extends XMLTestCase {

    private DocumentBuilder documentBuilder;

    protected Transformer transformer;

    protected final void setUp() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testSingle() throws Exception {
        Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
        XsdSchema single = createSchema(resource);
        String namespace = "http://www.springframework.org/spring-ws/single/schema";
        assertEquals("Invalid target namespace", namespace, single.getTargetNamespace());
        QName[] elementNames = single.getElementNames();
        assertQNamesEqual(new QName[]{new QName(namespace, "GetOrderRequest"), new QName(namespace, "GetOrderResponse"),
                new QName(namespace, "GetOrderFault")}, elementNames);
        resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
        Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
        DOMResult domResult = new DOMResult();
        transformer.transform(single.getSource(), domResult);
        Document result = (Document) domResult.getNode();
        assertXMLEqual("Invalid Source returned", expected, result);
    }

    public void testIncludes() throws Exception {
        Resource resource = new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class);
        XsdSchema including = createSchema(resource);
        String namespace = "http://www.springframework.org/spring-ws/include/schema";
        assertEquals("Invalid target namespace", namespace, including.getTargetNamespace());
        QName[] elementNames = including.getElementNames();
        assertQNamesEqual(new QName[]{new QName(namespace, "GetOrderRequest")}, elementNames);
        resource = new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class);
        Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
        DOMResult domResult = new DOMResult();
        transformer.transform(including.getSource(), domResult);
        Document result = (Document) domResult.getNode();
        assertXMLEqual("Invalid Source returned", expected, result);
    }

    public void testImports() throws Exception {
        Resource resource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class);
        XsdSchema importing = createSchema(resource);
        String namespace = "http://www.springframework.org/spring-ws/importing/schema";
        assertEquals("Invalid target namespace", namespace, importing.getTargetNamespace());
        QName[] elementNames = importing.getElementNames();
        assertQNamesEqual(new QName[]{new QName(namespace, "GetOrderRequest")}, elementNames);
        resource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class);
        Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
        DOMResult domResult = new DOMResult();
        transformer.transform(importing.getSource(), domResult);
        Document result = (Document) domResult.getNode();
        assertXMLEqual("Invalid Source returned", expected, result);
    }

    private void assertQNamesEqual(QName[] expected, QName[] result) {
        Set expectedSet = new HashSet(Arrays.asList(expected));
        Set resultSet = new HashSet(Arrays.asList(result));
        assertEquals("Invalid QNames", expectedSet, resultSet);
    }

    protected abstract XsdSchema createSchema(Resource resource) throws Exception;
}
