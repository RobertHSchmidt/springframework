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

import junit.framework.*;
import org.springframework.ws.soap.security.xwss.callback.SimpleUsernamePasswordCallbackHandler;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import com.sun.xml.wss.impl.callback.PasswordCallback;

public class SimpleUsernamePasswordCallbackHandlerTest extends TestCase {

    private SimpleUsernamePasswordCallbackHandler handler;

    protected void setUp() throws Exception {
        handler = new SimpleUsernamePasswordCallbackHandler();
        handler.setUsername("Bert");
        handler.setPassword("Ernie");
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