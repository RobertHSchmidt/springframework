/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import javax.crypto.SecretKey;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.DecryptionKeyCallback;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureVerificationKeyCallback;
import org.apache.xml.security.utils.RFC2253Parser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.security.support.KeyStoreFactoryBean;

/**
 * Callback handler that uses Java Security <code>KeyStore</code>s to handle cryptographic callbacks. Allows for
 * specific key stores to be set for various cryptographic operations.
 * <p/>
 * This handler requires one or more key stores to be set. You can configure them in your application context by using a
 * <code>KeyStoreFactoryBean</code>. The exact stores to be set depends on the cryptographic operations that are to be
 * performed by this handler. The table underneath show the key store to be used for each operation: <table border="1">
 * <tr> <td><strong>Cryptographic operation</strong></td> <td><strong>Key store used</strong></td> </tr> <tr>
 * <td>Certificate validation</td> <td>first <code>keyStore</code>, then <code>trustStore</code></td> </tr> <tr>
 * <td>Decryption based on private key</td> <td><code>keyStore</code></td> </tr> <tr> <td>Decryption based on symmetric
 * key</td> <td><code>symmetricStore</code></td> </tr> <tr> <td>Encryption based on certificate</td>
 * <td><code>trustStore</code></td> </tr> <tr> <td>Encryption based on symmetric key</td>
 * <td><code>symmetricStore</code></td> </tr> <tr> <td>Signing</td> <td><code>keyStore</code></td> </tr> <tr>
 * <td>Signature verification</td> <td><code>trustStore</code></td> </tr> </table>
 * <p/>
 * <h3>Default key stores</h3> If the <code>symmetricStore</code> is not set, it will default to the
 * <code>keyStore</code>. If the key or trust store is not set, this handler will use the standard Java mechanism to
 * load or create it. See {@link #loadDefaultKeyStore()} and {@link #loadDefaultTrustStore()}.
 * <p/>
 * <h3>Examples</h3> For instance, if you want to use the <code>KeyStoreCallbackHandler</code> to validate incoming
 * certificates or signatures, you would use a trust store, like so:
 * <pre>
 * &lt;bean id="keyStoreHandler" class="org.springframework.ws.soap.security.xwss.callback.KeyStoreCallbackHandler"&gt;
 *     &lt;property name="trustStore" ref="trustStore"/&gt;
 * &lt;/bean&gt;
 * <p/>
 * &lt;bean id="trustStore" class="org.springframework.ws.soap.security.support.KeyStoreFactoryBean"&gt;
 *     &lt;property name="location" value="classpath:truststore.jks"/&gt;
 *     &lt;property name="password" value="changeit"/&gt;
 * &lt;/bean&gt;
 * </pre>
 * If you want to use it to decrypt incoming certificates or sign outgoing messages, you would use a key store, like
 * so:
 * <pre>
 * &lt;bean id="keyStoreHandler" class="org.springframework.ws.soap.security.xwss.callback.KeyStoreCallbackHandler"&gt;
 *     &lt;property name="keyStore" ref="keyStore"/&gt;
 *     &lt;property name="privateKeyPassword" value="changeit"/&gt;
 * &lt;/bean&gt;
 * <p/>
 * &lt;bean id="keyStore" class="org.springframework.ws.soap.security.support.KeyStoreFactoryBean"&gt;
 *     &lt;property name="location" value="classpath:keystore.jks"/&gt;
 *     &lt;property name="password" value="changeit"/&gt;
 * &lt;/bean&gt;
 * </pre>
 * <p/>
 * <h3>Handled callbacks</h3> This class handles <code>CertificateValidationCallback</code>s,
 * <code>DecryptionKeyCallback</code>s, <code>EncryptionKeyCallback</code>s, <code>SignatureKeyCallback</code>s, and
 * <code>SignatureVerificationKeyCallback</code>s. It throws an <code>UnsupportedCallbackException</code> for others.
 *
 * @author Arjen Poutsma
 * @see KeyStore
 * @see org.springframework.ws.soap.security.support.KeyStoreFactoryBean
 * @see CertificateValidationCallback
 * @see DecryptionKeyCallback
 * @see EncryptionKeyCallback
 * @see SignatureKeyCallback
 * @see SignatureVerificationKeyCallback
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager">The
 *      standard Java trust store mechanism</a>
 */
public class KeyStoreCallbackHandler extends CryptographyCallbackHandler implements InitializingBean {

    private static final String X_509_CERTIFICATE_TYPE = "X.509";

