/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing.server;

import org.springframework.ws.soap.addressing.version.Addressing200408;
import org.springframework.ws.soap.addressing.version.AddressingVersion;

public class AddressingInterceptor200408Test extends AbstractAddressingInterceptorTestCase {

    protected AddressingVersion getVersion() {
        return new Addressing200408();
    }

    protected String getTestPath() {
        return "200408";
    }

    public void testNoneReplyTo() throws Exception {
        // This version of the spec does not have none addresses
    }
}
