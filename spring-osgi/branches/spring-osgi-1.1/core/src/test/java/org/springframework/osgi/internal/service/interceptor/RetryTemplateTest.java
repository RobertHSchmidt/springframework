/*
 * Copyright 2006-2008 the original author or authors.
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

package org.springframework.osgi.internal.service.interceptor;

import junit.framework.TestCase;

import org.springframework.osgi.service.importer.support.internal.support.DefaultRetryCallback;
import org.springframework.osgi.service.importer.support.internal.support.RetryCallback;
import org.springframework.osgi.service.importer.support.internal.support.RetryTemplate;

/**
 * 
 * @author Costin Leau
 * 
 */
public class RetryTemplateTest extends TestCase {

	private RetryTemplate template;
	private RetryCallback callback;
	private Object lock;


	private static class CountingCallback implements RetryCallback {

		private int count = 0;
		public final static int WAKES_THRESHOLD = 7;


		public synchronized Object doWithRetry() {
			System.out.println("woken up");
			count++;
			return null;
		}

		public boolean isComplete(Object result) {
			// postpone completion X times
			if (getCount() == WAKES_THRESHOLD)
				return true;
			return false;
		}

		public synchronized int getCount() {
			return count;
		}
	}


	protected void setUp() throws Exception {
		lock = new Object();
		callback = new DefaultRetryCallback() {

			public Object doWithRetry() {
				return null;
			}
		};
	}

	protected void tearDown() throws Exception {
		template = null;
	}

	public void testTemplateReset() throws Exception {
		long initialWaitTime = 20 * 1000;
		template = new RetryTemplate(0, initialWaitTime, lock);

		long start = System.currentTimeMillis();

		Runnable shutdownTask = new Runnable() {

			public void run() {
				// wait a bit

				try {
					Thread.sleep(3 * 1000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				System.out.println("About to reset template...");
				template.reset(0, 0);
				System.out.println("Resetted template...");
			}
		};

		Thread th = new Thread(shutdownTask, "shutdown-thread");
		th.start();
		assertNull(template.execute(callback));
		long stop = System.currentTimeMillis();

		long waitingTime = stop - start;
		assertTrue("Template not stopped in time", waitingTime < initialWaitTime);
	}

	public void testSpuriousWakeup() throws Exception {
		// wait 20s
		long initialWaitTime = 20 * 1000;
		final CountingCallback callback = new CountingCallback();
		template = new RetryTemplate(0, initialWaitTime, lock);

		Runnable spuriousTask = new Runnable() {

			public void run() {
				try {
					// start sending notifications to the lock
					do {
						// sleep for a while
						Thread.sleep(50);

						synchronized (lock) {
							lock.notifyAll();
						}
						System.out.println("sent notification");
					} while (!callback.isComplete(null));
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		};

		Thread th = new Thread(spuriousTask, "wake-up thread");
		th.start();

		long start = System.currentTimeMillis();
		template.execute(callback);
		long stop = System.currentTimeMillis();
		long waited = stop - start;

		assertEquals(CountingCallback.WAKES_THRESHOLD, callback.getCount());
		assertTrue(waited < initialWaitTime);
	}
}