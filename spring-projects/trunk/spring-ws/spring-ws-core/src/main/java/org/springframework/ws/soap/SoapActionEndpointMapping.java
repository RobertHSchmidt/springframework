/*
 * Copyright 2005 the original author or authors.
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

import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.endpoint.AbstractMapBasedSoapEndpointMapping;

/**
 * Implementation of the <code>EndpointMapping</code> interface to map from <code>SOAPAction</code> headers to endpoint
 * beans. Supports both mapping to bean instances and mapping to bean names: the latter is required for prototype
 * handlers.
 * <p/>
 * The <code>endpointMap</code> property is suitable for populating the endpoint map with bean references, e.g. via the
 * map element in XML bean definitions.
 * <p/>
 * Mappings to bean names can be set via the <code>mappings</code> property, in a form accepted by the
 * <code>java.util.Properties</code> class, like as follows:
 * <pre>
 * http://www.springframework.org/spring-ws/samples/airline/BookFlight=bookFlightEndpoint
 * http://www.springframework.org/spring-ws/samples/airline/GetFlights=getFlightsEndpoint
 * </pre>
 * The syntax is SOAP_ACTION=ENDPOINT_BEAN_NAME.
 * <p/>
 * This endpoint mapping does not read from the request message, and therefore is more suitable for message contexts
 * which directly read from the transport request (such as the <code>AxiomSoapMessageContextFactory</code> with the
 * <code>payloadCaching</code> disabled).
 *
 * @author Arjen Poutsma
 * @see SoapMessage#getSoapAction()
 */
public class SoapActionEndpointMapping extends AbstractMapBasedSoapEndpointMapping {

    protected boolean validateLookupKey(String key) {
        return StringUtils.hasLength(key);
    }

    protected String getLookupKeyForMessage(WebServiceMessage message) throws Exception {
        String soapAction = ((SoapMessage) message).getSoapAction();
        if (StringUtils.hasLength(soapAction) && soapAction.charAt(0) == '"' &&
                soapAction.charAt(soapAction.length() - 1) == '"') {
            return soapAction.substring(1, soapAction.length() - 1);
        }
        else {
            return soapAction;
        }
    }
}