    private static final String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";

    private KeyStore keyStore;

    private KeyStore symmetricStore;

    private KeyStore trustStore;

    private String defaultAlias;

    private char[] privateKeyPassword;

    private char[] symmetricKeyPassword;

    private static X509Certificate getCertificate(String alias, KeyStore store) throws IOException {
        try {
            return (X509Certificate) store.getCertificate(alias);
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static X509Certificate getCertificate(PublicKey pk, KeyStore store) throws IOException {
        try {
            Enumeration aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = store.getCertificate(alias);
                if (cert == null || !X_509_CERTIFICATE_TYPE.equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                if (x509Cert.getPublicKey().equals(pk)) {
                    return x509Cert;
                }
            }
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    /** Sets the key store alias for the default certificate and private key. */
    public void setDefaultAlias(String defaultAlias) {
        this.defaultAlias = defaultAlias;
    }

    /**
     * Sets the default key store. This property is required for decription based on private keys, and signing. If this
     * property is not set, a default key store is loaded.
     *
     * @see org.springframework.ws.soap.security.support.KeyStoreFactoryBean
     * @see #loadDefaultTrustStore()
     */
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Sets the password used to retrieve private keys from the keystore. This property is required for decription based
     * on private keys, and signing.
     */
    public void setPrivateKeyPassword(String privateKeyPassword) {
        if (privateKeyPassword != null) {
            this.privateKeyPassword = privateKeyPassword.toCharArray();
        }
    }

    /**
     * Sets the password used to retrieve keys from the symmetric keystore. If this property is not set, it default to
     * the private key password.
     *
     * @see #setPrivateKeyPassword(String)
     */
    public void setSymmetricKeyPassword(String symmetricKeyPassword) {
        if (symmetricKeyPassword != null) {
            this.symmetricKeyPassword = symmetricKeyPassword.toCharArray();
        }
    }

    /**
     * Sets the key store used for encryption and decryption using symmetric keys. If this property is not set, it
     * defaults to the <code>keyStore</code> property.
     *
     * @see org.springframework.ws.soap.security.support.KeyStoreFactoryBean
     * @see #setKeyStore(java.security.KeyStore)
     */
    public void setSymmetricStore(KeyStore symmetricStore) {
        this.symmetricStore = symmetricStore;
    }

    /**
     * Sets the key store used for signature verifications and encryptions. If this property is not set, a default key
     * store will be loaded.
     *
     * @see org.springframework.ws.soap.security.support.KeyStoreFactoryBean
     * @see #loadDefaultTrustStore()
     */
    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    public void afterPropertiesSet() throws Exception {
        if (keyStore == null) {
            loadDefaultKeyStore();
        }
        if (trustStore == null) {
            loadDefaultTrustStore();
        }
        if (symmetricStore == null) {
            symmetricStore = keyStore;
        }
        if (symmetricKeyPassword == null) {
            symmetricKeyPassword = privateKeyPassword;
        }
    }

    protected final void handleAliasPrivKeyCertRequest(SignatureKeyCallback callback,
                                                       SignatureKeyCallback.AliasPrivKeyCertRequest request)
            throws IOException {
        PrivateKey privateKey = getPrivateKey(request.getAlias());
        X509Certificate certificate = getCertificate(request.getAlias());
        request.setPrivateKey(privateKey);
        request.setX509Certificate(certificate);
    }

    protected final void handleAliasSymmetricKeyRequest(DecryptionKeyCallback callback,
                                                        DecryptionKeyCallback.AliasSymmetricKeyRequest request)
            throws IOException {
        SecretKey secretKey = getSymmetricKey(request.getAlias());
        request.setSymmetricKey(secretKey);
    }

    //
    // Encryption
    //

    protected final void handleAliasSymmetricKeyRequest(EncryptionKeyCallback callback,
                                                        EncryptionKeyCallback.AliasSymmetricKeyRequest request)
            throws IOException {
        SecretKey secretKey = getSymmetricKey(request.getAlias());
        request.setSymmetricKey(secretKey);
    }

    protected final void handleAliasX509CertificateRequest(EncryptionKeyCallback callback,
                                                           EncryptionKeyCallback.AliasX509CertificateRequest request)
            throws IOException {
        X509Certificate certificate = getCertificateFromTrustStore(request.getAlias());
        request.setX509Certificate(certificate);
    }

    //
    // Certificate validation
    //

    protected final void handleCertificateValidationCallback(CertificateValidationCallback callback) {
        callback.setValidator(new KeyStoreCertificateValidator());
    }

    //
    // Signing
    //

    protected final void handleDefaultPrivKeyCertRequest(SignatureKeyCallback callback,
                                                         SignatureKeyCallback.DefaultPrivKeyCertRequest request)
            throws IOException {
        PrivateKey privateKey = getPrivateKey(defaultAlias);
        X509Certificate certificate = getCertificate(defaultAlias);
        request.setPrivateKey(privateKey);
        request.setX509Certificate(certificate);
    }

    protected final void handleDefaultX509CertificateRequest(EncryptionKeyCallback callback,
                                                             EncryptionKeyCallback.DefaultX509CertificateRequest request)
            throws IOException {
        X509Certificate certificate = getCertificateFromTrustStore(defaultAlias);
        request.setX509Certificate(certificate);
    }

    protected final void handlePublicKeyBasedPrivKeyCertRequest(SignatureKeyCallback callback,
                                                                SignatureKeyCallback.PublicKeyBasedPrivKeyCertRequest request)
            throws IOException {
        PrivateKey privateKey = getPrivateKey(request.getPublicKey());
        X509Certificate certificate = getCertificate(request.getPublicKey());
        request.setPrivateKey(privateKey);
        request.setX509Certificate(certificate);
    }

    //
    // Decryption
    //
    protected final void handlePublicKeyBasedPrivKeyRequest(DecryptionKeyCallback callback,
                                                            DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest request)
            throws IOException {
        PrivateKey key = getPrivateKey(request.getPublicKey());
        request.setPrivateKey(key);
    }

    protected final void handlePublicKeyBasedRequest(EncryptionKeyCallback callback,
                                                     EncryptionKeyCallback.PublicKeyBasedRequest request)
            throws IOException {
        X509Certificate certificate = getCertificateFromTrustStore(request.getPublicKey());
        request.setX509Certificate(certificate);
    }

    protected final void handlePublicKeyBasedRequest(SignatureVerificationKeyCallback callback,
                                                     SignatureVerificationKeyCallback.PublicKeyBasedRequest request)
            throws IOException {
        X509Certificate certificate = getCertificateFromTrustStore(request.getPublicKey());
        request.setX509Certificate(certificate);
    }

    protected final void handleX509CertificateBasedRequest(DecryptionKeyCallback callback,
                                                           DecryptionKeyCallback.X509CertificateBasedRequest request)
            throws IOException {
        PrivateKey privKey = getPrivateKey(request.getX509Certificate());
        request.setPrivateKey(privKey);
    }

    protected final void handleX509IssuerSerialBasedRequest(DecryptionKeyCallback callback,
                                                            DecryptionKeyCallback.X509IssuerSerialBasedRequest request)
            throws IOException {
        PrivateKey key = getPrivateKey(request.getIssuerName(), request.getSerialNumber());
        request.setPrivateKey(key);
    }

    protected final void handleX509IssuerSerialBasedRequest(SignatureVerificationKeyCallback callback,
                                                            SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest request)
            throws IOException {
        X509Certificate certificate = getCertificateFromTrustStore(request.getIssuerName(), request.getSerialNumber());
        request.setX509Certificate(certificate);
    }

    protected final void handleX509SubjectKeyIdentifierBasedRequest(DecryptionKeyCallback callback,
                                                                    DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest request)
            throws IOException {
        PrivateKey key = getPrivateKey(request.getSubjectKeyIdentifier());
        request.setPrivateKey(key);
    }

    //
    // Signature verification
    //

    protected final void handleX509SubjectKeyIdentifierBasedRequest(SignatureVerificationKeyCallback callback,
                                                                    SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest request)
            throws IOException {
        X509Certificate certificate = getCertificateFromTrustStore(request.getSubjectKeyIdentifier());
        request.setX509Certificate(certificate);
    }

    // Certificate methods

    protected X509Certificate getCertificate(String alias) throws IOException {
        return getCertificate(alias, keyStore);
    }

    protected X509Certificate getCertificate(PublicKey pk) throws IOException {
        return getCertificate(pk, keyStore);
    }

    protected X509Certificate getCertificateFromTrustStore(String alias) throws IOException {
        return getCertificate(alias, trustStore);
    }

    protected X509Certificate getCertificateFromTrustStore(byte[] subjectKeyIdentifier) throws IOException {
        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !X_509_CERTIFICATE_TYPE.equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(subjectKeyIdentifier, keyId)) {
                    return x509Cert;
                }
            }
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    protected X509Certificate getCertificateFromTrustStore(PublicKey pk) throws IOException {
        return getCertificate(pk, trustStore);
    }

    protected X509Certificate getCertificateFromTrustStore(String issuerName, BigInteger serialNumber)
            throws IOException {
        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !X_509_CERTIFICATE_TYPE.equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName = RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) && thisSerialNumber.equals(serialNumber)) {
                    return x509Cert;
                }
            }
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    // Private Key methods

    protected PrivateKey getPrivateKey(String alias) throws IOException {
        try {
            return (PrivateKey) keyStore.getKey(alias, privateKeyPassword);
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
    }

    protected PrivateKey getPrivateKey(PublicKey publicKey) throws IOException {
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    // Just returning the first one here
                    return (PrivateKey) keyStore.getKey(alias, privateKeyPassword);
                }
            }
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    protected PrivateKey getPrivateKey(X509Certificate certificate) throws IOException {
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert != null && cert.equals(certificate)) {
                    return (PrivateKey) keyStore.getKey(alias, privateKeyPassword);
                }
            }
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    protected PrivateKey getPrivateKey(byte[] keyIdentifier) throws IOException {
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(keyIdentifier, keyId)) {
                    return (PrivateKey) keyStore.getKey(alias, privateKeyPassword);
                }
            }
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    protected PrivateKey getPrivateKey(String issuerName, BigInteger serialNumber) throws IOException {
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName = RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) && thisSerialNumber.equals(serialNumber)) {
                    return (PrivateKey) keyStore.getKey(alias, privateKeyPassword);
                }
            }
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    // Utility methods

    protected final byte[] getSubjectKeyIdentifier(X509Certificate cert) {
        byte[] subjectKeyIdentifier = cert.getExtensionValue(SUBJECT_KEY_IDENTIFIER_OID);
        if (subjectKeyIdentifier == null) {
            return null;
        }
        byte[] dest = new byte[subjectKeyIdentifier.length - 4];
        System.arraycopy(subjectKeyIdentifier, 4, dest, 0, subjectKeyIdentifier.length - 4);
        return dest;
    }

    //
    // Symmetric key methods
    //

    protected SecretKey getSymmetricKey(String alias) throws IOException {
        try {
            return (SecretKey) symmetricStore.getKey(alias, symmetricKeyPassword);
        }
        catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Loads the key store indicated by system properties. This method tries to load a key store by consulting the
     * following system properties:<code>javax.net.ssl.keyStore</code>, <code>javax.net.ssl.keyStorePassword</code>, and
     * <code>javax.net.ssl.keyStoreType</code>.
     * <p/>
     * If these properties specify a file with an appropriate password, the factory uses this file for the key store. If
     * that file does not exist, then a default, empty keystore is created.
     * <p/>
     * This behavior corresponds to the standard J2SDK behavior for SSL key stores.
     *
     * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html#X509KeyManager">The
     *      standard J2SDK SSL key store mechanism</a>
     */
    protected void loadDefaultKeyStore() {
        Resource location = null;
        String type = null;
        String password = null;
        String locationProperty = System.getProperty("javax.net.ssl.keyStore");
        if (StringUtils.hasLength(locationProperty)) {
            File f = new File(locationProperty);
            if (f.exists() && f.isFile() && f.canRead()) {
                location = new FileSystemResource(f);
            }
            String passwordProperty = System.getProperty("javax.net.ssl.keyStorePassword");
            if (StringUtils.hasLength(passwordProperty)) {
                password = passwordProperty;
            }
            type = System.getProperty("javax.net.ssl.trustStore");
        }
        // use the factory bean here, easier to setup
        KeyStoreFactoryBean factoryBean = new KeyStoreFactoryBean();
        factoryBean.setLocation(location);
        factoryBean.setPassword(password);
        factoryBean.setType(type);
        try {
            factoryBean.afterPropertiesSet();
            this.trustStore = (KeyStore) factoryBean.getObject();
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded default key store");
            }
        }
        catch (Exception ex) {
            logger.warn("Could not open default key store", ex);
        }
    }

    /**
     * Loads a default trust store. This method uses the following algorithm: <ol> <li> If the system property
     * <code>javax.net.ssl.trustStore</code> is defined, its value is loaded. If the
     * <code>javax.net.ssl.trustStorePassword</code> system property is also defined, its value is used as a password.
     * If the <code>javax.net.ssl.trustStoreType</code> system property is defined, its value is used as a key store
     * type.
     * <p/>
     * If <code>javax.net.ssl.trustStore</code> is defined but the specified file does not exist, then a default, empty
     * trust store is created. </li> <li> If the <code>javax.net.ssl.trustStore</code> system property was not
     * specified, but if the file <code>$JAVA_HOME/lib/security/jssecacerts</code> exists, that file is used. </li>
     * Otherwise, <li>If the file <code>$JAVA_HOME/lib/security/cacerts</code> exists, that file is used. </ol>
     * <p/>
     * This behavior corresponds to the standard J2SDK behavior for SSL trust stores.
     *
     * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager">The
     *      standard J2SDK SSL trust store mechanism</a>
     */
    protected void loadDefaultTrustStore() {
        Resource location = null;
        String type = null;
        String password = null;
        String locationProperty = System.getProperty("javax.net.ssl.trustStore");
        if (StringUtils.hasLength(locationProperty)) {
            File f = new File(locationProperty);
            if (f.exists() && f.isFile() && f.canRead()) {
                location = new FileSystemResource(f);
            }
            String passwordProperty = System.getProperty("javax.net.ssl.trustStorePassword");
            if (StringUtils.hasLength(passwordProperty)) {
                password = passwordProperty;
            }
            type = System.getProperty("javax.net.ssl.trustStoreType");
        }
        else {
            String javaHome = System.getProperty("java.home");
            location = new FileSystemResource(javaHome + "/lib/security/jssecacerts");
            if (!location.exists()) {
                location = new FileSystemResource(javaHome + "/lib/security/cacerts");
            }
        }
        // use the factory bean here, easier to setup
        KeyStoreFactoryBean factoryBean = new KeyStoreFactoryBean();
        factoryBean.setLocation(location);
        factoryBean.setPassword(password);
        factoryBean.setType(type);
        try {
            factoryBean.afterPropertiesSet();
            this.trustStore = (KeyStore) factoryBean.getObject();
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded default trust store");
            }
        }
        catch (Exception ex) {
            logger.warn("Could not open default trust store", ex);
        }
    }

    //
    // Inner classes
    //

    private class KeyStoreCertificateValidator implements CertificateValidationCallback.CertificateValidator {

        public boolean validate(X509Certificate certificate)
                throws CertificateValidationCallback.CertificateValidationException {
            if (isOwnedCert(certificate)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Certificate with DN [" + certificate.getSubjectX500Principal().getName() +
                            "] is in private keystore");
                }
                return true;
            }
            else if (trustStore == null) {
                return false;
            }

            try {
                certificate.checkValidity();
            }
            catch (CertificateExpiredException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Certificate with DN [" + certificate.getSubjectX500Principal().getName() +
                            "] has expired");
                }
                return false;
            }
            catch (CertificateNotYetValidException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Certificate with DN [" + certificate.getSubjectX500Principal().getName() +
                            "] is not yet valid");
                }
                return false;
            }

            X509CertSelector certSelector = new X509CertSelector();
            certSelector.setCertificate(certificate);

            PKIXBuilderParameters parameters;
            CertPathBuilder builder;
            try {
                parameters = new PKIXBuilderParameters(trustStore, certSelector);
                parameters.setRevocationEnabled(false);
                builder = CertPathBuilder.getInstance("PKIX");
            }
            catch (GeneralSecurityException ex) {
                throw new CertificateValidationCallback.CertificateValidationException(
                        "Could not create PKIX CertPathBuilder", ex);
            }

            try {
                builder.build(parameters);
            }
            catch (CertPathBuilderException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Certification path of certificate with DN [" +
                            certificate.getSubjectX500Principal().getName() + "] could not be validated");
                }
                return false;
            }
            catch (InvalidAlgorithmParameterException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Algorithm of certificate with DN [" +
                            certificate.getSubjectX500Principal().getName() + "] could not be validated");
                }
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Certificate with DN [" + certificate.getSubjectX500Principal().getName() + "] validated");
            }
            return true;
        }

        private boolean isOwnedCert(X509Certificate cert)
                throws CertificateValidationCallback.CertificateValidationException {
            if (keyStore == null) {
                return false;
            }
            try {
                Enumeration aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    if (keyStore.isKeyEntry(alias)) {
                        X509Certificate x509Cert = (X509Certificate) keyStore.getCertificate(alias);
                        if (x509Cert != null) {
                            if (x509Cert.equals(cert)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            catch (GeneralSecurityException e) {
                throw new CertificateValidationCallback.CertificateValidationException(
                        "Could not determine whether certificate is contained in main key store", e);
            }
        }
    }
}
