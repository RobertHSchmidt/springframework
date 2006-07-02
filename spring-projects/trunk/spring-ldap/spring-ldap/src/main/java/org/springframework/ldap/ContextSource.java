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

import javax.naming.directory.DirContext;

import org.springframework.dao.DataAccessException;

/**
 * Interface used to retrieve and authenticate LDAP contexts.
 * 
 * @see org.springframework.ldap.LdapTemplate
 * 
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public interface ContextSource {

    /**
     * Gets a read only LDAP context. You are only gaurrantied that you can read
     * from the context, but it is not enforced.
     * 
     * @return An DirContext, never null.
     * @throws DataAccessException
     *             if some error occurs creating an DirContext.
     */
    public DirContext getReadOnlyContext() throws DataAccessException;

    /**
     * Gets a read-write LDAP context.
     * 
     * @return an DirContext, never null.
     * @throws DataAccessException
     *             if some error occurs creating an DirContext.
     */
    public DirContext getReadWriteContext() throws DataAccessException;
}