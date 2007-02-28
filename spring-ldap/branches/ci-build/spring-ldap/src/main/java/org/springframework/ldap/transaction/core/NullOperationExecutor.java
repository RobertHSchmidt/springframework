/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.transaction.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.transaction.CompensatingTransactionOperationExecutor;

/**
 * A {@link CompensatingTransactionOperationExecutor} that performs nothing.
 * 
 * @author Mattias Arthursson
 */
public class NullOperationExecutor implements
        CompensatingTransactionOperationExecutor {

    private static Log log = LogFactory.getLog(NullOperationExecutor.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#rollback()
     */
    public void rollback() {
        log.info("Rolling back null operation");
    }

    public void commit() {
        log.info("Committing back null operation");
    }

    public void performOperation() {
        log.info("Performing null operation");
    }

}
