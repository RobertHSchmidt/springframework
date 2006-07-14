package org.springframework.ws.soap.context;

import java.util.Properties;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;

public abstract class AbstractSoap12MessageContextFactoryTestCase extends AbstractSoapMessageContextFactoryTestCase {

    public void testCreateMessageFromHttpServletRequest() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "application/soap+xml");
        headers.setProperty("SOAPAction", "\"Some-URI\"");
        AbstractSoap12MessageContextFactoryTestCase.MockTransportRequest request =
                new AbstractSoap12MessageContextFactoryTestCase.MockTransportRequest(headers, "soap12.xml");

        MessageContext messageContext = contextFactory.createContext(request);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
        assertEquals("Invalid soap action", "\"Some-URI\"", requestMessage.getSoapAction());
    }

    public void testCreateMessageFromHttpServletRequestWithAttachment() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type",
                "multipart/related; type=\"application/soap+xml\"; boundary=\"----=_Part_0_11416420.1149699787554\"");
        AbstractSoap12MessageContextFactoryTestCase.MockTransportRequest request =
                new AbstractSoap12MessageContextFactoryTestCase.MockTransportRequest(headers, "soap12-attachment.bin");

        MessageContext messageContext = contextFactory.createContext(request);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
        Attachment attachment = requestMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }

}
