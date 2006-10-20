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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.support.control.PagedResultsCookie;
import org.springframework.ldap.support.control.PagedResultsRequestControl;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the paged search result capability of LdapTemplate.
 * <p>
 * Note: Currently, ApacheDS does not support paged results controls, so this
 * test must be run under another directory server, for example OpenLdap. This
 * test will not run under ApacheDS, and the other integration tests assume
 * ApacheDS and will probably not run under OpenLdap.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplatePagedSearchITest extends
        AbstractDependencyInjectionSpringContextTests {

    private LdapTemplate tested;

    private static final String BASE_STRING = "dc=jayway,dc=se";

    private static final String FILTER_STRING = "(&(objectclass=person))";

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext-openldap.xml" };
    }

    protected void onSetUp() throws Exception {
        super.onSetUp();
    }

    protected void onTearDown() throws Exception {
        super.onTearDown();
    }

    public void testSearch_PagedResult() {
        SearchExecutor searchExecutor = new SearchExecutor() {
            public NamingEnumeration executeSearch(DirContext ctx)
                    throws NamingException {
                SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                return ctx.search(BASE_STRING, FILTER_STRING, searchControls);
            }
        };
        PersonAttributesMapper mapper = new PersonAttributesMapper();
        CollectingNameClassPairCallbackHandler callbackHandler = tested.new AttributesMapperCallbackHandler(
                mapper);

        Person person;
        List list;
        PagedResultsCookie cookie;
        PagedResultsRequestControl requestControl;

        // Prepare for first search
        requestControl = new PagedResultsRequestControl(3);
        tested.search(searchExecutor, callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        assertNotNull("Cookie should not be null yet", cookie.getCookie());
        list = callbackHandler.getList();
        assertEquals(3, list.size());
        person = (Person) list.get(0);
        assertEquals("Sweden, Company1, Some Person", person.getDescription());
        person = (Person) list.get(1);
        assertEquals("Sweden, Company1, Some Person2", person.getDescription());
        person = (Person) list.get(2);
        assertEquals("Sweden, Company1, Some Person3", person.getDescription());

        // Prepare for second and last search
        requestControl = new PagedResultsRequestControl(3, cookie);
        tested.search(searchExecutor, callbackHandler, requestControl);
        cookie = requestControl.getCookie();
        assertNull("Cookie should be null now", cookie.getCookie());
        assertEquals(5, list.size());
        person = (Person) list.get(3);
        assertEquals("Sweden, Company2, Some Person", person.getDescription());
        person = (Person) list.get(4);
        assertEquals("Norway, Company1, Multivalued Rdn+Rdn", person.getDescription());
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
