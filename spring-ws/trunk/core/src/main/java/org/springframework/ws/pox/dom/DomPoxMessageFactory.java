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
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of the {@link WebServiceMessageFactory} interface that creates a {@link DomPoxMessage}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.pox.dom.DomPoxMessage
 * @since 1.0.0
 */
public class DomPoxMessageFactory extends TransformerObjectSupport implements WebServiceMessageFactory {

    /** The default content type for the POX messages. */
    public static final String DEFAULT_CONTENT_TYPE = "application/xml";

    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private String contentType = DEFAULT_CONTENT_TYPE;

    public DomPoxMessageFactory() {
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);
    }

    /** Sets the content-type for the {@link DomPoxMessage}. */
    public void setContentType(String contentType) {
        Assert.hasLength(contentType, "'contentType' must not be empty");
        this.contentType = contentType;
    }

    /** Set whether or not the XML parser should be XML namespace aware. Default is <code>true</code>. */
    public void setNamespaceAware(boolean namespaceAware) {
        documentBuilderFactory.setNamespaceAware(namespaceAware);
    }

    /** Set if the XML parser should validate the document. Default is <code>false</code>. */
    public void setValidating(boolean validating) {
        documentBuilderFactory.setValidating(validating);
    }

    public WebServiceMessage createWebServiceMessage() {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document request = documentBuilder.newDocument();
            return new DomPoxMessage(request, createTransformer(), contentType);
        }
        catch (ParserConfigurationException ex) {
            throw new DomPoxMessageException("Could not create message context", ex);
        }
        catch (TransformerConfigurationException ex) {
            throw new DomPoxMessageException("Could not create transormer", ex);
        }
    }

    public WebServiceMessage createWebServiceMessage(InputStream inputStream) throws IOException {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document request = documentBuilder.parse(inputStream);
            return new DomPoxMessage(request, createTransformer(), contentType);
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
