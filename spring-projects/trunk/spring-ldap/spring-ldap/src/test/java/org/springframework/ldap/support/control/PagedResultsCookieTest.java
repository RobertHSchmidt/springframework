package org.springframework.ldap.support.control;

import com.gargoylesoftware.base.testing.EqualsTester;

import junit.framework.TestCase;

public class PagedResultsCookieTest extends TestCase {
    public void testEquals() {
        byte[] expectedCookie = new byte[] { 1, 2 };
        byte[] differentCookie = new byte[] { 2, 3 };

        PagedResultsCookie originalObject = new PagedResultsCookie(
                expectedCookie);
        PagedResultsCookie identicalObject = new PagedResultsCookie(
                expectedCookie);
        PagedResultsCookie differentObject = new PagedResultsCookie(
                differentCookie);
        PagedResultsCookie subclassObject = new PagedResultsCookie(
                expectedCookie) {

        };

        new EqualsTester(originalObject, identicalObject, differentObject,
                subclassObject);
    }
}
