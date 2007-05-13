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

package org.springframework.ws.client;

import javax.xml.transform.TransformerException;

/**
 * Exception thrown whenever an transformation error occurs on the client-side.
 *
 * @author Arjen Poutsma
 */
public class WebServiceTransformerException extends WebServiceClientException {

    public WebServiceTransformerException(String msg) {
        super(msg);
    }

    public WebServiceTransformerException(String msg, TransformerException ex) {
        super(msg, ex);
    }
}
