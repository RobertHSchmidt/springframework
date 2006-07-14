package org.springframework.ws.transport;

/**
 * @author Arjen Poutsma
 */
public interface TransportContext {

    TransportRequest getTransportRequest();

    TransportResponse getTransportResponse();

}
