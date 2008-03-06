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
package org.springframework.batch.execution.step.support;

import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.handler.ExceptionHandler;
import org.springframework.batch.repeat.synch.RepeatSynchronizationManager;
import org.springframework.batch.retry.RetryCallback;
import org.springframework.batch.retry.RetryContext;
import org.springframework.batch.retry.RetryPolicy;
import org.springframework.batch.retry.listener.RetryListenerSupport;

/**
 * @author Dave Syer
 * 
 */
public class SimpleRetryExceptionHandler extends RetryListenerSupport implements ExceptionHandler {

	/**
	 * Attribute key, whose existence signals an exhausted retry.
	 */
	private static final String EXHAUSTED = SimpleRetryExceptionHandler.class.getName() + ".RETRY_EXHAUSTED";

	private RetryPolicy retryPolicy;

	private ExceptionHandler exceptionHandler;

	/**
	 * @param retryPolicy
	 * @param exceptionHandler
	 */
	public SimpleRetryExceptionHandler(RetryPolicy retryPolicy, ExceptionHandler exceptionHandler) {
		this.retryPolicy = retryPolicy;
		this.exceptionHandler = exceptionHandler;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.repeat.exception.handler.ExceptionHandler#handleException(org.springframework.batch.repeat.RepeatContext,
	 * java.lang.Throwable)
	 */
	public void handleException(RepeatContext context, Throwable throwable) throws RuntimeException {
		// Only bother to check the delegate exception handler if we know that
		// retry is exhausted
		if (context.hasAttribute(EXHAUSTED)) {
			exceptionHandler.handleException(context, throwable);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.retry.RetryListener#close(org.springframework.batch.retry.RetryContext,
	 * org.springframework.batch.retry.RetryCallback, java.lang.Throwable)
	 */
	public void close(RetryContext context, RetryCallback callback, Throwable throwable) {
		if (!retryPolicy.canRetry(context)) {
			getRepeatContext().setAttribute(EXHAUSTED, "true");
		}
	}

	/**
	 * Get the parent context (the retry is in an inner "chunk" loop and we want
	 * the exception to be handled at the outer "step" level).
	 * @return the {@link RepeatContext} that should hold the exhausted flag.
	 */
	private RepeatContext getRepeatContext() {
		RepeatContext context = RepeatSynchronizationManager.getContext();
		if (context.getParent() != null) {
			return context.getParent();
		}
		return context;
	}

}
