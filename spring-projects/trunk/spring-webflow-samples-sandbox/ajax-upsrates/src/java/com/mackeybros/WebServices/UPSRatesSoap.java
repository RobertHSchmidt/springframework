/**
 * UPSRatesSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.mackeybros.WebServices;

public interface UPSRatesSoap extends java.rmi.Remote {

    /**
     * This function returns a "<strong>ShippingDetail</strong>" struct
     * with details of the rate request.
     */
    public com.mackeybros.WebServices.ShippingDetail getShippingRate(java.lang.String svcCode, java.lang.String rateChart, java.lang.String shipperZip, java.lang.String receiverZip, java.lang.String shipperCountry, java.lang.String receiverCountry, double pkgWeight, boolean isResidential, java.lang.String pkgType) throws java.rmi.RemoteException;

    /**
     * This function returns a set of possible values for the "<strong>GetShippingRate</strong>"
     * function
     */
    public com.mackeybros.WebServices.ParameterValues possibleValues() throws java.rmi.RemoteException;
}
