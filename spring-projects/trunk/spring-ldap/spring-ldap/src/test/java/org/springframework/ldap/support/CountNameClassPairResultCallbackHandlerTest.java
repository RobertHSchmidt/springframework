package org.springframework.ldap.support;

import javax.naming.directory.SearchResult;

import org.springframework.ldap.support.CountNameClassPairCallbackHandler;


import junit.framework.TestCase;

public class CountNameClassPairResultCallbackHandlerTest extends TestCase {

    
    private CountNameClassPairCallbackHandler tested;

    protected void setUp() throws Exception {
        super.setUp();
        
        tested = new CountNameClassPairCallbackHandler();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        tested = null;
    }

    public void testHandleSearchResult() throws Exception {
        SearchResult dummy = new SearchResult(null, null, null);
        tested.handleNameClassPair(dummy);
        tested.handleNameClassPair(dummy);
        tested.handleNameClassPair(dummy);
        
        assertEquals(3, tested.getNoOfRows());
    }
    
}
