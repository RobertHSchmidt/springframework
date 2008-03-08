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

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Message;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class SuffixBasedPortTypesProvider extends AbstractPortTypesProvider {

    /** The default suffix used to detect request elements in the schema. */
    public static final String DEFAULT_REQUEST_SUFFIX = "Request";

    /** The default suffix used to detect response elements in the schema. */
    public static final String DEFAULT_RESPONSE_SUFFIX = "Response";

    /** The default suffix used to detect fault elements in the schema. */
    public static final String DEFAULT_FAULT_SUFFIX = "Fault";

    private String requestSuffix = DEFAULT_REQUEST_SUFFIX;

    private String responseSuffix = DEFAULT_RESPONSE_SUFFIX;

    private String faultSuffix = DEFAULT_FAULT_SUFFIX;

    /**
     * Returns the suffix used to detect request elements in the schema.
     *
     * @see #DEFAULT_REQUEST_SUFFIX
     */
    public String getRequestSuffix() {
        return requestSuffix;
    }

    /**
     * Sets the suffix used to detect request elements in the schema.
     *
     * @see #DEFAULT_REQUEST_SUFFIX
     */
    public void setRequestSuffix(String requestSuffix) {
        this.requestSuffix = requestSuffix;
    }

    /**
     * Returns the suffix used to detect response elements in the schema.
     *
     * @see #DEFAULT_RESPONSE_SUFFIX
     */
    public String getResponseSuffix() {
        return responseSuffix;
    }

    /**
     * Sets the suffix used to detect response elements in the schema.
     *
     * @see #DEFAULT_RESPONSE_SUFFIX
     */
    public void setResponseSuffix(String responseSuffix) {
        this.responseSuffix = responseSuffix;
    }

    /**
     * Returns the suffix used to detect fault elements in the schema.
     *
     * @see #DEFAULT_FAULT_SUFFIX
     */
    public String getFaultSuffix() {
        return faultSuffix;
    }

    /**
     * Sets the suffix used to detect fault elements in the schema.
     *
     * @see #DEFAULT_FAULT_SUFFIX
     */
    public void setFaultSuffix(String faultSuffix) {
        this.faultSuffix = faultSuffix;
    }

    protected String getOperationName(Message message) {
        String messageName = getMessageName(message);
        if (messageName != null) {
            if (messageName.endsWith(getRequestSuffix())) {
                return messageName.substring(0, messageName.length() - getRequestSuffix().length());
            }
            else if (messageName.endsWith(getResponseSuffix())) {
                return messageName.substring(0, messageName.length() - getResponseSuffix().length());
            }
            else if (messageName.endsWith(getFaultSuffix())) {
                return messageName.substring(0, messageName.length() - getFaultSuffix().length());
            }
        }
        return null;
    }

    /**
     * Indicates whether the given name name should be included as {@link javax.wsdl.Input} message in the definition.
     * <p/>
     * This implementation checks whether the message name ends with the {@link #setRequestSuffix(String)
     * requestSuffix}.
     *
     * @param message the message
     * @return <code>true</code> if to be included as input; <code>false</code> otherwise
     */
    protected boolean isInputMessage(Message message) {
        String messageName = getMessageName(message);
        return messageName != null && messageName.endsWith(getRequestSuffix());
    }

    /**
     * Indicates whether the given name name should be included as {@link javax.wsdl.Output} message in the definition.
     * <p/>
     * This implementation checks whether the message name ends with the {@link #setResponseSuffix(String)
     * responseSuffix}.
     *
     * @param message the message
     * @return <code>true</code> if to be included as output; <code>false</code> otherwise
     */
    protected boolean isOutputMessage(Message message) {
        String messageName = getMessageName(message);
        return messageName != null && messageName.endsWith(getResponseSuffix());
    }

    /**
     * Indicates whether the given name name should be included as {@link javax.wsdl.Fault} message in the definition.
     * <p/>
     * This implementation checks whether the message name ends with the {@link #setFaultSuffix(String) faultSuffix}.
     *
     * @param message the message
     * @return <code>true</code> if to be included as fault; <code>false</code> otherwise
     */
    protected boolean isFaultMessage(Message message) {
        String messageName = getMessageName(message);
        return messageName != null && messageName.endsWith(getFaultSuffix());
    }

    private String getMessageName(Message message) {
        return message.getQName().getLocalPart();
    }
}
