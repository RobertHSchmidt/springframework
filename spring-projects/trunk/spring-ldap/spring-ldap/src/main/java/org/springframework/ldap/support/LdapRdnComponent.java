package org.springframework.ldap.support;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Represents part of an LdapRdn. As specified in RFC2253 an LdapRdn may be
 * composed of several attributes, separated by &quot;+&quot;. An
 * LdapRdnComponent represents one of these attributes.
 * 
 * @author Mattias Arthursson
 * 
 */
public class LdapRdnComponent {
    public static final boolean DONT_DECODE_VALUE = false;

    private String key;

    private String value;

    /**
     * Constructs an LdapRdnComponent without decoding the value.
     * 
     * @param key
     *            the Atttribute name.
     * @param value
     *            the Attribute value.
     */
    public LdapRdnComponent(String key, String value) {
        this(key, value, DONT_DECODE_VALUE);
    }

    /**
     * Constructs an LdapRdnComponent, optionally decoding the value.
     * 
     * @param key
     *            the Atttribute name.
     * @param value
     *            the Attribute value.
     * @param decodeValue
     *            if <code>true</code> the value is decoded (typically used
     *            when a DN is parsed from a String), otherwise the value is
     *            used as specified.
     */
    public LdapRdnComponent(String key, String value, boolean decodeValue) {
        Validate.notEmpty(key, "Key must not be empty");
        Validate.notEmpty(value, "Value must not be empty");

        this.key = StringUtils.lowerCase(key);
        if (decodeValue) {
            this.value = LdapEncoder.nameDecode(value);
        } else {
            this.value = value;
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Encode key and value to ldap
     * 
     * @return The ldap encoded rdn
     */
    protected String encodeLdap() {
        StringBuffer buff = new StringBuffer(key.length() + value.length() * 2);

        buff.append(key);
        buff.append('=');
        buff.append(LdapEncoder.nameEncode(value));

        return buff.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getLdapEncoded();
    }

    /**
     * @return The LdapRdn as a string where the value is LDAP-encoded.
     */
    public String getLdapEncoded() {
        return encodeLdap();
    }

    public String encodeUrl() {
        // Use the URI class to properly URL encode the value.
        try {
            URI valueUri = new URI(null, null, value, null);
            return key + "=" + valueUri.toString();
        } catch (URISyntaxException e) {
            // This should really never happen...
            return key + "=" + "value";
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return key.hashCode() ^ value.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == LdapRdnComponent.class) {
            LdapRdnComponent that = (LdapRdnComponent) obj;
            return StringUtils.equalsIgnoreCase(this.key, that.key)
                    && StringUtils.equalsIgnoreCase(this.value, that.value);

        } else {
            return false;
        }
    }
}
