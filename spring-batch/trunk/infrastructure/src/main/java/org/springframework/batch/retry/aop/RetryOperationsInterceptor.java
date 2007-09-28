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

package org.springframework.batch.retry.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.batch.retry.RetryCallback;
import org.springframework.batch.retry.RetryContext;
import org.springframework.batch.retry.RetryOperations;
import org.springframework.batch.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * A {@link MethodInterceptor} that can be used to automatically retry calls to
 * a method on a service if it fails. The injected {@link RetryOperations} is
 * used to control the number of retries. By default it will retry a fixed
 * number of times, according to the defaults in {@link RetryTemplate}.
 * 
 * @author Rob Harrop
 * @author Dave Syer
 * @since 2.1
 */
public class RetryOperationsInterceptor implements MethodInterceptor {

	private RetryOperations retryOperations = new RetryTemplate();

	public void setRetryTemplate(RetryOperations retryTemplate) {
		Assert.notNull(retryTemplate, "'retryOperations' cannot be null.");
		this.retryOperations = retryTemplate;
	}

	public Object invoke(final MethodInvocation invocation) throws Throwable {
		// TODO: use the method name to initialise a statistics context
		return this.retryOperations.execute(new RetryCallback() {

			public Object doWithRetry(RetryContext context) throws Throwable {

				/*
				 * If we don't copy the invocation carefully it won't keep a
				 * reference to the other interceptors in the chain. We don't
				 * have a choice here but to specialise to
				 * ReflectiveMethodInvocation (but how often would another
				 * implementation come along?).
				 */
				if (invocation instanceof ProxyMethodInvocation) {
					return ((ProxyMethodInvocation) invocation).invocableClone().proceed();
				} else {
					throw new IllegalStateException(
							"MethodInvocation of the wrong type detected - this should not happen with Spring AOP, so please raise an issue if you see this exception");
				}
			}

		});
	}
}
