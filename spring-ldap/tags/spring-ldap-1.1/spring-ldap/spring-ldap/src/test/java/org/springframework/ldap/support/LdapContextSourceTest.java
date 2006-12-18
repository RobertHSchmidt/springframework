package org.springframework.ldap.support;

import java.util.Hashtable;

import javax.naming.Context;

import org.springframework.ldap.AuthenticationSource;
import org.springframework.ldap.support.LdapContextSource;


import junit.framework.TestCase;

public class LdapContextSourceTest extends TestCase {

    private LdapContextSource tested;

    protected void setUp() throws Exception {
        tested = new LdapContextSource();
    }

    protected void tearDown() throws Exception {
        tested = null;
    }

    public void testAfterPropertiesSet_NoUrl() throws Exception {
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testAfterPropertiesSet_BaseAndTooEarlyJdk() throws Exception {
        tested = new LdapContextSource() {
            String getJdkVersion() {
                return "1.4.1_03";
            }
        };

        tested.setUrl("http://ldap.example.com:389");
        tested.setBase("dc=jayway,dc=se");
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testGetAnonymousEnv() throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        tested.setUserName("cn=Some User");
        tested.setPassword("secret");
        tested.afterPropertiesSet();
        Hashtable env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertNull(env.get(Context.SECURITY_PRINCIPAL));
        assertNull(env.get(Context.SECURITY_CREDENTIALS));

        // Verify that changing values does not change the environment values.
        tested.setBase("dc=other,dc=se");
        tested.setUrl("ldap://ldap2.example.com:389");
        tested.setPooled(false);

        env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertNull(env.get(Context.SECURITY_PRINCIPAL));
        assertNull(env.get(Context.SECURITY_CREDENTIALS));
    }

    public void testGetAuthenticatedEnv() throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        tested.setUserName("cn=Some User");
        tested.setPassword("secret");
        tested.afterPropertiesSet();

        Hashtable env = tested.getAuthenticatedEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertEquals("cn=Some User", env.get(Context.SECURITY_PRINCIPAL));
        assertEquals("secret", env.get(Context.SECURITY_CREDENTIALS));
    }

    public void testGetAuthenticatedEnv_DummyAuthenticationProvider()
            throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        DummyAuthenticationProvider authenticationProvider = new DummyAuthenticationProvider();
        tested.setAuthenticationSource(authenticationProvider);
        authenticationProvider.setPrincipal("cn=Some User");
        authenticationProvider.setCredentials("secret");
        tested.afterPropertiesSet();

        Hashtable env = tested.getAuthenticatedEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertEquals("cn=Some User", env.get(Context.SECURITY_PRINCIPAL));
        assertEquals("secret", env.get(Context.SECURITY_CREDENTIALS));
    }

    public void testGetAuthenticatedEnv_DummyAuthenticationProvider_Changed() throws Exception{        
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        DummyAuthenticationProvider authenticationProvider = new DummyAuthenticationProvider();
        tested.setAuthenticationSource(authenticationProvider);
        authenticationProvider.setPrincipal("cn=Some User");
        authenticationProvider.setCredentials("secret");
        tested.afterPropertiesSet();
        
        authenticationProvider.setPrincipal("cn=Some Other User");
        authenticationProvider.setCredentials("other secret");
        
        Hashtable env = tested.getAuthenticatedEnv();
        assertEquals("cn=Some Other User", env.get(Context.SECURITY_PRINCIPAL));
        assertEquals("other secret", env.get(Context.SECURITY_CREDENTIALS));
        
    }

    public void testGetAnonymousEnv_DontCacheEnv() throws Exception{
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        tested.setUserName("cn=Some User");
        tested.setPassword("secret");
        tested.setCacheEnvironmentProperties(false);
        tested.afterPropertiesSet();
        Hashtable env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertNull(env.get(Context.SECURITY_PRINCIPAL));
        assertNull(env.get(Context.SECURITY_CREDENTIALS));
        
        tested.setUrl("ldap://ldap2.example.com:389");
        env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap2.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
    }
    
    private class DummyAuthenticationProvider implements AuthenticationSource {
        private String principal;

        private String credentials;

        public void setCredentials(String credentials) {
            this.credentials = credentials;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public String getPrincipal() {
            return principal;
        }

        public String getCredentials() {
            return credentials;
        }

    }

}
