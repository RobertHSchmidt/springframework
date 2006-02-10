/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.propertyeditors;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class QNameEditorTest extends TestCase {

    private QNameEditor editor;

    protected void setUp() throws Exception {
        editor = new QNameEditor();
    }

    public void testNamespaceLocalPartPrefix() throws Exception {
        QName qname = new QName("namespace", "localpart", "prefix");
        doTest(qname);
    }

    public void testNamespaceLocalPart() throws Exception {
        QName qname = new QName("namespace", "localpart");
        doTest(qname);
    }

    public void testLocalPart() throws Exception {
        QName qname = new QName("localpart");
        doTest(qname);
    }

    private void doTest(QName qname) {
        editor.setValue(qname);
        String text = editor.getAsText();
        assertNotNull("getAsText returns null", text);
        editor.setAsText(text);
        QName result = (QName) editor.getValue();
        assertNotNull("getValue returns null", result);
        assertEquals("Parsed QName is not equal to original", qname, result);
    }
}