package org.springframework.ws.soap.addressing.version;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.EndpointReference;
import org.springframework.ws.soap.addressing.MessageAddressingProperties;

/**
 * Defines the contract for a specific version of the WS-Addressing specification.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public interface WsAddressingVersion {

    /**
     * Returns the {@link MessageAddressingProperties} for the given message.
     *
     * @param message the message to find the map for
     * @return the message addressing properties
     * @see <a href="http://www.w3.org/TR/ws-addr-core/#msgaddrprops">Message Addressing Properties</a>
     */
    MessageAddressingProperties getMessageAddressingProperties(SoapMessage message);

    /**
     * Adds addressing SOAP headers to the given message, using the given {@link MessageAddressingProperties}.
     *
     * @param message the message to add the headers to
     * @param map     the message addressing properties
     */
    void addAddressingHeaders(SoapMessage message, MessageAddressingProperties map);

    /**
     * Given a <code>SoapHeaderElement</code>, return whether or not this version understands it.
     *
     * @param headerElement the header
     * @return <code>true</code> if understood, <code>false</code> otherwise
     */
    boolean understands(SoapHeaderElement headerElement);

    /*
     * Address URIs
     */

    /**
     * Indicates whether the given endpoint reference has a Anonymous address. This address is used to indicate that a
     * message should be sent in-band.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-core/#formreplymsg">Formulating a Reply Message</a>
     */
    boolean hasAnonymousAddress(EndpointReference epr);

    /**
     * Indicates whether the given endpoint reference has a None address. Messages to be sent to this address will not
     * be sent.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-core/#sendmsgepr">Sending a Message to an EPR</a>
     */
    boolean hasNoneAddress(EndpointReference epr);

    /*
     * Faults
     */

    /**
     * Adds a Invalid Addressing Header fault to the given message.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-soap/#invalidmapfault">Invalid Addressing Header</a>
     */
    SoapFault addInvalidAddressingHeaderFault(SoapMessage message);

    /**
     * Adds a Message Addressing Header Required fault to the given message.
     *
     * @see <a href="http://www.w3.org/TR/ws-addr-soap/#missingmapfault">Message Addressing Header Required</a>
     */
    SoapFault addMessageAddressingHeaderRequiredFault(SoapMessage message);

}
