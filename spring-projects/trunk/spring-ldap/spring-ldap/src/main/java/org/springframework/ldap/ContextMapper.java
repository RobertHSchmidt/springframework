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

import javax.naming.Name;

/**
 * Responsible for mapping from LDAP Contexts to beans.
 * 
 * @see org.springframework.ldap.LdapTemplate#search(Name, String, ContextMapper)
 * 
 * @author Mattias Arthursson
 */
public interface ContextMapper {
    /**
     * Map a single LDAP Context to an object. The supplied Object
     * <code>ctx</code> is the object from a single <code>SearchResult</code>
     * or a lookup operation.
     * 
     * @param ctx
     *            the context to map to an object.
     * @return an object built from the data in the context.
     */
    public Object mapFromContext(Object ctx);
}
