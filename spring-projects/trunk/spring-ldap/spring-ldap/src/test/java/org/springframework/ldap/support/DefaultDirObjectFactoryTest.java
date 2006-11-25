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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;


import org.easymock.MockControl;
import org.springframework.ldap.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;

public class DefaultDirObjectFactoryTest extends TestCase {

    private MockControl contextControl;

    private Context contextMock;

    private static final Name DN = new DistinguishedName(
            "ou=some unit, dc=jayway, dc=se");

    private DefaultDirObjectFactory tested;

    protected void setUp() throws Exception {
        super.setUp();

        contextControl = MockControl.createControl(Context.class);
        contextMock = (Context) contextControl.getMock();

        tested = new DefaultDirObjectFactory();
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        contextControl = null;
        contextMock = null;

        tested = null;
    }

    protected void replay() {
        contextControl.replay();
    }

    protected void verify() {
        contextControl.verify();
    }

    public void testGetObjectInstance() throws Exception {
        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("someAttribute", "someValue");

        contextMock.close();

        replay();

        DirContextAdapter adapter = (DirContextAdapter) tested
                .getObjectInstance(contextMock, DN, null, new Hashtable(),
                        expectedAttributes);

        verify();

        assertEquals(DN, adapter.getDn());
        assertEquals(expectedAttributes, adapter.getAttributes());
    }

    public void testGetObjectInstance_nullObject() throws Exception {
        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("someAttribute", "someValue");

        replay();

        DirContextAdapter adapter = (DirContextAdapter) tested
                .getObjectInstance(null, DN, null, new Hashtable(),
                        expectedAttributes);

        verify();

        assertEquals(DN, adapter.getDn());
        assertEquals(expectedAttributes, adapter.getAttributes());
    }

    public void testGetObjectInstance_ObjectNotContext() throws Exception {
        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("someAttribute", "someValue");

        replay();

        DirContextAdapter adapter = (DirContextAdapter) tested
                .getObjectInstance(new Object(), DN, null, new Hashtable(),
                        expectedAttributes);

        verify();

        assertEquals(DN, adapter.getDn());
        assertEquals(expectedAttributes, adapter.getAttributes());
    }

    /**
     * Make sure that the base suffix is stripped off from the DN.
     * 
     * @throws Exception
     */
    public void testGetObjectInstance_BaseSet() throws Exception {
        BasicAttributes expectedAttributes = new BasicAttributes();
        expectedAttributes.put("someAttribute", "someValue");
        Hashtable env = new Hashtable();
        env.put(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY,
                new DistinguishedName("dc=jayway,dc=se"));

        contextMock.close();

        replay();

        DirContextAdapter adapter = (DirContextAdapter) tested
                .getObjectInstance(contextMock, DN, null, env,
                        expectedAttributes);

        verify();

        assertEquals("ou=some unit", adapter.getDn().toString());
        assertEquals(expectedAttributes, adapter.getAttributes());
    }
}
