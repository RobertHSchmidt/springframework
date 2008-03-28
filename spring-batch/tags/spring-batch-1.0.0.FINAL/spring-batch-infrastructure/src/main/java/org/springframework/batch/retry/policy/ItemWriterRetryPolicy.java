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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.FailedItemIdentifier;
import org.springframework.batch.item.ItemKeyGenerator;
import org.springframework.batch.item.ItemRecoverer;
import org.springframework.batch.repeat.support.RepeatSynchronizationManager;
import org.springframework.batch.retry.RetryCallback;
import org.springframework.batch.retry.RetryContext;
import org.springframework.batch.retry.RetryPolicy;
import org.springframework.batch.retry.TerminatedRetryException;
import org.springframework.batch.retry.callback.ItemWriterRetryCallback;
import org.springframework.batch.retry.context.RetryContextSupport;
import org.springframework.util.Assert;

/**
 * A {@link RetryPolicy} that detects an {@link ItemWriterRetryCallback} when it
 * opens a new context, and uses it to make sure the item is in place for later
 * decisions about how to retry or backoff. The callback should be an instance
 * of {@link ItemWriterRetryCallback} otherwise an exception will be thrown when
 * the context is created.
 * 
 * @author Dave Syer
 * 
 */
public class ItemWriterRetryPolicy extends AbstractStatefulRetryPolicy {

	protected Log logger = LogFactory.getLog(getClass());

	public static final String EXHAUSTED = ItemWriterRetryPolicy.class.getName() + ".EXHAUSTED";

	private RetryPolicy delegate;

	/**
	 * Convenience constructor to set delegate on init.
	 * 
	 * @param delegate
	 */
	public ItemWriterRetryPolicy(RetryPolicy delegate) {
		super();
		this.delegate = delegate;
	}

	/**
	 * Default constructor. Creates a new {@link SimpleRetryPolicy} for the
	 * delegate.
	 */
	public ItemWriterRetryPolicy() {
		this(new SimpleRetryPolicy());
	}

	/**
	 * Setter for delegate.
	 * 
	 * @param delegate
	 */
	public void setDelegate(RetryPolicy delegate) {
		this.delegate = delegate;
	}

	/**
	 * Check the history of this item, and if it has reached the retry limit,
	 * then return false.
	 * 
	 * @see org.springframework.batch.retry.RetryPolicy#canRetry(org.springframework.batch.retry.RetryContext)
	 */
	public boolean canRetry(RetryContext context) {
		return ((RetryPolicy) context).canRetry(context);
	}

	/**
	 * Delegates to the delegate context.
	 * 
	 * @see org.springframework.batch.retry.RetryPolicy#close(org.springframework.batch.retry.RetryContext)
	 */
	public void close(RetryContext context) {
		((RetryPolicy) context).close(context);
	}

	/**
	 * Create a new context for the execution of the callback, which must be an
	 * instance of {@link ItemWriterRetryCallback}.
	 * 
	 * @see org.springframework.batch.retry.RetryPolicy#open(org.springframework.batch.retry.RetryCallback,
	 * RetryContext)
	 * 
	 * @throws IllegalStateException if the callback is not of the required
	 * type.
	 */
	public RetryContext open(RetryCallback callback, RetryContext parent) {
		Assert.state(callback instanceof ItemWriterRetryCallback, "Callback must be ItemProviderRetryCallback");
		ItemWriterRetryContext context = new ItemWriterRetryContext((ItemWriterRetryCallback) callback, parent);
		context.open(callback, null);
		return context;
	}

	/**
	 * If {@link #canRetry(RetryContext)} is false then take remedial action (if
	 * implemented by subclasses), and remove the current item from the history.
	 * 
	 * @see org.springframework.batch.retry.RetryPolicy#registerThrowable(org.springframework.batch.retry.RetryContext,
	 * java.lang.Throwable)
	 */
	public void registerThrowable(RetryContext context, Throwable throwable) throws TerminatedRetryException {
		((RetryPolicy) context).registerThrowable(context, throwable);
		// The throwable is stored in the delegate context.
	}

