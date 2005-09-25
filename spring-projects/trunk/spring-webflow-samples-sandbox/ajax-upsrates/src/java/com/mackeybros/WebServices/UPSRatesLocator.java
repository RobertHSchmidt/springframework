/**
 * UPSRatesLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.mackeybros.WebServices;

public class UPSRatesLocator extends org.apache.axis.client.Service implements com.mackeybros.WebServices.UPSRates {

/**
 * Obtains UPS shipping rates
 */

    public UPSRatesLocator() {
    }


    public UPSRatesLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public UPSRatesLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for UPSRatesSoap
    private java.lang.String UPSRatesSoap_address = "http://mackeybros.com/aspx/mackeybros/WebServices/UPSRates.asmx";

    public java.lang.String getUPSRatesSoapAddress() {
        return UPSRatesSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String UPSRatesSoapWSDDServiceName = "UPSRatesSoap";

    public java.lang.String getUPSRatesSoapWSDDServiceName() {
        return UPSRatesSoapWSDDServiceName;
    }

    public void setUPSRatesSoapWSDDServiceName(java.lang.String name) {
        UPSRatesSoapWSDDServiceName = name;
    }

    public com.mackeybros.WebServices.UPSRatesSoap getUPSRatesSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(UPSRatesSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getUPSRatesSoap(endpoint);
    }

    public com.mackeybros.WebServices.UPSRatesSoap getUPSRatesSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.mackeybros.WebServices.UPSRatesSoapStub _stub = new com.mackeybros.WebServices.UPSRatesSoapStub(portAddress, this);
            _stub.setPortName(getUPSRatesSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setUPSRatesSoapEndpointAddress(java.lang.String address) {
        UPSRatesSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.mackeybros.WebServices.UPSRatesSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                com.mackeybros.WebServices.UPSRatesSoapStub _stub = new com.mackeybros.WebServices.UPSRatesSoapStub(new java.net.URL(UPSRatesSoap_address), this);
                _stub.setPortName(getUPSRatesSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("UPSRatesSoap".equals(inputPortName)) {
            return getUPSRatesSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "UPSRates");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "UPSRatesSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("UPSRatesSoap".equals(portName)) {
            setUPSRatesSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
