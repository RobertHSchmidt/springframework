package org.springframework.ws.soap.endpoint;

import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.AbstractLoggingInterceptor;
import org.springframework.ws.soap.SoapEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;

/**
 * SOAP-specific <code>EndpointInterceptor</code> that logs the complete request and response
 * <code>SoapMessage</code>messages. By default, request, response and fault messages are logged, but this behaviour can
 * be changed using the <code>logRequest</code>, <code>logResponse</code>, <code>logFault</code> properties.
 *
 * @author Arjen Poutsma
 * @see #setLogRequest(boolean)
 * @see #setLogResponse(boolean)
 * @see #setLogFault(boolean)
 */
public class SoapMessageLoggingInterceptor extends AbstractLoggingInterceptor implements SoapEndpointInterceptor {

    private boolean logFault = true;

    /**
     * Indicates whether a SOAP Fault should be logged. Default is <code>true</code>.
     */
    public void setLogFault(boolean logFault) {
        this.logFault = logFault;
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        if (logFault && logger.isDebugEnabled()) {
            logMessageSource("Fault: ", getSource(messageContext.getResponse()));
        }
        return true;
    }

    public boolean understands(SoapHeaderElement header) {
        return false;
    }

    protected Source getSource(WebServiceMessage message) {
        if (message instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) message;
            return soapMessage.getEnvelope().getSource();
        }
        else {
            return null;
        }
    }
}
