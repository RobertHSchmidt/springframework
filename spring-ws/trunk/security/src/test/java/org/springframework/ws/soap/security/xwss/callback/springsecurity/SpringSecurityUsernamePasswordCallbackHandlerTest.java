/*
 * Copyright ${YEAR} the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback.springsecurity;

import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import junit.framework.TestCase;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

public class SpringSecurityUsernamePasswordCallbackHandlerTest extends TestCase {

    private SpringSecurityUsernamePasswordCallbackHandler handler;

    protected void setUp() throws Exception {
        handler = new SpringSecurityUsernamePasswordCallbackHandler();
        Authentication authentication = new UsernamePasswordAuthenticationToken("Bert", "Ernie");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
    }

    public void testUsernameCallback() throws Exception {
        UsernameCallback usernameCallback = new UsernameCallback();
        handler.handleInternal(usernameCallback);
        assertEquals("Invalid username", "Bert", usernameCallback.getUsername());
    }

    public void testPasswordCallback() throws Exception {
        PasswordCallback passwordCallback = new PasswordCallback();
        handler.handleInternal(passwordCallback);
        assertEquals("Invalid username", "Ernie", passwordCallback.getPassword());
    }
}