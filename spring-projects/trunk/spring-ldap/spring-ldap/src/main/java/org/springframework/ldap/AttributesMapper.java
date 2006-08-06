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
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * Responsible for mapping from LDAP Attributes to beans.
 * 
 * @see org.springframework.ldap.LdapTemplate#search(Name, String, AttributesMapper)
 * @author Mattias Arthursson
 */
public interface AttributesMapper {
    /**
     * Map Attributes to an object. The supplied attributes are the attributes
     * from a single SearchResult.
     * 
     * @param attributes
     *            attributes from a SearchResult.
     * @return an object built from the attributes.
     * @throws NamingException if any error occurs mapping the attributes
     */
    public Object mapFromAttributes(Attributes attributes)
            throws NamingException;
}
