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

package org.springframework.ws.soap.security.xwss.callback.acegi;

import junit.framework.*;
import org.springframework.ws.soap.security.xwss.callback.acegi.AcegiDigestPasswordValidationCallbackHandler;
import org.easymock.MockControl;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.GrantedAuthority;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

public class AcegiDigestPasswordValidationCallbackHandlerTest extends TestCase {

    private AcegiDigestPasswordValidationCallbackHandler callbackHandler;

    private MockControl control;

    private UserDetailsService mock;

    private String username;

    private String password;

    private PasswordValidationCallback callback;

    protected void setUp() throws Exception {
        callbackHandler = new AcegiDigestPasswordValidationCallbackHandler();
        control = MockControl.createControl(UserDetailsService.class);
        mock = (UserDetailsService) control.getMock();
        callbackHandler.setUserDetailsService(mock);
        username = "Bert";
        password = "Ernie";
        String nonce = "9mdsYDCrjjYRur0rxzYt2oD7";
        String passwordDigest = "kwNstEaiFOrI7B31j7GuETYvdgk=";
        String creationTime = "2006-06-01T23:48:42Z";
        PasswordValidationCallback.DigestPasswordRequest request =
                new PasswordValidationCallback.DigestPasswordRequest(username, passwordDigest, nonce, creationTime);
        callback = new PasswordValidationCallback(request);
    }

    public void testAuthenticateUserDigestUserNotFound() throws Exception {
        control.expectAndThrow(mock.loadUserByUsername(username), new UsernameNotFoundException(username));
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertFalse("Authenticated", authenticated);
        control.verify();
    }

    public void testAuthenticateUserDigestValid() throws Exception {
        User user = new User(username, password, true, true, true, true, new GrantedAuthority[0]);
        control.expectAndReturn(mock.loadUserByUsername(username), user);
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertTrue("Not authenticated", authenticated);
        control.verify();
    }

    public void testAuthenticateUserDigestValidInvalid() throws Exception {
        User user = new User(username, "Big bird", true, true, true, true, new GrantedAuthority[0]);
        control.expectAndReturn(mock.loadUserByUsername(username), user);
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertFalse("Authenticated", authenticated);
        control.verify();
    }
}