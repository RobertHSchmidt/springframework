/*
 * Copyright 2005-2006 the original author or authors.
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
package org.springframework.ldap.samples.person.dao;

import org.springframework.ldap.ContextMapper;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.support.DirContextOperations;
import org.springframework.ldap.support.DistinguishedName;

/**
 * Maps from DirContextOperations (DirContextAdapters, really) to Person
 * objects. A DN for a person will be of the form
 * <code>cn=[fullname],ou=[company],c=[country]</code>, so the values of
 * these attributes must be extracted from the DN. For this, we use the
 * DistinguishedName.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PersonContextMapper implements ContextMapper {

    public Object mapFromContext(Object ctx) {
        DirContextOperations dirContext = (DirContextOperations) ctx;
        DistinguishedName dn = new DistinguishedName(dirContext.getDn());
        Person person = new Person();
        DistinguishedName dnWithBase = new DistinguishedName(dn);
        dnWithBase.prepend(new DistinguishedName("dc=jayway, dc=se"));
        person.setDn(dnWithBase.encode());
        person.setCountry(dn.getLdapRdn(0).getComponent().getValue());
        person.setCompany(dn.getLdapRdn(1).getComponent().getValue());
        person.setFullName(dirContext.getStringAttribute("cn"));
        person.setLastName(dirContext.getStringAttribute("sn"));
        person.setDescription(dirContext.getStringAttributes("description"));
        person.setPhone(dirContext.getStringAttribute("telephoneNumber"));

        return person;
    }
}
