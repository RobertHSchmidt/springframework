package org.springframework.ws.soap.endpoint;

import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.ws.soap.support.SoapMessageUtils;

/**
 * Simple, SOAP-specific implementation of the <code>EndpointExceptionResolver</code> that stores the exception's
 * message as the fault code.
 *
 * @author Arjen Poutsma
 */
public class SimpleSoapExceptionResolver extends AbstractEndpointExceptionResolver {

    protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
        if (!(messageContext instanceof SoapMessageContext)) {
            throw new IllegalArgumentException("SimpleSoapExceptionResolver requires a SoapMessageContext");
        }
        String faultString = StringUtils.hasLength(ex.getMessage()) ? ex.getMessage() : ex.toString();
        SoapMessageContext soapContext = (SoapMessageContext) messageContext;
        SoapMessage response = soapContext.getSoapResponse();
        SoapMessageUtils.addReceiverFault(response, faultString);
        return true;
    }
}
