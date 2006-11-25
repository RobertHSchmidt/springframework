package org.springframework.ldap.support.control;

import java.util.LinkedList;
import java.util.List;

import com.gargoylesoftware.base.testing.EqualsTester;

import junit.framework.TestCase;

public class PagedResultTest extends TestCase {
    public void testEquals() throws Exception {
        List expectedList = new LinkedList();
        expectedList.add("dummy");
        List otherList = new LinkedList();
        otherList.add("different");

        PagedResult originalObject = new PagedResult(expectedList,
                new PagedResultsCookie(null));
        PagedResult identicalObject = new PagedResult(expectedList,
                new PagedResultsCookie(null));
        PagedResult differentObject = new PagedResult(otherList,
                new PagedResultsCookie(null));
        PagedResult subclassObject = new PagedResult(expectedList,
                new PagedResultsCookie(null)) {

        };

        new EqualsTester(originalObject, identicalObject, differentObject,
                subclassObject);
    }
}
