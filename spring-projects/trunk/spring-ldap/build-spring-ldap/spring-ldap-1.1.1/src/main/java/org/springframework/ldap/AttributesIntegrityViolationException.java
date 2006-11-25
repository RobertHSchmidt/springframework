/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Exception that indicates that an invalid or missing Attribute has been
 * supplied to an LDAP operation.
 * 
 * @author Mattias Arthursson
 * 
 */
public class AttributesIntegrityViolationException extends
        DataIntegrityViolationException {

    private static final long serialVersionUID = -6368616096960202571L;

    public AttributesIntegrityViolationException(String msg) {
        super(msg);
    }

    public AttributesIntegrityViolationException(String msg, Throwable t) {
        super(msg, t);
    }
}
