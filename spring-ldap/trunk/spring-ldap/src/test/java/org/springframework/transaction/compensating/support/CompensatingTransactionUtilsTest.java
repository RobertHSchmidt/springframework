package org.springframework.transaction.compensating.support;

import java.lang.reflect.Method;

import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.compensating.DirContextHolder;
import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.compensating.support.CompensatingTransactionHolderSupport;
import org.springframework.transaction.compensating.support.CompensatingTransactionUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class CompensatingTransactionUtilsTest extends TestCase {

    private MockControl dirContextControl;

    private DirContext dirContextMock;

    private MockControl contextSourceControl;

    private ContextSource contextSourceMock;

    private MockControl operationManagerControl;

    private CompensatingTransactionOperationManager operationManagerMock;

    protected void setUp() throws Exception {
        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        operationManagerControl = MockControl
                .createControl(CompensatingTransactionOperationManager.class);
        operationManagerMock = (CompensatingTransactionOperationManager) operationManagerControl
                .getMock();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    protected void tearDown() throws Exception {
        dirContextControl = null;
        dirContextMock = null;

        contextSourceControl = null;
        contextSourceMock = null;

        operationManagerControl = null;
        operationManagerMock = null;
    }

    protected void replay() {
        dirContextControl.replay();
        contextSourceControl.replay();
        operationManagerControl.replay();
    }

    protected void verify() {
        dirContextControl.verify();
        contextSourceControl.verify();
        operationManagerControl.verify();
    }


    public void testPerformOperation() throws Throwable {
        CompensatingTransactionHolderSupport holder = new DirContextHolder(dirContextMock, null);
        holder.setTransactionOperationManager(operationManagerMock);

        TransactionSynchronizationManager.bindResource(contextSourceMock,
                holder);

        Object[] expectedArgs = new Object[] { "someDn" };
        operationManagerMock.performOperation("unbind", expectedArgs);

        replay();
        CompensatingTransactionUtils.performOperation(contextSourceMock, dirContextMock,
                getUnbindMethod(), expectedArgs);
        verify();
    }

    public void testPerformOperation_NoTransaction() throws Throwable {
        Object[] expectedArgs = new Object[] { "someDn" };
        dirContextMock.unbind("someDn");

        replay();
        CompensatingTransactionUtils.performOperation(contextSourceMock, dirContextMock,
                getUnbindMethod(), expectedArgs);
        verify();
    }

    private Method getUnbindMethod() throws NoSuchMethodException {
        return DirContext.class.getMethod("unbind",
                new Class[] { String.class });
    }

    public void dummyMethod() {

    }

}
