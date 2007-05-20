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

package org.springframework.xml.xpath;

/**
 * Exception throws when a XPath expression cannot be parsed.
 *
 * @author Arjen Poutsma
 */
public class XPathParseException extends XPathException {

    /**
     * Constructs a new instance of the <code>XPathParseException</code> with the specific detail message.
     *
     * @param message the detail message
     */
    public XPathParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of the <code>XPathParseException</code> with the specific detail message and
     * exception.
     *
     * @param message   the detail message
     * @param throwable the wrapped exception
     */
    public XPathParseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
