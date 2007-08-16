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

package org.springframework.ws.soap;

/**
 * Exception thrown when a SOAP body could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
public class SoapEnvelopeException extends SoapMessageException {

    public SoapEnvelopeException(String msg) {
        super(msg);
    }

    public SoapEnvelopeException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public SoapEnvelopeException(Throwable ex) {
        super("Could not access envelope: " + ex.getMessage(), ex);
    }

}
