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

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.support.DistinguishedName;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests for LdapTemplate's list methods.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateListITest extends
        AbstractDependencyInjectionSpringContextTests {

    private LdapTemplate tested;

    private AttributeCheckContextMapper contextMapper;

    private static final String BASE_STRING = "dc=jayway,dc=se";

    private static final Name BASE_NAME = new DistinguishedName(BASE_STRING);

    private static final String[] ALL_ATTRIBUTES = { "cn", "sn", "description",
            "telephoneNumber" };

    private static final String[] ALL_VALUES = { "Some Person", "Person",
            "Sweden, Company2, Some Person", "Some Person Phone" };

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        super.onSetUp();

        contextMapper = new AttributeCheckContextMapper();
    }

    protected void onTearDown() throws Exception {
        super.onTearDown();

        contextMapper = null;
    }

    public void testListBindings_ContextMapper() {
        contextMapper.setExpectedAttributes(ALL_ATTRIBUTES);
        contextMapper.setExpectedValues(ALL_VALUES);
        List list = tested.listBindings("ou=company2,c=Sweden," + BASE_STRING, contextMapper);
        assertEquals(1, list.size());
    }

    public void testList() {
        List list = tested.list(BASE_STRING);
        assertEquals(2, list.size());
        assertEquals("c=Sweden", list.get(0));
        assertEquals("c=Norway", list.get(1));
    }

    public void testList_Name() {
        List list = tested.list(BASE_NAME);
        assertEquals(2, list.size());
        assertEquals("c=Sweden", list.get(0));
        assertEquals("c=Norway", list.get(1));
    }

    public void testList_Handler() throws Exception {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
        tested.list(BASE_STRING, handler);
        assertEquals(2, handler.getNoOfRows());
    }

    public void testList_Name_Handler() throws Exception {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
        tested.list(BASE_NAME, handler);
        assertEquals(2, handler.getNoOfRows());
    }

    public void testListBindings() {
        List list = tested.listBindings(BASE_STRING);
        assertEquals(2, list.size());
        assertEquals("c=Sweden", list.get(0));
        assertEquals("c=Norway", list.get(1));
    }

    public void testListBindings_Name() {
        List list = tested.listBindings(BASE_NAME);
        assertEquals(2, list.size());
        assertEquals("c=Sweden", list.get(0));
        assertEquals("c=Norway", list.get(1));
    }

    public void testListBindings_Handler() throws Exception {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
        tested.listBindings(BASE_STRING, handler);
        assertEquals(2, handler.getNoOfRows());
    }

    public void testListBindings_Name_Handler() throws Exception {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
        tested.listBindings(BASE_NAME, handler);
        assertEquals(2, handler.getNoOfRows());
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
