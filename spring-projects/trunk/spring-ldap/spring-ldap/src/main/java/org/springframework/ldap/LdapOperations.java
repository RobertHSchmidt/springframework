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

import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Interface that specifies a basic set of LDAP operations. Implemented by
 * LdapTemplate. Useful option to enhance testability.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public interface LdapOperations {
    /**
     * Perform a search. Use this method only if especially needed - for the
     * most cases there is an overloaded convenience method which calls this one
     * with suitable argments. This method handles all the plumbing; getting a
     * readonly context; looping through the NamingEnumeration and closing the
     * context and enumeration. The actual search is delegated to the
     * SearchExecutor and each SearchResult is passed to the CallbackHandler.
     * Any encountered NamingException will be translated using the
     * NamingExceptionTranslator.
     * 
     * @param se
     *            the SearchExecutor to use for performing the actual search.
     * @param handler
     *            the NameClassPairCallbackHandler to which each found entry
     *            will be passed.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(SearchExecutor se, NameClassPairCallbackHandler handler)
            throws DataAccessException;

    /**
     * Perform an operation (or series of operations) on a read-only context.
     * This method handles the plumbing - getting a DirContext, translating any
     * exceptions and closing the context afterwards. This method is not
     * intended for searches; use
     * {@link #search(SearchExecutor, NameClassPairCallbackHandler)} or any of
     * the overloaded search methods for this.
     * 
     * @param ce
     *            the ContextExecutor to which the actual operation on the
     *            DirContext will be delegated.
     * @return the result from the ContextExecutor's operation.
     * @throws DataAccessException
     *             if the operation resulted in a NamingException.
     */
    public Object executeReadOnly(ContextExecutor ce)
            throws DataAccessException;

    /**
     * Perform an operation (or series of operations) on a read-write context.
     * This method handles the plumbing - getting a DirContext, translating any
     * exceptions and closing the context afterwards.
     * 
     * @param ce
     *            the ContextExecutor to which the actual operation on the
     *            DirContext will be delegated.
     * @return the result from the ContextExecutor's operation.
     * @throws DataAccessException
     *             if the operation resulted in a NamingException.
     */
    public Object executeReadWrite(ContextExecutor ce)
            throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. Use the specified
     * SearchControls in the search. Note that if you are using a ContextMapper,
     * the returningObjFlag needs to be set to true.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param controls
     *            the SearchControls to use in the search.
     * @param handler
     *            the NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     */
    public void search(Name base, String filter, SearchControls controls,
            NameClassPairCallbackHandler handler);

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. Use the specified
     * SearchControls in the search. Note that if you are using a ContextMapper,
     * the returningObjFlag needs to be set to true.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param controls
     *            the SearchControls to use in the search.
     * @param handler
     *            the NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     */
    public void search(String base, String filter, SearchControls controls,
            NameClassPairCallbackHandler handler);

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. Use the specified
     * search scope and return objects flag in search controls.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param returningObjFlag
     *            whether the bound object should be returned in search results.
     * @param handler
     *            the NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(Name base, String filter, int searchScope,
            boolean returningObjFlag, NameClassPairCallbackHandler handler)
            throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. Use the specified
     * search scope and return objects flag in search controls.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param returningObjFlag
     *            whether the bound object should be returned in search results.
     * @param handler
     *            the NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(String base, String filter, int searchScope,
            boolean returningObjFlag, NameClassPairCallbackHandler handler)
            throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. The default
     * Search scope (SearchControls.SUBTREE_SCOPE) will be used and the
     * returnObjects flag will be set to false.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param handler
     *            the NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(Name base, String filter,
            NameClassPairCallbackHandler handler) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. Each SearchResult is
     * supplied to the specified NameClassPairCallbackHandler. The default
     * Search scope (SearchControls.SUBTREE_SCOPE) will be used and no the
     * returnObjects will be set to false.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param handler
     *            the NameClassPairCallbackHandler to supply the SearchResults
     *            to.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public void search(String base, String filter,
            NameClassPairCallbackHandler handler) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. Only search for the
     * specified attributes. The Attributes in each SearchResult is supplied to
     * the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param attrs
     *            the attributes to return, null means returning all attributes.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            String[] attrs, AttributesMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. Only search for the
     * specified attributes. The Attributes in each SearchResult is supplied to
     * the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param attrs
     *            the attributes to return, null means returning all attributes.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            String[] attrs, AttributesMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            AttributesMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            AttributesMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper. The
     * default seach scope will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, AttributesMapper mapper)
            throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Attributes in
     * each SearchResult is supplied to the specified AttributesMapper. The
     * default seach scope will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the AttributesMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, AttributesMapper mapper)
            throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. Only
     * look for the supplied attributes.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param attrs
     *            the attributes to return, null means all attributes.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            String[] attrs, ContextMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. Only
     * look for the supplied attributes.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param attrs
     *            the attributes to return, null means all attributes.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            String[] attrs, ContextMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, int searchScope,
            ContextMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. The
     * default search scope (SearchControls.SUBTREE_SCOPE) will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param searchScope
     *            the search scope to set in SearchControls.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, int searchScope,
            ContextMapper mapper) throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. The
     * default search scope (SearchControls.SUBTREE_SCOPE) will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, ContextMapper mapper)
            throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper. The
     * default search scope (SearchControls.SUBTREE_SCOPE) will be used.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, ContextMapper mapper)
            throws DataAccessException;

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param controls
     *            the SearchControls to use in the search. If the returnObjFlag
     *            is not set in the SearchControls, this method will set it
     *            automatically, as this is required for the ContextMapper to
     *            work.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, SearchControls controls,
            ContextMapper mapper);

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified ContextMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param controls
     *            the SearchControls to use in the search. If the returnObjFlag
     *            is not set in the SearchControls, this method will set it
     *            automatically, as this is required for the ContextMapper to
     *            work.
     * @param mapper
     *            the ContextMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, SearchControls controls,
            ContextMapper mapper);

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param controls
     *            the SearchControls to use in the search.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(String base, String filter, SearchControls controls,
            AttributesMapper mapper);

    /**
     * Search for all objects matching the supplied filter. The Object returned
     * in each SearchResult is supplied to the specified AttributesMapper.
     * 
     * @param base
     *            The base DN where the search should begin.
     * @param filter
     *            the filter to use in the search.
     * @param controls
     *            the SearchControls to use in the search. If the returnObjFlag
     *            is not set in the SearchControls, this method will set it
     *            automatically, as this is required for the ContextMapper to
     *            work.
     * @param mapper
     *            the AttributesMapper to use for translating each entry.
     * @return a List containing all entries received from the ContextMapper.
     * @throws DataAccessException
     *             if any error occurs. Note that a NameNotFoundException will
     *             be ignored. Instead this is interpreted that no entries were
     *             found.
     */
    public List search(Name base, String filter, SearchControls controls,
            AttributesMapper mapper);

    /**
     * Perform a non-recursive listing of the contexts bound to the given
     * <code>base</code>. Each resulting NameClassPair is supplied to the
     * specified ListResultCallbackHandler.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param handler
     *            the {@link NameClassPairCallbackHandler} to supply each
     *            {@link NameClassPair} to.
     */
    public void list(Name base, NameClassPairCallbackHandler handler);

    /**
     * Perform a non-recursive listing of the contexts bound to the given
     * <code>base</code>. Each resulting NameClassPair is supplied to the
     * specified ListResultCallbackHandler.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @param handler
     *            the {@link NameClassPairCallbackHandler} to supply each
     *            {@link NameClassPair} to.
     */
    public void list(String base, NameClassPairCallbackHandler handler);

    /**
     * Perform a non-recursive listing of the contexts bound to the given
     * <code>base</code>. Pass all the found NameClassPair objects to the
     * supplied Mapper and return all the returned values as a List.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the Objects returned from the Mapper.
     */
    public List list(String base, NameClassPairMapper mapper);

    /**
     * Perform a non-recursive listing of the contexts bound to the given
     * <code>base</code>. Pass all the found NameClassPair objects to the
     * supplied Mapper and return all the returned values as a List.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the Objects returned from the Mapper.
     */
    public List list(Name base, NameClassPairMapper mapper);

    /**
     * Perform a non-recursive listing of the contexts bound to the given
     * <code>base</code>.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the names of all the contexts bound to the
     *         given <code> base.
     */
    public List list(String base);

    /**
     * Perform a non-recursive listing of the contexts bound to the given
     * <code>base</code>.
     * 
     * @param base
     *            The base DN where the list should be performed.
     * @return a List containing the names of all the contexts bound to the
     *         given <code> base.
     */
    public List list(Name base);

    /**
     * Lookup the supplied DN and return the found object. <b>WARNING</b>: This
     * method should only be used if a DirObjectFactory has been specified on
     * the ContextFactory. If this is not the case, you will get a new instance
     * of the actual DirContext, which is probably not what you want. If,
     * however this <b>is</b> what you want, be careful to close the context
     * after you finished working with it.
     * 
     * @param dn
     *            the distinguished name of the object to find.
     * @return the found object.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public Object lookup(Name dn) throws DataAccessException;

    /**
     * Lookup the supplied DN and return the found object. <b>WARNING</b>: This
     * method should only be used if a DirObjectFactory has been specified on
     * the ContextFactory. If this is not the case, you will get a new instance
     * of the actual DirContext, which is probably not what you want. If,
     * however this <b>is</b> what you want, be careful to close the context
     * after you finished working with it.
     * 
     * @param dn
     *            the distinguished name of the object to find.
     * @return the found object.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public Object lookup(String dn) throws DataAccessException;

    /**
     * Convenience method to get the attributes of a specified DN and
     * automatically pass them to an AttributesMapper.
     * 
     * @param dn
     *            the distinguished name to find.
     * @param mapper
     *            the AttributesMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public Object lookup(Name dn, AttributesMapper mapper)
            throws DataAccessException;

    /**
     * Convenience method to get the attributes of a specified DN and
     * automatically pass them to an AttributesMapper.
     * 
     * @param dn
     *            the distinguished name to find.
     * @param mapper
     *            the AttributesMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public Object lookup(String dn, AttributesMapper mapper)
            throws DataAccessException;

    /**
     * Convenience method to lookup a specified DN and automatically pass the
     * found objectt to a ContextMapper.
     * 
     * @param dn
     *            the distinguished name to find.
     * @param mapper
     *            the ContextMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public Object lookup(Name dn, ContextMapper mapper)
            throws DataAccessException;

    /**
     * Convenience method to lookup a specified DN and automatically pass the
     * found objectt to a ContextMapper.
     * 
     * @param dn
     *            the distinguished name to find.
     * @param mapper
     *            the ContextMapper to use for mapping the found object.
     * @return the object returned from the mapper.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public Object lookup(String dn, ContextMapper mapper)
            throws DataAccessException;

    /**
     * Modify the distinguished name dn with the supplied ModificationItems.
     * 
     * @param dn
     *            The distinguished name of the node to modify.
     * @param mods
     *            the modifications to perform.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void modifyAttributes(Name dn, ModificationItem[] mods)
            throws DataAccessException;

    /**
     * Modify the distinguished name dn with the supplied ModificationItems.
     * 
     * @param dn
     *            The distinguished name of the node to modify.
     * @param mods
     *            the modifications to perform.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void modifyAttributes(String dn, ModificationItem[] mods)
            throws DataAccessException;

    /**
     * Bind the supplied object together with the attributes to the specified
     * dn.
     * 
     * @param dn
     *            the distinguished name to bind the object and attributes to.
     * @param obj
     *            the object to bind, may be null.
     * @param attributes
     *            the attributes to bind.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void bind(Name dn, Object obj, Attributes attributes)
            throws DataAccessException;

    /**
     * Bind the supplied object together with the attributes to the specified
     * dn.
     * 
     * @param dn
     *            the distinguished name to bind the object and attributes to.
     * @param obj
     *            the object to bind, may be null.
     * @param attributes
     *            the attributes to bind.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void bind(String dn, Object obj, Attributes attributes)
            throws DataAccessException;

    /**
     * Unbind the specified distinguished name.
     * 
     * @param dn
     *            the distinguished name to unbind.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void unbind(Name dn) throws DataAccessException;

    /**
     * Unbind the specified distinguished name.
     * 
     * @param dn
     *            the distinguished name to unbind.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void unbind(String dn) throws DataAccessException;

    /**
     * Unbind the specified distinguished name.
     * 
     * @param dn
     *            the distinguished name to unbind.
     * @param recursive
     *            whether to unbind all subcontexts as well.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void unbind(Name dn, boolean recursive) throws DataAccessException;

    /**
     * Unbind the specified distinguished name.
     * 
     * @param dn
     *            the distinguished name to unbind.
     * @param recursive
     *            whether to unbind all subcontexts as well.
     * @throws DataAccessException
     *             if any error occurs.
     */
    public void unbind(String dn, boolean recursive) throws DataAccessException;

    /**
     * Rebind the name to the object along with the specified attributes,
     * overwriting any previous values. This method assumes that the specified
     * context already exists.
     * 
     * @param dn
     *            the distinguished name to rebind.
     * @param obj
     *            the object to bind to the DN.
     * @param attributes
     *            the attributes to bind.
     */
    public void rebind(Name dn, Object obj, Attributes attributes)
            throws DataAccessException;

    /**
     * Rebind the name to the object along with the specified attributes,
     * overwriting any previous values. This method assumes that the specified
     * context already exists.
     * 
     * @param dn
     *            the distinguished name to rebind.
     * @param obj
     *            the object to bind to the DN.
     * @param attributes
     *            the attributes to bind.
     */
    public void rebind(String dn, Object obj, Attributes attributes)
            throws DataAccessException;

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name. Both names are relative to this context. Any attributes associated
     * with the old name become associated with the new name. Intermediate
     * contexts of the old name are not changed.
     * 
     * @param oldDn
     *            the name of the existing binding; may not be empty
     * @param newDn
     *            the name of the new binding; may not be empty
     * @throws DataIntegrityViolationException
     *             if newDn is already bound
     */
    public void rename(final Name oldDn, final Name newDn)
            throws DataAccessException;

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name. See {@link #rename(Name, Name)} for details.
     * 
     * @param oldDn
     *            the name of the existing binding; may not be empty
     * @param newDn
     *            the name of the new binding; may not be empty
     * @throws DataIntegrityViolationException
     *             if newDn is already bound
     */
    public void rename(final String oldDn, final String newDn)
            throws DataAccessException;
}
