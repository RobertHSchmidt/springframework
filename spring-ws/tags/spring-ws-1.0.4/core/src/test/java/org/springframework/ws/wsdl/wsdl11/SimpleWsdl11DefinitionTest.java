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

package org.springframework.ws.wsdl.wsdl11;

import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;

public class SimpleWsdl11DefinitionTest extends TestCase {

    private SimpleWsdl11Definition definition;

    protected void setUp() throws Exception {
        definition = new SimpleWsdl11Definition();
        definition.setWsdl(new ClassPathResource("complete.wsdl", getClass()));
        definition.afterPropertiesSet();
    }

    public void testGetSource() throws Exception {
        assertNotNull("No source returned", definition.getSource());
    }
}