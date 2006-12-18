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
package org.springframework.ldap.samples.person.dao;

import java.util.List;

import org.springframework.ldap.samples.person.domain.Person;

import org.springframework.ldap.ContextMapper;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DirContextOperations;
import org.springframework.ldap.support.DistinguishedName;
import org.springframework.ldap.support.filter.EqualsFilter;

/**
 * Default implementation of PersonDao. This implementation uses
 * DirContextOperations (DirContextAdapter really, but for mock testing purposes
 * we use the interface) for managing attribute values. It has been specified in
 * the Spring Context that the DirObjectFactory should be used when creating
 * objects from contexts, which defaults to creating DirContextAdapter objects.
 * This means that we can use a ContextMapper to map from the found contexts to
 * our domain objects. This is especially useful since we in this case have
 * properties in our domain objects that depend on parts of the DN.
 * 
 * We could have worked with Attributes and an AttributesMapper implementation
 * instead, but working with Attributes is a bore and also, working with
 * AttributesMapper objects (or, indeed Attributes) does not give us access to
 * the distinguished name.
 * 
 * @author Mattias Arthursson
 */
public class PersonDaoImpl implements PersonDao {

    private LdapOperations ldapOperations;

    DistinguishedName buildDn(Person person) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("c", person.getCountry());
        dn.add("ou", person.getCompany());
        dn.add("cn", person.getFullName());
        return dn;
    }

    DirContextOperations getContextToBind(Person person) {
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", person.getFullName());
        adapter.setAttributeValue("sn", person.getLastName());
        adapter.setAttributeValue("description", person.getDescription());
        adapter.setAttributeValue("telephoneNumber", person.getPhone());
        return adapter;
    }

    ContextMapper getContextMapper() {
        return new PersonContextMapper();
    }

    public void create(Person person) {
        ldapOperations.bind(buildDn(person), getContextToBind(person), null);
    }

    public void update(Person person) {
        ldapOperations.rebind(buildDn(person), getContextToBind(person), null);
    }

    public void delete(Person person) {
        ldapOperations.unbind(buildDn(person));
    }

    public List findAll() {
        EqualsFilter filter = new EqualsFilter("objectclass", "person");
        return ldapOperations.search(DistinguishedName.EMPTY_PATH, filter
                .encode(), getContextMapper());
    }

    public Person findByPrimaryKey(String country, String company,
            String fullname) {

        DistinguishedName dn = new DistinguishedName();
        dn.add("c", country);
        dn.add("ou", company);
        dn.add("cn", fullname);

        return (Person) ldapOperations.lookup(dn, getContextMapper());
    }

    public void setLdapOperations(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }
}
