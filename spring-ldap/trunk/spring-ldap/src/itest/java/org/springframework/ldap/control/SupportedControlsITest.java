/*
 * Copyright 2005-2007 the original author or authors.
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

package org.springframework.ldap.control;

import java.util.Arrays;
import java.util.LinkedList;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Provides tests that verify that the server supports certain controls.
 * 
 * @author Ulrik Sandberg
 */
public class SupportedControlsITest extends AbstractLdapTemplateIntegrationTest {
    /** must use a context source that has no base set */
    private LdapTemplate tested;

    private static final String SUPPORTED_CONTROL = "supportedcontrol";

    protected String[] getConfigLocations() {
        return new String[] { "/conf/rootContextSourceTestContext.xml" };
    }

    public void testExpectedControlsSupported() throws Exception {
        /**
         * Maps the 'supportedcontrol' attribute to a string array.
         */
        final AttributesMapper mapper = new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                NamingEnumeration enumeration = attributes.get(
                        SUPPORTED_CONTROL).getAll();
                return toStringArray(enumeration);
            }
        };

        String[] controls = (String[]) tested.lookup("",
                new String[] { SUPPORTED_CONTROL }, mapper);
        System.out.println(Arrays.toString(controls));
        assertEquals("Persistent Search LDAPv3 control,",
                "2.16.840.1.113730.3.4.3", controls[0]);
        assertEquals("Entry Change Notification LDAPv3 control,",
                "2.16.840.1.113730.3.4.7", controls[1]);
        assertEquals("Subentries Control,", "1.3.6.1.4.1.4203.1.10.1",
                controls[2]);
        assertEquals("Manage DSA IT LDAPv3 control,",
                "2.16.840.1.113730.3.4.2", controls[3]);
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }

    private String[] toStringArray(NamingEnumeration enumeration) {
        LinkedList list = new LinkedList();
        while (enumeration.hasMoreElements()) {
            list.add((String) enumeration.nextElement());
        }
        return (String[]) list.toArray(new String[0]);
    }
}