	/**
	 * Call recovery path (if any) and clean up context history.
	 * 
	 * @see org.springframework.batch.retry.policy.AbstractStatefulRetryPolicy#handleRetryExhausted(org.springframework.batch.retry.RetryContext)
	 */
	public Object handleRetryExhausted(RetryContext context) throws Exception {
		return ((RetryPolicy) context).handleRetryExhausted(context);
	}

	private class ItemWriterRetryContext extends RetryContextSupport implements RetryPolicy {

		final private Object item;

		// The delegate context...
		private RetryContext delegateContext;

		final private ItemRecoverer recoverer;

		final private ItemKeyGenerator keyGenerator;

		final private FailedItemIdentifier failedItemIdentifier;

		public ItemWriterRetryContext(ItemWriterRetryCallback callback, RetryContext parent) {
			super(parent);
			this.item = callback.getItem();
			this.recoverer = callback.getRecoverer();
			this.keyGenerator = callback.getKeyGenerator();
			this.failedItemIdentifier = callback.getFailedItemIdentifier();
		}

		public boolean canRetry(RetryContext context) {
			return delegate.canRetry(this.delegateContext);
		}

		public void close(RetryContext context) {
			delegate.close(this.delegateContext);
		}

		public RetryContext open(RetryCallback callback, RetryContext parent) {
			if (hasFailed(failedItemIdentifier, keyGenerator, item)) {
				this.delegateContext = retryContextCache.get(keyGenerator.getKey(item));
			}
			if (this.delegateContext == null) {
				// Only create a new context if we don't know the history of
				// this item:
				this.delegateContext = delegate.open(callback, null);
			}
			// The return value shouldn't be used...
			return null;
		}

		public void registerThrowable(RetryContext context, Throwable throwable) throws TerminatedRetryException {
			retryContextCache.put(keyGenerator.getKey(item), this.delegateContext);
			delegate.registerThrowable(this.delegateContext, throwable);
		}

		public boolean isExternal() {
			// Not called...
			throw new UnsupportedOperationException("Not supported - this code should be unreachable.");
		}

		public boolean shouldRethrow(RetryContext context) {
			// Not called...
			throw new UnsupportedOperationException("Not supported - this code should be unreachable.");
		}

		public Object handleRetryExhausted(RetryContext context) throws Exception {
			// If there is no going back, then we can remove the history
			retryContextCache.remove(keyGenerator.getKey(item));
			RepeatSynchronizationManager.setCompleteOnly();
			if (recoverer != null) {
				boolean success = recoverer.recover(item, context.getLastThrowable());
				if (!success) {
					int count = context.getRetryCount();
					logger.error("Could not recover from error after retry exhausted after [" + count + "] attempts.",
							context.getLastThrowable());
				}
			}
			return item;
		}

		public Throwable getLastThrowable() {
			return delegateContext.getLastThrowable();
		}

		public int getRetryCount() {
			return delegateContext.getRetryCount();
		}

	}

	/**
	 * Extension point for cases where it is possible to avoid a cache hit by
	 * inspecting the item to determine if could ever have been seen before. In
	 * a messaging environment where the item is a message, it can be inspected
	 * to see if it has been delivered before.<br/>
	 * 
	 * The default implementation of this method checks for a non-null
	 * {@link FailedItemIdentifier}. Otherwise we just check the cache for the
	 * item key.
	 * 
	 * @param failedItemIdentifier
	 * @param keyGenerator
	 * @param item
	 * @return
	 */
	protected boolean hasFailed(FailedItemIdentifier failedItemIdentifier, ItemKeyGenerator keyGenerator, Object item) {
		if (failedItemIdentifier != null) {
			return failedItemIdentifier.hasFailed(item);
		}
		return retryContextCache.containsKey(keyGenerator.getKey(item));
	}

}
