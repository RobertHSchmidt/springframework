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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import junit.framework.TestCase;
import org.w3c.dom.Element;

public class StringSourceTest extends TestCase {

    public void testStringSource() throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        String content = "<prefix:content xmlns:prefix='namespace'/>";
        DOMResult result = new DOMResult();
        transformer.transform(new StringSource(content), result);
        Element rootElement = (Element) result.getNode().getFirstChild();
        assertEquals("content", rootElement.getLocalName());
        assertEquals("prefix", rootElement.getPrefix());
        assertEquals("namespace", rootElement.getNamespaceURI());
    }
}