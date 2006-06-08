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

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import com.sun.xml.wss.impl.callback.PasswordCallback;

/**
 * Simple callback handler that supplies a username and password to a username token at runtime.
 * <p/>
 * This class handles <code>UsernameCallback</code>s and <code>PasswordCallback</code>s, and throws an
 * <code>UnsupportedCallbackException</code> for others
 *
 * @author Arjen Poutsma
 * @see #setUsername(String)
 * @see #setPassword(String)
 */
public class SimpleUsernamePasswordCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    private String username;

    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(username, "username must be set");
        Assert.hasLength(password, "password must be set");
    }

    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof UsernameCallback) {
            UsernameCallback usernameCallback = (UsernameCallback) callback;
            usernameCallback.setUsername(username);
        }
        else if (callback instanceof PasswordCallback) {
            PasswordCallback passwordCallback = (PasswordCallback) callback;
            passwordCallback.setPassword(password);
        }
        else {
            throw new UnsupportedCallbackException(callback);
        }
    }
}
