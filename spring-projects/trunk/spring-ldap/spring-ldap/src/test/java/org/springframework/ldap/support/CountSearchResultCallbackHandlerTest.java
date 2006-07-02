package org.springframework.ldap.support;

import javax.naming.directory.SearchResult;

import org.springframework.ldap.support.CountSearchResultCallbackHandler;


import junit.framework.TestCase;

public class CountSearchResultCallbackHandlerTest extends TestCase {

    
    private CountSearchResultCallbackHandler tested;

    protected void setUp() throws Exception {
        super.setUp();
        
        tested = new CountSearchResultCallbackHandler();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        tested = null;
    }

    public void testHandleSearchResult() throws Exception {
        SearchResult dummy = new SearchResult(null, null, null);
        tested.handleSearchResult(dummy);
        tested.handleSearchResult(dummy);
        tested.handleSearchResult(dummy);
        
        assertEquals(3, tested.getNoOfRows());
    }
    
}
