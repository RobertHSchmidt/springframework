package org.springframework.ldap.support;

import javax.naming.NamingException;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.DirContextProcessor;

public class AggregateDirContextProcessorTest extends TestCase {

    private MockControl processor1Control;

    private DirContextProcessor processor1Mock;

    private MockControl processor2Control;

    private DirContextProcessor processor2Mock;

    private AggregateDirContextProcessor tested;

    protected void setUp() throws Exception {
        super.setUp();

        // Create processor1 mock
        processor1Control = MockControl
                .createControl(DirContextProcessor.class);
        processor1Mock = (DirContextProcessor) processor1Control.getMock();

        // Create processor2 mock
        processor2Control = MockControl
                .createControl(DirContextProcessor.class);
        processor2Mock = (DirContextProcessor) processor2Control.getMock();

        tested = new AggregateDirContextProcessor();
        tested.addDirContextProcessor(processor1Mock);
        tested.addDirContextProcessor(processor2Mock);

    }

    protected void tearDown() throws Exception {
        super.tearDown();

        processor1Control = null;
        processor1Mock = null;

        processor2Control = null;
        processor2Mock = null;
    }

    protected void replay() {
        processor1Control.replay();
        processor2Control.replay();

    }

    protected void verify() {
        processor1Control.verify();
        processor2Control.verify();
    }

    public void testPreProcess() throws NamingException {
        processor1Mock.preProcess(null);
        processor2Mock.preProcess(null);
        
        replay();
        
        tested.preProcess(null);
        
        verify();
    }

    public void testPostProcess() throws NamingException {
        processor1Mock.postProcess(null);
        processor2Mock.postProcess(null);
        
        replay();
        
        tested.postProcess(null);
        
        verify();
    }

}
