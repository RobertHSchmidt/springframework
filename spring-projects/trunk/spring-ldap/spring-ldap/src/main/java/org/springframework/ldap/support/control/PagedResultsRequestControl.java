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

package org.springframework.ldap.support.control;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.springframework.ldap.UncategorizedLdapException;

import com.sun.jndi.ldap.ctl.PagedResultsControl;
import com.sun.jndi.ldap.ctl.PagedResultsResponseControl;

/**
 * DirContextProcessor implementation for managing the paged results.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PagedResultsRequestControl extends
        AbstractRequestControlDirContextProcessor {

    private static final boolean CRITICAL_CONTROL = true;

    private int pageSize;

    private PagedResultsCookie cookie;

    public PagedResultsRequestControl(int pageSize) {
        this(pageSize, null);
    }

    public PagedResultsRequestControl(int pageSize, PagedResultsCookie cookie) {
        this.pageSize = pageSize;
        this.cookie = cookie;
    }

    public PagedResultsCookie getCookie() {
        return cookie;
    }

    public void setCookie(PagedResultsCookie cookie) {
        this.cookie = cookie;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.control.AbstractRequestControlDirContextProcessor#createRequestControl()
     */
    public Control createRequestControl() {
        try {
            if (cookie != null) {
                return new PagedResultsControl(pageSize, cookie.getCookie(),
                        CRITICAL_CONTROL);
            } else {
                return new PagedResultsControl(pageSize);
            }
        } catch (IOException e) {
            throw new UncategorizedLdapException(
                    "Error creating PagedResultsControl", e);
        }
    }

    public void postProcess(DirContext ctx) throws NamingException {
        LdapContext ldapContext = (LdapContext) ctx;
        Control[] responseControls = ldapContext.getResponseControls();

        for (int i = 0; i < responseControls.length; i++) {
            if (responseControls[i] instanceof PagedResultsResponseControl) {
                PagedResultsResponseControl control = (PagedResultsResponseControl) responseControls[i];
                this.cookie = new PagedResultsCookie(control.getCookie());
            }
        }
    }

}
