/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.xpath;

import java.util.Properties;
import javax.xml.transform.Source;

import org.springframework.xml.transform.TransformerObjectSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Abstract base class for implementations of {@link XPathOperations}. Contains a namespaces property.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractXPathTemplate extends TransformerObjectSupport implements XPathOperations {

    private Properties namespaces;

    /** Returns namespaces used in the XPath expression. */
    public Properties getNamespaces() {
        return namespaces;
    }

    /** Sets namespaces used in the XPath expression. Maps prefixes to namespaces. */
    public void setNamespaces(Properties namespaces) {
        this.namespaces = namespaces;
    }

    public final void evaluate(String expression, Source context, NodeCallbackHandler callbackHandler)
            throws XPathException {
        evaluate(expression, context, new NodeCallbackHandlerNodeMapper(callbackHandler));
    }

    /** Static inner class that adapts a {@link NodeCallbackHandler} to the interface of {@link NodeMapper}. */
    private static class NodeCallbackHandlerNodeMapper implements NodeMapper {

        private final NodeCallbackHandler callbackHandler;

        public NodeCallbackHandlerNodeMapper(NodeCallbackHandler callbackHandler) {
            this.callbackHandler = callbackHandler;
        }

        public Object mapNode(Node node, int nodeNum) throws DOMException {
            callbackHandler.processNode(node);
            return null;
        }
    }
}
