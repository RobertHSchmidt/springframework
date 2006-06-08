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

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.SoapMessage;

public class XwssMessageProcessorEncryptTest extends XwssMessageProcessorKeyStoreTestCase {

    public void testEncryptDefaultCertificate() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("encrypt-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.getCertificate(Collections.EMPTY_MAP, "", false), certificate);
        control.replay();
        SoapMessage empty = loadSoapMessage("empty-soap.xml");
        SoapMessage result = processor.secureMessage(empty);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/xenc:EncryptedKey",
                result);
        control.verify();
    }

    public void testEncryptAlias() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("encrypt-alias-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.getCertificate(Collections.EMPTY_MAP, "alias", false), certificate);
        control.replay();
        SoapMessage empty = loadSoapMessage("empty-soap.xml");
        SoapMessage result = processor.secureMessage(empty);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/xenc:EncryptedKey",
                result);
        control.verify();
    }

    public void testDecrypt() throws Exception {
        processor.setPolicyConfiguration(new ClassPathResource("decrypt-config.xml", getClass()));
        processor.afterPropertiesSet();
        control.expectAndReturn(mock.getPrivateKey(Collections.EMPTY_MAP, certificate), privateKey);
        control.replay();
        SoapMessage empty = loadSoapMessage("encrypted-soap.xml");
        SoapMessage result = processor.validateMessage(empty);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
        control.verify();
    }

}
