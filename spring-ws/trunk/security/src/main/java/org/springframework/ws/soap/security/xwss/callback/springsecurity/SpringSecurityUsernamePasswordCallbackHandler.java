/*
 * Copyright 2008 the original author or authors.
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

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

/**
 * Callback handler that adds username/password information to a mesage using an Spring Security {@link
 * org.springframework.security.context.SecurityContext}.
 * <p/>
 * This class handles <code>UsernameCallback</code>s and <code>PasswordCallback</code>s, and throws an
 * <code>UnsupportedCallbackException</code> for others
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class SpringSecurityUsernamePasswordCallbackHandler extends AbstractCallbackHandler {

    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof UsernameCallback) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                UsernameCallback usernameCallback = (UsernameCallback) callback;
                usernameCallback.setUsername(authentication.getName());
                return;
            }
            else {
                logger.warn(
                        "Cannot handle UsernameCallback: Spring Security SecurityContext contains no Authentication");
            }
        }
        else if (callback instanceof PasswordCallback) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                PasswordCallback passwordCallback = (PasswordCallback) callback;
                passwordCallback.setPassword(authentication.getCredentials().toString());
                return;
            }
            else {
                logger.warn(
                        "Canot handle PasswordCallback: Spring Security SecurityContext contains no Authentication");
            }
        }
        throw new UnsupportedCallbackException(callback);
    }
}