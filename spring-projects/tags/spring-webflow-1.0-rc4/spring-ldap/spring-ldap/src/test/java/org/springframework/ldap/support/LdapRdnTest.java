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

import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.support.LdapRdn;

import junit.framework.TestCase;

/**
 * Unit test for the LdapRdn class.
 * 
 * @author Adam Skogman
 */
public class LdapRdnTest extends TestCase {

    public void testLdapRdn_parse_simple() {

        LdapRdn rdn = new LdapRdn("foo=bar");

        assertEquals("foo", rdn.getKey());
        assertEquals("bar", rdn.getValue());
        assertEquals("foo=bar", rdn.getLdapEncoded());
    }

    public void testLdapRdn_parse_spaces() {

        LdapRdn rdn = new LdapRdn(" foo = bar ");

        assertEquals("foo", rdn.getKey());
        assertEquals("bar", rdn.getValue());
        assertEquals("foo=bar", rdn.getLdapEncoded());
    }

    public void testLdapRdn_parse_escape() {

        LdapRdn rdn = new LdapRdn("foo=bar\\=fum");

        assertEquals("foo", rdn.getKey());
        assertEquals("bar=fum", rdn.getValue());
        assertEquals("foo=bar\\=fum", rdn.getLdapEncoded());
    }

    public void testLdapRdn_parse_hexEscape() {

        LdapRdn rdn = new LdapRdn("foo=bar\\0dfum");

        assertEquals("foo", rdn.getKey());
        assertEquals("bar\rfum", rdn.getValue());
        assertEquals("foo=bar\\0Dfum", rdn.getLdapEncoded());
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

        assertEquals("foo", rdn.getKey());
        assertEquals(" bar   ", rdn.getValue());
        assertEquals("foo=\\ bar  \\ ", rdn.getLdapEncoded());
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

        assertEquals("ou", rdn.getKey());
        assertEquals("Clerical / Secretarial Staff", rdn.getValue());
        assertEquals("ou=Clerical / Secretarial Staff", rdn.getLdapEncoded());
    }

    public void testLdapRdn_parse_quoteInKey() {
        try {
            new LdapRdn("\"umanroleid=2583");
            fail("Should throw BadLdapGrammarException");
        } catch (BadLdapGrammarException e) {
            assertEquals(
                    "Not a proper name (such as key=value): \"umanroleid=2583",
                    e.getMessage());
        }
    }

    public void testLdapRdn_KeyValue_simple() {
        LdapRdn rdn = new LdapRdn("foo", "bar");

        assertEquals("foo", rdn.getKey());
        assertEquals("bar", rdn.getValue());
        assertEquals("foo=bar", rdn.getLdapEncoded());
    }

    public void testLdapRdn_KeyValue_valueNeedsEscape() {
        LdapRdn rdn = new LdapRdn("foo", "bar\\");

        assertEquals("foo", rdn.getKey());
        assertEquals("bar\\", rdn.getValue());
        assertEquals("foo=bar\\\\", rdn.getLdapEncoded());
    }

    public void testEncodeUrl() {
        LdapRdn rdn = new LdapRdn("o = example.com ");
        assertEquals("o=example.com", rdn.encodeUrl());
    }

    public void testEncodeUrl_SpacesInValue() {
        LdapRdn rdn = new LdapRdn("o = my organization ");
        assertEquals("o=my%20organization", rdn.encodeUrl());
    }
}
