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

import java.util.LinkedList;
import java.util.List;

import javax.naming.directory.SearchResult;

/**
 * A SearchResultCallbackHandler to collect all results in an internal List.
 * Useful in combination with e.g. an
 * {@link org.springframework.ldap.AttributesMapper} or
 * {@link org.springframework.ldap.ContextMapper}.
 * 
 * @see org.springframework.ldap.LdapTemplate
 * @see org.springframework.ldap.LdapTemplate.AttributesMapperCallbackHandler
 * @see org.springframework.ldap.LdapTemplate.ContextMapperCallbackHandler
 * @author Mattias Arthursson
 */
public abstract class CollectingSearchResultCallbackHandler implements
        SearchResultCallbackHandler {

    private List list = new LinkedList();

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.SearchResultCallbackHandler#handleSearchResult(javax.naming.directory.SearchResult)
     */
    public void handleSearchResult(SearchResult searchResult) {
        list.add(getObjectFromResult(searchResult));
    }

    /**
     * Handle a single search result and transform it to an Object.
     * 
     * @param searchResult
     *            a SearchResult from a search operation.
     * @return an object constructed from the data in the SearchResult.
     */
    protected abstract Object getObjectFromResult(SearchResult searchResult);

    /**
     * Get the assembled list.
     * 
     * @return the list of all assembled objects.
     */
    public List getList() {
        return list;
    }
}
