/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.support;

import junit.framework.TestCase;

import org.springframework.ldap.BadLdapGrammarException;

import com.gargoylesoftware.base.testing.EqualsTester;

/**
 * Unit test for the LdapRdn class.
 * 
 * @author Adam Skogman
 */
public class LdapRdnTest extends TestCase {

    public void testLdapRdn_parse_simple() {

        LdapRdn rdn = new LdapRdn("foo=bar");

        assertEquals("foo", rdn.getComponent().getKey());
        assertEquals("bar", rdn.getComponent().getValue());
        assertEquals("foo=bar", rdn.getComponent().getLdapEncoded());
    }

    public void testLdapRdn_parse_spaces() {

        LdapRdn rdn = new LdapRdn(" foo = bar ");

        assertEquals("foo", rdn.getComponent().getKey());
        assertEquals("bar", rdn.getComponent().getValue());
        assertEquals("foo=bar", rdn.getComponent().getLdapEncoded());
    }

    public void testLdapRdn_parse_escape() {

        LdapRdn rdn = new LdapRdn("foo=bar\\=fum");

        assertEquals("foo", rdn.getComponent().getKey());
        assertEquals("bar=fum", rdn.getComponent().getValue());
        assertEquals("foo=bar\\=fum", rdn.getComponent().getLdapEncoded());
    }

    public void testLdapRdn_parse_hexEscape() {

        LdapRdn rdn = new LdapRdn("foo=bar\\0dfum");

        assertEquals("foo", rdn.getComponent().getKey());
        assertEquals("bar\rfum", rdn.getComponent().getValue());
        assertEquals("foo=bar\\0Dfum", rdn.getComponent().getLdapEncoded());
    }

    public void testLdapRdn_parse_trailingBackslash() {

        try {
            new LdapRdn("foo=bar\\");
            fail("Should throw BadLdapGrammarException");
        } catch (BadLdapGrammarException e) {
            assertTrue(true);
        }
    }

    public void testLdapRdn_parse_spaces_escape() {

        LdapRdn rdn = new LdapRdn(" foo = \\ bar\\20 \\  ");

        assertEquals("foo", rdn.getComponent().getKey());
        assertEquals(" bar   ", rdn.getComponent().getValue());
        assertEquals("foo=\\ bar  \\ ", rdn.getComponent().getLdapEncoded());
    }

    public void testLdapRdn_parse_tooMuchTrim() {
        try {
            new LdapRdn("foo=bar\\");
            fail("Should throw BadLdapGrammarException");
        } catch (BadLdapGrammarException e) {
            assertTrue(true);
        }
    }

    public void testLdapRdn_parse_slash() {
        LdapRdn rdn = new LdapRdn("ou=Clerical / Secretarial Staff");

        assertEquals("ou", rdn.getComponent().getKey());
        assertEquals("Clerical / Secretarial Staff", rdn.getComponent().getValue());
        assertEquals("ou=Clerical / Secretarial Staff", rdn.getComponent().getLdapEncoded());
    }

    public void testLdapRdn_parse_quoteInKey() {
        try {
            new LdapRdn("\"umanroleid=2583");
            fail("Should throw BadLdapGrammarException");
        } catch (BadLdapGrammarException e) {
            assertTrue(true);
        }
    }

    public void testLdapRdn_KeyValue_simple() {
        LdapRdn rdn = new LdapRdn("foo", "bar");

        assertEquals("foo", rdn.getComponent().getKey());
        assertEquals("bar", rdn.getComponent().getValue());
        assertEquals("foo=bar", rdn.getComponent().getLdapEncoded());
    }

    public void testLdapRdn_KeyValue_valueNeedsEscape() {
        LdapRdn rdn = new LdapRdn("foo", "bar\\");

        assertEquals("foo", rdn.getComponent().getKey());
        assertEquals("bar\\", rdn.getComponent().getValue());
        assertEquals("foo=bar\\\\", rdn.getComponent().getLdapEncoded());
    }

    public void testEncodeUrl() {
        LdapRdn rdn = new LdapRdn("o = example.com ");
        assertEquals("o=example.com", rdn.encodeUrl());
    }

    public void testEncodeUrl_SpacesInValue() {
        LdapRdn rdn = new LdapRdn("o = my organization ");
        assertEquals("o=my%20organization", rdn.encodeUrl());
    }
    
    public void testLdapRdn_Parse_MultipleComponents(){
        LdapRdn rdn = new LdapRdn("cn=John Doe+sn=Doe");
        assertEquals("cn=John Doe", rdn.getComponent(0).encodeLdap());
        assertEquals("sn=Doe", rdn.getComponent(1).encodeLdap());
        assertEquals("cn=John Doe+sn=Doe", rdn.getLdapEncoded());
    }

    public void testEquals() throws Exception {
        // original object
        final Object originalObject = new LdapRdn("cn", "john.doe");

        // another object that has the same values as the original
        final Object identicalObject = new LdapRdn("cn", "john.doe");

        // another object with different values
        final Object differentObject = new LdapRdn("cn", "john.svensson");

        // a subclass with the same values as the original
        final Object subclassObject = new LdapRdn("cn", "john.doe") {
            private static final long serialVersionUID = 1L;
        };

        new EqualsTester(originalObject, identicalObject, differentObject,
                subclassObject);
    }
}
