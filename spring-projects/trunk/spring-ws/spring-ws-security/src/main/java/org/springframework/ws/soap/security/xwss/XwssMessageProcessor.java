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

package org.springframework.ws.soap.security.xwss;

import java.io.InputStream;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.config.SecurityConfigurationXmlReader;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.WsSecurityMessageProcessor;
import org.springframework.ws.soap.security.xwss.callback.CallbackHandlerChain;

/**
 * Implementation of the <code>WsSecurityMessageProcessor</code> that is based on Sun's XML and Web Services Security
 * package (XWSS). This WS-Security implementation is part of the Java Web Services Developer Pack (Java WSDP).
 * <p/>
 * This processor needs either a <code>SecurityEnvironment</code> or (preferably) a <code>CallbackHandler</code> to
 * operate. This dependency is used to retrieve certificates, private keys, and user credentials. Refer to the XWSS
 * Javadoc to learn more about the specific <code>Callback</code>s fired by XWSS. You can also set multiple handlers,
 * each of which will be used in turn.
 * <p/>
 * Additionally, you must set a XWSS policy. The easiest way to do so is by specifying the
 * <code>policyConfiguration</code> property. Alternatively, you can override both <code>getSecuringPolicy()</code> and
 * <code>getValidatingPolicy</code>. Overriding these can be useful for creating dynamic security policies, which change
 * from one message to another.
 * <p/>
 * <b>Note</b> that this processor depends on SAAJ, and thus requires <code>SaajSoapMessage</code>s to operate. This
 * means that you must use a <code>SaajSoapMessageContextFactory</code> to create the SOAP messages.
 *
 * @author Arjen Poutsma
 * @see #setCallbackHandler(javax.security.auth.callback.CallbackHandler)
 * @see #setPolicyConfiguration(org.springframework.core.io.Resource)
 * @see #getSecuringPolicy
 * @see #getValidatingPolicy
 * @see com.sun.xml.wss.impl.callback.XWSSCallback
 * @see org.springframework.ws.soap.saaj.SaajSoapMessageContextFactory
 * @see <a href="https://xwss.dev.java.net/">XWSS</a>
 */
public class XwssMessageProcessor implements WsSecurityMessageProcessor, InitializingBean {

    private static final Log logger = LogFactory.getLog(XwssMessageProcessor.class);

    private SecurityEnvironment securityEnvironment;

    private CallbackHandler callbackHandler;

    private DeclarativeSecurityConfiguration declarativeSecurityConfiguration;

    private Resource policyConfiguration;

    private static final QName WS_SECURITY_NAME =
            new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");

    /**
     * Sets the handler to resolve XWSS callbacks.
     *
     * @see com.sun.xml.wss.impl.callback.XWSSCallback
     */
    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    /**
     * Sets the handlers to resolve XWSS callbacks.
     *
     * @see com.sun.xml.wss.impl.callback.XWSSCallback
     */
    public void setCallbackHandlers(CallbackHandler[] callbackHandler) {
        this.callbackHandler = new CallbackHandlerChain(callbackHandler);
    }

    /**
     * Sets the policy configuration to use for XWSS. If this property is not set, you must override
     * <code>getValidatingPolicy()</code> and <code>getSecuringPolicy</code>.
     *
     * @see #getValidatingPolicy(javax.xml.soap.SOAPMessage)
     * @see #getSecuringPolicy(javax.xml.soap.SOAPMessage)
     */
    public void setPolicyConfiguration(Resource policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    /**
     * Sets the security environment to use for XWSS. If this property is set, the <code>callbackHandler</code> will be
     * ignored.
     */
    public void setSecurityEnvironment(SecurityEnvironment securityEnvironment) {
        this.securityEnvironment = securityEnvironment;
    }

    public void afterPropertiesSet() throws Exception {
        if ((securityEnvironment != null && callbackHandler != null) ||
                (securityEnvironment == null && callbackHandler == null)) {
            throw new IllegalArgumentException("Set either the 'securityEnvironment' or 'callbackHandler' property.");
        }
        if (securityEnvironment == null && callbackHandler != null) {
            this.securityEnvironment = new DefaultSecurityEnvironmentImpl(callbackHandler);
        }
        if (policyConfiguration != null) {
            InputStream is = null;
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Loading policy configuration from from '" + policyConfiguration.getFilename() + "'");
                }
                is = policyConfiguration.getInputStream();
                declarativeSecurityConfiguration = SecurityConfigurationXmlReader.createDeclarativeConfiguration(is);
            }
            finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        else if (logger.isWarnEnabled()) {
            logger.warn("No policy configuration specified. Have you overridden getSecuringPolicy() and " +
                    "getValidatingPolicy() ?");
        }
    }

    public SoapMessage secureMessage(SoapMessage message) throws XwssSecuritySecurementException {
        try {
            SOAPMessage saajMessage = convertToSaajMessage(message);
            ProcessingContext context = new ProcessingContext();
            context.setSOAPMessage(saajMessage);
            SecurityPolicy securityPolicy = getSecuringPolicy(saajMessage);
            context.setSecurityPolicy(securityPolicy);
            context.setSecurityEnvironment(securityEnvironment);
            SecurityAnnotator.secureMessage(context);
            return new SaajSoapMessage(context.getSOAPMessage());
        }
        catch (XWSSecurityException ex) {
            throw new XwssSecuritySecurementException("Could not secure message: " + ex.getMessage(), ex);
        }
    }

    public SoapMessage validateMessage(SoapMessage message) throws XwssSecurityValidationException {
        try {
            SOAPMessage saajMessage = convertToSaajMessage(message);
            ProcessingContext context = new ProcessingContext();
            context.setSOAPMessage(saajMessage);
            SecurityPolicy securityPolicy = getValidatingPolicy(saajMessage);
            context.setSecurityPolicy(securityPolicy);
            context.setSecurityEnvironment(securityEnvironment);
            SecurityRecipient.validateMessage(context);
            return new SaajSoapMessage(context.getSOAPMessage());
        }
        catch (XWSSecurityException ex) {
            throw new XwssSecurityValidationException("Could not secure message: " + ex.getMessage(), ex);
        }
    }

    public final boolean understands(SoapHeaderElement headerElement) {
        return WS_SECURITY_NAME.equals(headerElement.getName());
    }

    private SOAPMessage convertToSaajMessage(SoapMessage soapMessage) {
        if (soapMessage instanceof SaajSoapMessage) {
            SaajSoapMessage saajSoapMessage = (SaajSoapMessage) soapMessage;
            return saajSoapMessage.getSaajMessage();
        }
        else {
            throw new IllegalArgumentException("XwssSecurityMessageProcessor requires a SaajSoapMessage. Use a " +
                    "SaajSoapMessageContextFactory to create the SOAP messages.");
        }
    }

    protected SecurityPolicy getSecuringPolicy(SOAPMessage saajMessage) throws IllegalStateException {
        if (declarativeSecurityConfiguration != null) {
            return declarativeSecurityConfiguration.senderSettings();
        }
        else {
            throw new IllegalStateException("Could not determine securing SecurityPolicy. Either set the " +
                    "'policyConfiguration' property or override getSecuringPolicy().");
        }
    }

    protected SecurityPolicy getValidatingPolicy(SOAPMessage message) throws IllegalStateException {
        if (declarativeSecurityConfiguration != null) {
            return declarativeSecurityConfiguration.receiverSettings();
        }
        else {
            throw new IllegalStateException("Could not determine validating SecurityPolicy. Either set the " +
                    "'policyConfiguration' property or override getValidatingPolicy().");
        }
    }
}
