/**
 * UPSRates.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.mackeybros.WebServices;

public interface UPSRates extends javax.xml.rpc.Service {

/**
 * Obtains UPS shipping rates
 */
    public java.lang.String getUPSRatesSoapAddress();

    public com.mackeybros.WebServices.UPSRatesSoap getUPSRatesSoap() throws javax.xml.rpc.ServiceException;

    public com.mackeybros.WebServices.UPSRatesSoap getUPSRatesSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
