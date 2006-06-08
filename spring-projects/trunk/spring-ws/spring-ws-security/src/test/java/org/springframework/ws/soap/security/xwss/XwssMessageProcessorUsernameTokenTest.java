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

import java.util.Collections;

import javax.security.auth.Subject;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.core.io.ClassPathResource;

public class XwssMessageProcessorUsernameTokenTest extends XwssMessageProcessorTestCase {

    public void testAddUsernameTokenDigest() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("usernameToken-digest-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.getUsername(Collections.EMPTY_MAP), "Bert");
        control.expectAndReturn(mock.getPassword(Collections.EMPTY_MAP), "Ernie");
        control.replay();
        SoapMessage empty = loadSoapMessage("empty-soap.xml");
        SoapMessage result = processor.secureMessage(empty);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", result);
        assertXpathExists("Password does not exist",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest']",
                result);
        control.verify();
    }

    public void testAddUsernameTokenPlainText() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("usernameToken-plainText-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.getUsername(Collections.EMPTY_MAP), "Bert");
        control.expectAndReturn(mock.getPassword(Collections.EMPTY_MAP), "Ernie");
        control.replay();
        SoapMessage empty = loadSoapMessage("empty-soap.xml");
        SoapMessage result = processor.secureMessage(empty);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", result);
        assertXpathEvaluatesTo("Invalid Password", "Ernie",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']/text()",
                result);
        control.verify();
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("requireUsernameToken-plainText-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.authenticateUser(Collections.EMPTY_MAP, "Bert", "Ernie"), true);
        mock.updateOtherPartySubject(new Subject(), "Bert", "Ernie");
        control.replay();
        SoapMessage message = loadSoapMessage("userNameTokenPlainText-soap.xml");
        SoapMessage result = processor.validateMessage(message);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
        control.verify();
    }

    public void testValidateUsernameTokenDigest() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("requireUsernameToken-digest-config.xml", getClass()));
        processor.afterPropertiesSet();
        String creationTime = "2006-06-01T23:48:42Z";
        String nonce = "9mdsYDCrjjYRur0rxzYt2oD7";
        String passwordDigest = "kwNstEaiFOrI7B31j7GuETYvdgk=";
        control.expectAndReturn(mock.authenticateUser(Collections.EMPTY_MAP, "Bert", passwordDigest,
                nonce, creationTime), true);
        mock.validateCreationTime(Collections.EMPTY_MAP, creationTime, 60000,300000);
        control.expectAndReturn(mock.validateAndCacheNonce(nonce, creationTime, 900000), true);
        mock.updateOtherPartySubject(new Subject(), "Bert", null);
        control.replay();
        SoapMessage message = loadSoapMessage("userNameTokenDigest-soap.xml");
        SoapMessage result = processor.validateMessage(message);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
        control.verify();
    }

}