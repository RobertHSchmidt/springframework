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

package org.springframework.ws.client.core;

import org.springframework.ws.WebServiceMessage;

/**
 * Simple fault resolver that simply throws a {@link WebServiceFaultException} when a fault occurs.
 *
 * @author Arjen Poutsma
 * @see WebServiceFaultException
 */
public class SimpleFaultResolver implements FaultResolver {

    /**
     * Throws a new <code>WebServiceFaultException</code>.
     */
    public void resolveFault(WebServiceMessage message) {
        throw new WebServiceFaultException(message.getFaultReason());
    }
}
