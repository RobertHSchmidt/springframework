package org.springframework.ldap.support;

import javax.naming.directory.DirContext;

import org.springframework.ldap.ContextSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ContextSourceTransactionManager extends
        AbstractPlatformTransactionManager {

    private ContextSource contextSource;

    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public ContextSource getContextSource() {
        return contextSource;
    }

    protected void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) transaction;

        if (txObject.getContextHolder() == null) {
            DirContext newCtx = getContextSource().getReadOnlyContext();
            txObject.setContextHolder(new DirContextHolder(newCtx));
            TransactionSynchronizationManager.bindResource(getContextSource(),
                    newCtx);
        }
    }

    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException {
        // Nothing much to do here.
    }

    protected Object doGetTransaction() throws TransactionException {
        DirContextHolder contextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(this.contextSource);
        ContextSourceTransactionObject txObject = new ContextSourceTransactionObject(
                contextHolder);
        return txObject;
    }

    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        // Perform compensating transaction cleanup using information stored in
        // ContextHolder.
    }

    private class ContextSourceTransactionObject {
        private DirContextHolder contextHolder;

        public ContextSourceTransactionObject(DirContextHolder contextHolder) {
            this.contextHolder = contextHolder;
        }

        public DirContextHolder getContextHolder() {
            return contextHolder;
        }

        public void setContextHolder(DirContextHolder contextHolder) {
            this.contextHolder = contextHolder;
        }

    }

}
