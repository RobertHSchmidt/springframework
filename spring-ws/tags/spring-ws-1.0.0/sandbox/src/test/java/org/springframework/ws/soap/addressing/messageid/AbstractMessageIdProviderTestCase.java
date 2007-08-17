/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.soap.addressing.messageid;

import junit.framework.TestCase;
import org.springframework.util.StringUtils;

public abstract class AbstractMessageIdProviderTestCase extends TestCase {

    private MessageIdProvider provider;

    protected final void setUp() throws Exception {
        provider = createProvider();
    }

    protected abstract MessageIdProvider createProvider();

    public void testProvider() {
        String messageId1 = provider.getMessageId(null);
        System.out.println(messageId1);
        assertTrue("Empty messageId", StringUtils.hasLength(messageId1));
        String messageId2 = provider.getMessageId(null);
        assertTrue("Empty messageId", StringUtils.hasLength(messageId2));
        assertFalse("Equal messageIds", messageId1.equals(messageId2));
    }
}
