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

public class XwssMessageProcessorSignTest extends XwssMessageProcessorKeyStoreTestCase {


    public void testSignDefaultCertificate() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("sign-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.getDefaultCertificate(Collections.EMPTY_MAP), certificate);
        control.expectAndReturn(mock.getPrivateKey(Collections.EMPTY_MAP, certificate), privateKey);
        control.replay();
        SoapMessage empty = loadSoapMessage("empty-soap.xml");
        SoapMessage result = processor.secureMessage(empty);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature",
                result);
        control.verify();
    }

    public void testSignAlias() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("sign-alias-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.getCertificate(Collections.EMPTY_MAP, "alias", true), certificate);
        control.expectAndReturn(mock.getPrivateKey(Collections.EMPTY_MAP, "alias"), privateKey);
        control.replay();
        SoapMessage empty = loadSoapMessage("empty-soap.xml");
        SoapMessage result = processor.secureMessage(empty);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature",
                result);
        control.verify();
    }

    public void testVerifyCertificate() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("requireSignature-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.validateCertificate(certificate), true);
        mock.updateOtherPartySubject(new Subject(), certificate);
        control.replay();
        SoapMessage empty = loadSoapMessage("signed-soap.xml");
        SoapMessage result = processor.validateMessage(empty);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
        control.verify();
    }

}
