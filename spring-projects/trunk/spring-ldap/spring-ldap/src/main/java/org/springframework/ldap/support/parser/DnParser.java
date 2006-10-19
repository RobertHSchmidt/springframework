package org.springframework.ldap.support.parser;

import org.springframework.ldap.support.DistinguishedName;
import org.springframework.ldap.support.LdapRdn;

/**
 * A parser for RFC2253-compliant Distinguished Names.
 * 
 * @author Mattias Arthursson
 * 
 */
public interface DnParser {
    /**
     * Parse a full Distinguished Name.
     * 
     * @return the DistinguishedName corresponding to the parsed stream.
     */
    public DistinguishedName dn() throws ParseException;

    /**
     * Parse an Relative Distinguished Name.
     * 
     * @return the next rdn on the stream.
     */
    public LdapRdn rdn() throws ParseException;
}
