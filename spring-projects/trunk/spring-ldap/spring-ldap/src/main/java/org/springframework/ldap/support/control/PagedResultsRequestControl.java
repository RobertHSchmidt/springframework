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
import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.util.ReflectionUtils;

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

    private static final String JAVA5_RESPONSE_CONTROL = "javax.naming.ldap.PagedResultsResponseControl";

    private static final boolean CRITICAL_CONTROL = true;

    private int pageSize;

    private PagedResultsCookie cookie;

    private int resultSize;

    private boolean forceComSunPagedResultsControl = false;

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

    public int getPageSize() {
        return pageSize;
    }

    public int getResultSize() {
        return resultSize;
    }

    public boolean isForceComSunPagedResultsControl() {
        return forceComSunPagedResultsControl;
    }

    public void setForceComSunPagedResultsControl(
            boolean forceComSunPagedResultsControl) {
        this.forceComSunPagedResultsControl = forceComSunPagedResultsControl;
    }

    /*
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

    /*
     * @see org.springframework.ldap.DirContextProcessor#postProcess(javax.naming.directory.DirContext)
     */
    public void postProcess(DirContext ctx) throws NamingException {
        LdapContext ldapContext = (LdapContext) ctx;
        Control[] responseControls = ldapContext.getResponseControls();

        // Use Java5 version if available, unless forced to skip it
        Class clazz = PagedResultsResponseControl.class;
        if (!forceComSunPagedResultsControl) {
            try {
                clazz = Class.forName(JAVA5_RESPONSE_CONTROL);
            } catch (ClassNotFoundException e) {
                clazz = PagedResultsResponseControl.class;
            }
        }

        // Go through response controls and get info, regardless of class
        for (int i = 0; i < responseControls.length; i++) {
            if (responseControls[i].getClass().isAssignableFrom(clazz)) {
                Object control = responseControls[i];
                byte[] result = (byte[]) invokeMethod("getCookie", clazz,
                        control);
                this.cookie = new PagedResultsCookie(result);
                Integer wrapper = (Integer) invokeMethod("getResultSize",
                        clazz, control);
                this.resultSize = wrapper.intValue();
            }
        }
    }

    private Object invokeMethod(String method, Class clazz, Object control) {
        Method m = ReflectionUtils.findMethod(clazz, method, new Class[0]);
        return ReflectionUtils.invokeMethod(m, control);
    }
}
