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
package org.springframework.ldap.support.transaction;

import javax.naming.Name;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.LdapOperations;

/**
 * A {@link CompensatingTransactionOperationRecorder} keeping track of a rebind
 * operation. Creates {@link RebindOperationExecutor} objects in
 * {@link #recordOperation(Object[])}.
 * 
 * @author Mattias Arthursson
 */
public class RebindOperationRecorder implements
        CompensatingTransactionOperationRecorder {

    private LdapOperations ldapOperations;

    private TempEntryRenamingStrategy renamingStrategy = new DefaultTempEntryRenamingStrategy();

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            {@link LdapOperations} to use for getting the rollback
     *            information and supply to the {@link RebindOperationExecutor}.
     */
    public RebindOperationRecorder(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationRecorder#recordOperation(java.lang.Object[])
     */
    public CompensatingTransactionOperationExecutor recordOperation(
            Object[] args) {
        if (args == null || args.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid arguments for bind operation");
        }
        Name dn = LdapUtils.getFirstArgumentAsName(args);
        Object object = args[1];
        Attributes attributes = null;
        if (args[2] != null && !(args[2] instanceof Attributes)) {
            throw new IllegalArgumentException(
                    "Invalid third argument to bind operation");
        } else if (args[2] != null) {
            attributes = (Attributes) args[2];
        }

        Name temporaryName = renamingStrategy.getTemporaryName(dn);

        return new RebindOperationExecutor(ldapOperations, dn, temporaryName,
                object, attributes);
    }

    /**
     * Get the LdapOperations. For testing purposes.
     * 
     * @return the LdapOperations.
     */
    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    public TempEntryRenamingStrategy getRenamingStrategy() {
        return renamingStrategy;
    }

    public void setRenamingStrategy(TempEntryRenamingStrategy renamingStrategy) {
        this.renamingStrategy = renamingStrategy;
    }
}
