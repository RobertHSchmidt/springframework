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

package org.springframework.batch.retry.policy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.batch.common.ExceptionClassifierSupport;
import org.springframework.batch.retry.RetryContext;
import org.springframework.batch.retry.synch.RetrySynchronizationManager;

public class ExceptionClassifierRetryPolicyTests extends TestCase {

	ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();

	public void testDefaultPolicies() throws Exception {
		RetryContext context = policy.open(null);
		assertNotNull(context);
	}

	public void testTrivialPolicies() throws Exception {
		policy.setPolicyMap(Collections.singletonMap(ExceptionClassifierSupport.DEFAULT, new MockRetryPolicySupport()));
		RetryContext context = policy.open(null);
		assertNotNull(context);
		assertTrue(policy.canRetry(context));
	}

	public void testNullPolicies() throws Exception {
		policy.setPolicyMap(new HashMap());
		try {
			policy.open(null);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testClassifierOperates() throws Exception {

		Map map = new HashMap();
		map.put(ExceptionClassifierSupport.DEFAULT, new AlwaysRetryPolicy());
		map.put("foo", new NeverRetryPolicy());
		policy.setPolicyMap(map);

		RetryContext context = policy.open(null);
		assertNotNull(context);

		assertTrue(policy.canRetry(context));
		policy.registerThrowable(context, new IllegalArgumentException());
		assertTrue(policy.canRetry(context));

		policy.setExceptionClassifier(new ExceptionClassifierSupport() {
			public Object classify(Throwable throwable) {
				//TODO probably (==null) and (!=null) branches are interchanged?
				if (throwable != null) {
					return "foo";
				}
				return super.classify(throwable);
			}
		});

		// The context saves the classifier, so changing it now has no effect
		assertTrue(policy.canRetry(context));
		policy.registerThrowable(context, new IllegalArgumentException());
		assertTrue(policy.canRetry(context));

		// But now the classifier will be active in the new context...
		context = policy.open(null);
		assertTrue(policy.canRetry(context));
		policy.registerThrowable(context, new IllegalArgumentException());
		assertFalse(policy.canRetry(context));

	}

	int count = 0;

	public void testClose() throws Exception {
		policy.setPolicyMap(Collections.singletonMap(ExceptionClassifierSupport.DEFAULT, new MockRetryPolicySupport() {
			public void close(RetryContext context) {
				count++;
			}
		}));
		RetryContext context = policy.open(null);

		// The mapped (child) policy hasn't been used yet, so if we close now
		// we don't incur the possible expense of ceating the child context.
		policy.close(context);
		assertEquals(0, count); // not classified yet
		// This forces a child context to be created and the child policy is
		// then closed
		policy.registerThrowable(context, new IllegalStateException());
		policy.close(context);
		assertEquals(1, count); // now classified
	}

	public void testRetryCount() throws Exception {
		ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
		RetryContext context = policy.open(null);
		assertNotNull(context);
		policy.registerThrowable(context, null);
		assertEquals(0, context.getRetryCount());
		policy.registerThrowable(context, new RuntimeException("foo"));
		assertEquals(1, context.getRetryCount());
		assertEquals("foo", context.getLastThrowable().getMessage());
	}

	public void testParent() throws Exception {
		ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
		RetryContext context = policy.open(null);
		RetrySynchronizationManager.register(context);
		RetryContext child = policy.open(null);
		assertNotSame(child, context);
		assertSame(context, child.getParent());
	}

}
