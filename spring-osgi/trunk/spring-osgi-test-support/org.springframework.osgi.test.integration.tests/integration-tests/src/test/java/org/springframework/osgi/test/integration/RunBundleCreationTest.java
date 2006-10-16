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
package org.springframework.osgi.test.integration;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/**
 * @author Hal Hildebrand
 *         Date: Sep 25, 2006
 *         Time: 11:27:24 AM
 */
public class RunBundleCreationTest extends TestCase {
    public void testBundleCreation() throws Exception {
        TestCase test = new BundleCreationTests();
        TestResult result = new TestResult();
        test.setName("testAssertionPass");
        test.run(result);
        test.setName("testAssertionFailure");
        test.run(result);
        test.setName("testFailure");
        test.run(result);
        test.setName("testException");
        test.run(result);

        assertEquals(4, result.runCount());
        
        if (result.errorCount() > 1) {
        	// tell us what went wrong...
        	Enumeration errors = result.errors();
        	while (errors.hasMoreElements()) {
        		TestFailure testFailure = (TestFailure) errors.nextElement();
        		reportOn(testFailure);
        	}
        }
        assertEquals(1, result.errorCount());
        
        if (result.failureCount() > 2) {
        	// tell us what went wrong...
        	Enumeration failures = result.failures();
        	while (failures.hasMoreElements()) {
        		TestFailure testFailure = (TestFailure) failures.nextElement();
        		reportOn(testFailure);
        	}
        }
        assertEquals(2, result.failureCount());
    }
    
    private void reportOn(TestFailure aFailure) {
    	System.err.println(aFailure.failedTest());
    	System.err.println(aFailure.exceptionMessage());
    	System.err.println(aFailure.trace());
    }
}
