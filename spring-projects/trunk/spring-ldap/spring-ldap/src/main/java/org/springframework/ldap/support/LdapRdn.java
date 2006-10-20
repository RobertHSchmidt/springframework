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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.support.parser.DefaultDnParserFactory;
import org.springframework.ldap.support.parser.DnParser;
import org.springframework.ldap.support.parser.ParseException;
import org.springframework.ldap.support.parser.TokenMgrError;

/**
 * Datatype for a LDAP name, a part of a path.
 * 
 * The name: uid=adam.skogman Key: uid Value: adam.skogman
 * 
 * @author Adam Skogman
 */
public class LdapRdn implements Serializable {
    private static final long serialVersionUID = 5681397547245228750L;

    private List components = new LinkedList();

    public LdapRdn() {
    }

    public LdapRdn(String string) {
        DnParser parser = DefaultDnParserFactory.createDnParser(string);
        LdapRdn rdn;
        try {
            rdn = parser.rdn();
        } catch (ParseException e) {
            throw new BadLdapGrammarException("Failed to parse Rdn", e);
        } catch (TokenMgrError e) {
            throw new BadLdapGrammarException("Failed to parse Rdn", e);
        }
        this.components = rdn.components;
    }

    public LdapRdn(String key, String value) {
        components.add(new LdapRdnComponent(key, value));
    }

    public void addComponent(LdapRdnComponent rdnComponent) {
        components.add(rdnComponent);
    }

    public List getComponents() {
        return components;
    }

    /**
     * Gets the first LdapRdnComponent of this LdapRdn.
     * 
     * @return The first LdapRdnComponent of this LdapRdn.
     * @throws IndexOutOfBoundsException
     *             if no components have been added.
     */
    public LdapRdnComponent getComponent() {
        return (LdapRdnComponent) components.get(0);
    }

    /**
     * Get the LdapRdnComponent at index <code>idx</code>.
     * 
     * @param idx
     *            the 0-based index of the component to get.
     * @return the LdapRdnComponent at indet <code>idx</code>.
     * @throws IndexOutOfBoundsException
     *             if no component exists at index <code>idx</code>.
     */
    public LdapRdnComponent getComponent(int idx) {
        return (LdapRdnComponent) components.get(idx);
    }

    public String getLdapEncoded() {
        if (components.size() == 0) {
            throw new IndexOutOfBoundsException("No components in Rdn.");
        }
        StringBuffer sb = new StringBuffer(100);
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            LdapRdnComponent component = (LdapRdnComponent) iter.next();
            sb.append(component.encodeLdap());
            if (iter.hasNext()) {
                sb.append("+");
            }
        }

        return sb.toString();
    }

    public String encodeUrl() {
        StringBuffer sb = new StringBuffer(100);
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            LdapRdnComponent component = (LdapRdnComponent) iter.next();
            sb.append(component.encodeUrl());
            if(iter.hasNext()){
                sb.append("+");
            }
        }
        
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        
        LdapRdn that = (LdapRdn)obj;
        return this.getComponents().equals(that.getComponents());
    }
    
    public int hashCode() {
        return this.getClass().hashCode() ^ getComponents().hashCode();
    }
}