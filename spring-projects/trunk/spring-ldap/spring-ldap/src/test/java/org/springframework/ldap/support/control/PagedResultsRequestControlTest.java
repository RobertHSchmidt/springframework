package org.springframework.ldap.support.control;

import java.io.IOException;

import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.easymock.MockControl;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
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
        byte[] value = new byte[1];
        value[0] = 8;
        byte[] cookie = encodeValue(20, value);
        PagedResultsResponseControl control = new PagedResultsResponseControl(
                "dummy", true, cookie);

        ldapContextControl.expectAndDefaultReturn(ldapContextMock
                .getResponseControls(), new Control[] { control });

        PagedResultsRequestControl tested = new PagedResultsRequestControl(20);

        replay();

        tested.postProcess(ldapContextMock);

        verify();

        PagedResultsCookie returnedCookie = tested.getCookie();
        assertEquals(value[0], returnedCookie.getCookie()[0]);
    }

    public void testBerDecoding() throws Exception {
        byte[] value = new byte[1];
        value[0] = 8;
        int pageSize = 20;
        byte[] cookie = encodeValue(pageSize, value);
        
        BerDecoder ber = new BerDecoder(cookie, 0, cookie.length);

        ber.parseSeq(null);
        int actualPageSize = ber.parseInt();
        byte[] actualValue = ber.parseOctetString(Ber.ASN_OCTET_STR, null);

        assertEquals("pageSize,", 20, actualPageSize);
        assertEquals("value length", value.length, actualValue.length);
        for (int i = 0; i < value.length; i++) {
            assertEquals("value (index " + i + "),", value[i], actualValue[i]);
        }
    }

    private byte[] encodeValue(int pageSize, byte[] cookie)
            throws IOException {

        // build the ASN.1 encoding
        BerEncoder ber = new BerEncoder(10 + cookie.length);

        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
        ber.encodeInt(pageSize);
        ber.encodeOctetString(cookie, Ber.ASN_OCTET_STR);
        ber.endSeq();

        return ber.getTrimmedBuf();
    }
}
