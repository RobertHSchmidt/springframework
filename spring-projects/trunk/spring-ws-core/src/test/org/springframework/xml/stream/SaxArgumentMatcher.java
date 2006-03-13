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

package org.springframework.xml.stream;

import java.util.Arrays;

import org.easymock.AbstractMatcher;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Easymock <code>ArgumentMatcher</code> implementation that matches SAX arguments.
 */
public class SaxArgumentMatcher extends AbstractMatcher {

    public boolean matches(Object[] expected, Object[] actual) {
        if (expected == actual) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        if (expected.length != actual.length) {
            throw new IllegalArgumentException("Expected and actual arguments must have the same size");
        }
        if (expected.length == 3 && (expected[0] instanceof char[]) && (expected[1] instanceof Integer) &&
                (expected[2] instanceof Integer)) {
            // handling of the character(char[], int, int) methods
            String expectedString = new String((char[]) expected[0], ((Integer) expected[1]).intValue(),
                    ((Integer) expected[2]).intValue());
            String actualString =
                    new String((char[]) actual[0], ((Integer) actual[1]).intValue(), ((Integer) actual[2]).intValue());
            return (expectedString.equals(actualString));
        }
        else if (expected.length == 1 && (expected[0] instanceof Locator)) {
            return true;
        }
        else {
            return super.matches(expected, actual);
        }
    }

    protected boolean argumentMatches(Object expected, Object actual) {
        if (expected instanceof char[]) {
            return Arrays.equals((char[]) expected, (char[]) actual);
        }
        else if (expected instanceof Attributes) {
            Attributes expectedAttributes = (Attributes) expected;
            Attributes actualAttributes = (Attributes) actual;
            if (expectedAttributes.getLength() != actualAttributes.getLength()) {
                return false;
            }
            for (int i = 0; i < expectedAttributes.getLength(); i++) {
                if (!expectedAttributes.getURI(i).equals(actualAttributes.getURI(i)) ||
                        !expectedAttributes.getQName(i).equals(actualAttributes.getQName(i)) ||
                        !expectedAttributes.getType(i).equals(actualAttributes.getType(i)) ||
                        !expectedAttributes.getValue(i).equals(actualAttributes.getValue(i))) {
                    return false;
                }
            }
            return true;
        }
        else if (expected instanceof Locator) {
            Locator expectedLocator = (Locator) expected;
            Locator actualLocator = (Locator) actual;
            return (expectedLocator.getColumnNumber() == actualLocator.getColumnNumber() &&
                    expectedLocator.getLineNumber() == actualLocator.getLineNumber());
        }
        return super.argumentMatches(expected, actual);
    }

    protected String argumentToString(Object argument) {
        if (argument instanceof char[]) {
            char[] array = (char[]) argument;
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < array.length; i++) {
                buffer.append(array[i]);
            }
            return buffer.toString();
        }
        else if (argument instanceof Attributes) {
            Attributes attributes = (Attributes) argument;
            StringBuffer buffer = new StringBuffer("[");
            for (int i = 0; i < attributes.getLength(); i++) {
                if (StringUtils.hasLength(attributes.getURI(i))) {
                    buffer.append('{');
                    buffer.append(attributes.getURI(i));
                    buffer.append('}');
                }
                buffer.append(attributes.getQName(i));
                if (i < attributes.getLength() - 1) {
                    buffer.append(", ");
                }
            }
            buffer.append(']');
            return buffer.toString();
        }
        else if (argument instanceof Locator) {
            Locator locator = (Locator) argument;
            StringBuffer buffer = new StringBuffer("[");
            buffer.append(locator.getLineNumber());
            buffer.append(',');
            buffer.append(locator.getColumnNumber());
            buffer.append(']');
            return buffer.toString();
        }
        else {
            return super.argumentToString(argument);
        }
    }
}
