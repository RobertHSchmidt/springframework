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

package org.springframework.ws.pox.dom;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportRequest;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of the <code>MessageContextFactory</code> interface that creates a DOM
 *
 * @author Arjen Poutsma
 * @see DomPoxMessageContext
 */
public class DomPoxMessageContextFactory implements MessageContextFactory, InitializingBean {

    private DocumentBuilderFactory documentBuilderFactory;

    private boolean namespaceAware = true;

    private TransformerFactory transformerFactory;

    private boolean validating = false;

    /**
     * Set whether or not the XML parser should be XML namespace aware. Default is <code>true</code>.
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Set if the XML parser should validate the document. Default is <code>false</code>.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public void afterPropertiesSet() throws Exception {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(validating);
        documentBuilderFactory.setNamespaceAware(namespaceAware);
        transformerFactory = TransformerFactory.newInstance();
    }

    public MessageContext createContext(TransportContext transportContext) throws IOException {
        TransportRequest transportRequest = transportContext.getTransportRequest();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document request = documentBuilder.parse(transportRequest.getInputStream());
            return new DomPoxMessageContext(request,
                    transportRequest,
                    documentBuilder,
                    transformerFactory.newTransformer());
        }
        catch (ParserConfigurationException ex) {
            throw new DomPoxMessageException("Could not create message context", ex);
        }
        catch (SAXException ex) {
            throw new DomPoxMessageException("Could not parse request message", ex);
        }
        catch (TransformerConfigurationException ex) {
            throw new DomPoxMessageException("Could not create transormer", ex);
        }
    }
}
