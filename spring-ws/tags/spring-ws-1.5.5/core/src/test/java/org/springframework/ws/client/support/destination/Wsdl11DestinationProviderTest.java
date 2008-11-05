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

package org.springframework.ws.client.support.destination;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class Wsdl11DestinationProviderTest extends TestCase {

    private Wsdl11DestinationProvider provider;

    protected void setUp() throws Exception {
        provider = new Wsdl11DestinationProvider();
    }

    public void testSimple() throws URISyntaxException {
        Resource wsdl = new ClassPathResource("simple.wsdl", getClass());
        provider.setWsdl(wsdl);

        URI result = provider.getDestination();

        assertEquals("Invalid URI returned", new URI("http://example.com/myService"), result);
    }

    public void testComplex() throws URISyntaxException {
        Resource wsdl = new ClassPathResource("complex.wsdl", getClass());
        provider.setWsdl(wsdl);

        URI result = provider.getDestination();

        assertEquals("Invalid URI returned", new URI("http://example.com/soap11"), result);
    }

    public void testCustomExpression() throws URISyntaxException {
        provider.setLocationExpression("/wsdl:definitions/wsdl:service/wsdl:port/soap12:address/@location");
        Resource wsdl = new ClassPathResource("complex.wsdl", getClass());
        provider.setWsdl(wsdl);

        URI result = provider.getDestination();

        assertEquals("Invalid URI returned", new URI("http://example.com/soap12"), result);
    }
}