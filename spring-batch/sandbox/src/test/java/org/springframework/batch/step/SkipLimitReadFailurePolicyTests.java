/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.step;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class SkipLimitReadFailurePolicyTests extends TestCase {

	public void testZeroFailures() {
		ReadContext readContext = new ReadContext();
		SkipLimitReadFailurePolicy policy = new SkipLimitReadFailurePolicy(Long.valueOf(2));
		assertTrue(policy.shouldContinue(readContext));
	}

	public void testLowerFailures() {
		ReadContext readContext = new ReadContext();
		SkipLimitReadFailurePolicy policy = new SkipLimitReadFailurePolicy(Long.valueOf(2));
		policy.registerFailure(new RuntimeException(), readContext);
		assertTrue(policy.shouldContinue(readContext));
	}

	public void testExactFailures() {
		ReadContext readContext = new ReadContext();
		SkipLimitReadFailurePolicy policy = new SkipLimitReadFailurePolicy(Long.valueOf(2));
		policy.registerFailure(new RuntimeException(), readContext);
		policy.registerFailure(new RuntimeException(), readContext);
		assertFalse(policy.shouldContinue(readContext));
	}

	public void testHigherFailures() {
		ReadContext readContext = new ReadContext();
		SkipLimitReadFailurePolicy policy = new SkipLimitReadFailurePolicy(Long.valueOf(2));
		policy.registerFailure(new RuntimeException(), readContext);
		policy.registerFailure(new RuntimeException(), readContext);
		policy.registerFailure(new RuntimeException(), readContext);
		assertFalse(policy.shouldContinue(readContext));
	}

	public void testFailuresIgnored() {
		ReadContext readContext = new ReadContext();
		List ignoredExceptions = new ArrayList(1);
		ignoredExceptions.add(RuntimeException.class);
		SkipLimitReadFailurePolicy policy = new SkipLimitReadFailurePolicy(Long.valueOf(2), ignoredExceptions);
		policy.registerFailure(new RuntimeException(), readContext);
		policy.registerFailure(new RuntimeException(), readContext);
		assertTrue(policy.shouldContinue(readContext));
	}

}
