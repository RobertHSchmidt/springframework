package org.springframework.ldap.support.control;

import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;
import com.sun.jndi.ldap.ctl.PagedResultsControl;
import com.sun.jndi.ldap.ctl.PagedResultsResponseControl;

public class PagedResultsRequestControlTest extends TestCase {

    private MockControl ldapContextControl;

    private LdapContext ldapContextMock;

    protected void setUp() throws Exception {
        super.setUp();

        // Create ldapContext mock
        ldapContextControl = MockControl.createControl(LdapContext.class);
        ldapContextMock = (LdapContext) ldapContextControl.getMock();

    }

    protected void tearDown() throws Exception {
        super.tearDown();

        ldapContextControl = null;
        ldapContextMock = null;

    }

    protected void replay() {
        ldapContextControl.replay();
    }

    protected void verify() {
        ldapContextControl.verify();
    }

    public void testCreateRequestControl() throws Exception {
        PagedResultsRequestControl tested = new PagedResultsRequestControl(20);

        PagedResultsControl control = (PagedResultsControl) tested
                .createRequestControl();
        assertNotNull(control);
    }

    public void testCreateRequestControl_CookieSet() throws Exception {
        PagedResultsCookie cookie = new PagedResultsCookie(new byte[0]);
        PagedResultsRequestControl tested = new PagedResultsRequestControl(20,
                cookie);

        PagedResultsControl control = (PagedResultsControl) tested
                .createRequestControl();
        assertNotNull(control);
    }

    public void testPostProcess() throws Exception {
        byte[] dummy = new byte[1];
        dummy[1] = 2;
        BerEncoder ber = new BerEncoder(10 + dummy.length);

        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
        ber.encodeInt(20);
        ber.encodeOctetString(dummy, Ber.ASN_OCTET_STR);
        ber.endSeq();

        byte[] cookie = ber.getTrimmedBuf();
        PagedResultsResponseControl control = new PagedResultsResponseControl(
                "dummy", true, cookie);

        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), new Control[] { control });

        PagedResultsRequestControl tested = new PagedResultsRequestControl(20);

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        PagedResultsCookie returnedCookie = tested.getCookie();
        assertEquals(dummy[0], returnedCookie.getCookie()[0]);
    }

}
