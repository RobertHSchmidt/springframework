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

package org.springframework.ldap;


import org.springframework.ldap.AttributesMapper;
import org.springframework.ldap.ContextMapper;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the lookup methods of LdapTemplate.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateLookupITest extends
        AbstractDependencyInjectionSpringContextTests {
    private LdapTemplate tested;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml"};
    }

    /**
     * This method depends on a DirObjectFactory ({@link org.springframework.ldap.support.DefaultDirObjectFactory})
     * being set in the ContextSource.
     */
    public void testLookup_Plain() {
        DirContextAdapter result = (DirContextAdapter) tested
                .lookup("cn=Some Person2, ou=company1,c=Sweden,dc=jayway,dc=se");

        assertEquals("Some Person2", result.getStringAttribute("cn"));
        assertEquals("Person2", result.getStringAttribute("sn"));
        assertEquals("Sweden, Company1, Some Person2", result
                .getStringAttribute("description"));
    }

    public void testLookup_AttributesMapper() {
        AttributesMapper mapper = new PersonAttributesMapper();
        Person person = (Person) tested
                .lookup(
                        "cn=Some Person2, ou=company1,c=Sweden,dc=jayway,dc=se",
                        mapper);

        assertEquals("Some Person2", person.getFullname());
        assertEquals("Person2", person.getLastname());
        assertEquals("Sweden, Company1, Some Person2", person.getDescription());
    }

    public void testLookup_AttributesMapper_DistinguishedName() {
        AttributesMapper mapper = new PersonAttributesMapper();
        Person person = (Person) tested.lookup(new DistinguishedName(
                "cn=Some Person2, ou=company1,c=Sweden,dc=jayway,dc=se"),
                mapper);

        assertEquals("Some Person2", person.getFullname());
        assertEquals("Person2", person.getLastname());
        assertEquals("Sweden, Company1, Some Person2", person.getDescription());
    }

    /**
     * This method depends on a DirObjectFactory ({@link org.springframework.ldap.support.DefaultDirObjectFactory})
     * being set in the ContextSource.
     */
    public void testLookup_ContextMapper() {
        ContextMapper mapper = new PersonContextMapper();
        Person person = (Person) tested
                .lookup(
                        "cn=Some Person2, ou=company1,c=Sweden,dc=jayway,dc=se",
                        mapper);

        assertEquals("Some Person2", person.getFullname());
        assertEquals("Person2", person.getLastname());
        assertEquals("Sweden, Company1, Some Person2", person.getDescription());
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
