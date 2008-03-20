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

package org.springframework.ws;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;

/**
 * Sub-interface of {@link WebServiceMessage} that can contain special Fault messages. Fault messages (such as {@link
 * SoapFault} SOAP Faults) often require different processing rules.
 *
 * @author Arjen Poutsma
 * @see SoapMessage
 * @since 1.0.0
 */
public interface FaultAwareWebServiceMessage extends WebServiceMessage {

    /**
     * Does this message have a fault?
     *
     * @return <code>true</code> if the message has a fault.
     * @see #getFaultReason()
     */
    boolean hasFault();

    /**
     * Returns the fault reason message.
     *
     * @return the fault reason message, if any; returns <code>null</code> when no fault is present.
     * @see #hasFault()
     */
    String getFaultReason();
}
