/**
 * ParameterValues.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.mackeybros.WebServices;

public class ParameterValues  implements java.io.Serializable {
    private com.mackeybros.WebServices.ArrayOfString rateCharts;
    private com.mackeybros.WebServices.ArrayOfString svcCodes;
    private com.mackeybros.WebServices.ArrayOfString pkgTypes;

    public ParameterValues() {
    }

    public ParameterValues(
           com.mackeybros.WebServices.ArrayOfString rateCharts,
           com.mackeybros.WebServices.ArrayOfString svcCodes,
           com.mackeybros.WebServices.ArrayOfString pkgTypes) {
           this.rateCharts = rateCharts;
           this.svcCodes = svcCodes;
           this.pkgTypes = pkgTypes;
    }


    /**
     * Gets the rateCharts value for this ParameterValues.
     * 
     * @return rateCharts
     */
    public com.mackeybros.WebServices.ArrayOfString getRateCharts() {
        return rateCharts;
    }


    /**
     * Sets the rateCharts value for this ParameterValues.
     * 
     * @param rateCharts
     */
    public void setRateCharts(com.mackeybros.WebServices.ArrayOfString rateCharts) {
        this.rateCharts = rateCharts;
    }


    /**
     * Gets the svcCodes value for this ParameterValues.
     * 
     * @return svcCodes
     */
    public com.mackeybros.WebServices.ArrayOfString getSvcCodes() {
        return svcCodes;
    }


    /**
     * Sets the svcCodes value for this ParameterValues.
     * 
     * @param svcCodes
     */
    public void setSvcCodes(com.mackeybros.WebServices.ArrayOfString svcCodes) {
        this.svcCodes = svcCodes;
    }


    /**
     * Gets the pkgTypes value for this ParameterValues.
     * 
     * @return pkgTypes
     */
    public com.mackeybros.WebServices.ArrayOfString getPkgTypes() {
        return pkgTypes;
    }


    /**
     * Sets the pkgTypes value for this ParameterValues.
     * 
     * @param pkgTypes
     */
    public void setPkgTypes(com.mackeybros.WebServices.ArrayOfString pkgTypes) {
        this.pkgTypes = pkgTypes;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ParameterValues)) return false;
        ParameterValues other = (ParameterValues) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.rateCharts==null && other.getRateCharts()==null) || 
             (this.rateCharts!=null &&
              this.rateCharts.equals(other.getRateCharts()))) &&
            ((this.svcCodes==null && other.getSvcCodes()==null) || 
             (this.svcCodes!=null &&
              this.svcCodes.equals(other.getSvcCodes()))) &&
            ((this.pkgTypes==null && other.getPkgTypes()==null) || 
             (this.pkgTypes!=null &&
              this.pkgTypes.equals(other.getPkgTypes())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getRateCharts() != null) {
            _hashCode += getRateCharts().hashCode();
        }
        if (getSvcCodes() != null) {
            _hashCode += getSvcCodes().hashCode();
        }
        if (getPkgTypes() != null) {
            _hashCode += getPkgTypes().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ParameterValues.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "ParameterValues"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rateCharts");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "RateCharts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "ArrayOfString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("svcCodes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "SvcCodes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "ArrayOfString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pkgTypes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "PkgTypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mackeybros.com/WebServices/", "ArrayOfString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
