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
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * Unit tests for the <code>list</code> operations in {@link LdapTemplate}.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateListTest extends TestCase {

    private static final String NAME = "o=example.com";

    private static final String CLASS = "com.example.SomeClass";

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private MockControl namingEnumerationControl;

    private NamingEnumeration namingEnumerationMock;

    private MockControl nameControl;

    private Name nameMock;

    private MockControl handlerControl;

    private NameClassPairCallbackHandler handlerMock;

    private MockControl exceptionTranslatorControl;

    private NamingExceptionTranslator exceptionTranslatorMock;

    private LdapTemplate tested;

    protected void setUp() throws Exception {
        super.setUp();

        // Setup ContextSource mock
        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        // Setup LdapContext mock
        dirContextControl = MockControl.createControl(LdapContext.class);
        dirContextMock = (LdapContext) dirContextControl.getMock();

        // Setup NamingEnumeration mock
        namingEnumerationControl = MockControl
                .createControl(NamingEnumeration.class);
        namingEnumerationMock = (NamingEnumeration) namingEnumerationControl
                .getMock();

        // Setup Name mock
        nameControl = MockControl.createControl(Name.class);
        nameMock = (Name) nameControl.getMock();

        // Setup Handler mock
        handlerControl = MockControl
                .createControl(NameClassPairCallbackHandler.class);
        handlerMock = (NameClassPairCallbackHandler) handlerControl.getMock();

        exceptionTranslatorControl = MockControl
                .createControl(NamingExceptionTranslator.class);
        exceptionTranslatorMock = (NamingExceptionTranslator) exceptionTranslatorControl
                .getMock();

        tested = new LdapTemplate(contextSourceMock);
        tested.setExceptionTranslator(exceptionTranslatorMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        contextSourceControl = null;
        contextSourceMock = null;

        dirContextControl = null;
        dirContextMock = null;

        namingEnumerationControl = null;
        namingEnumerationMock = null;

        nameControl = null;
        nameMock = null;

        handlerControl = null;
        handlerMock = null;

        exceptionTranslatorControl = null;
        exceptionTranslatorMock = null;
    }

    protected void replay() {
        contextSourceControl.replay();
        dirContextControl.replay();
        namingEnumerationControl.replay();
        nameControl.replay();
        handlerControl.replay();
        exceptionTranslatorControl.replay();
    }

    protected void verify() {
        contextSourceControl.verify();
        dirContextControl.verify();
        namingEnumerationControl.verify();
        nameControl.verify();
        handlerControl.verify();
        exceptionTranslatorControl.verify();
    }

    private void expectGetReadOnlyContext() {
        contextSourceControl.expectAndReturn(contextSourceMock
                .getReadOnlyContext(), dirContextMock);
    }

    private void setupStringListAndNamingEnumeration(NameClassPair listResult)
            throws NamingException {
        dirContextControl.expectAndReturn(dirContextMock.list(NAME),
                namingEnumerationMock);

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                listResult);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();
    }

    private void setupListAndNamingEnumeration(NameClassPair listResult)
            throws NamingException {
        dirContextControl.expectAndReturn(dirContextMock.list(nameMock),
                namingEnumerationMock);

        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), true);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock.next(),
                listResult);
        namingEnumerationControl.expectAndReturn(namingEnumerationMock
                .hasMore(), false);
        namingEnumerationMock.close();
    }

    public void testList_Name() throws NamingException {
        expectGetReadOnlyContext();

        NameClassPair listResult = new NameClassPair(NAME, CLASS);

        setupListAndNamingEnumeration(listResult);

        dirContextMock.close();

        replay();

        List list = tested.list(nameMock);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(NAME, list.get(0));
    }

    public void testList_String() throws NamingException {
        expectGetReadOnlyContext();

        NameClassPair listResult = new NameClassPair(NAME, CLASS);

        setupStringListAndNamingEnumeration(listResult);

        dirContextMock.close();

        replay();

        List list = tested.list(NAME);

        verify();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(NAME, list.get(0));
    }

    public void testList_Name_CallbackHandler() throws NamingException {
        expectGetReadOnlyContext();

        NameClassPair listResult = new NameClassPair(NAME, CLASS);

        setupListAndNamingEnumeration(listResult);

        handlerMock.handleNameClassPair(listResult);

        dirContextMock.close();

        replay();

        tested.list(nameMock, handlerMock);

        verify();
    }

    public void testList_String_CallbackHandler() throws NamingException {
        expectGetReadOnlyContext();

        NameClassPair listResult = new NameClassPair(NAME, CLASS);

        setupStringListAndNamingEnumeration(listResult);

        handlerMock.handleNameClassPair(listResult);

        dirContextMock.close();

        replay();

        tested.list("o=example.com", handlerMock);

        verify();
    }

    public void testList_PartialResultException() throws NamingException {
        expectGetReadOnlyContext();

        PartialResultException pre = new PartialResultException();
        dirContextControl.expectAndThrow(dirContextMock.list(NAME), pre);

        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(pre), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.list(NAME);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }

    public void testList_PartialResultException_Ignore() throws NamingException {
        expectGetReadOnlyContext();

        PartialResultException pre = new PartialResultException();
        dirContextControl.expectAndThrow(dirContextMock.list(NAME), pre);

        dirContextMock.close();

        tested.setIgnorePartialResultException(true);

        replay();

        List list = tested.list(NAME);

        verify();

        assertNotNull(list);
        assertEquals(0, list.size());
    }

    public void testList_NamingException() throws NamingException {
        expectGetReadOnlyContext();

        NamingException ne = new NamingException();
        dirContextControl.expectAndThrow(dirContextMock.list(NAME), ne);

        dirContextMock.close();

        exceptionTranslatorControl.expectAndReturn(exceptionTranslatorMock
                .translate(ne), new EntryNotFoundException("dummy"));

        replay();

        try {
            tested.list(NAME);
            fail("EntryNotFoundException expected");
        } catch (EntryNotFoundException expected) {
            assertTrue(true);
        }

        verify();
    }
}
