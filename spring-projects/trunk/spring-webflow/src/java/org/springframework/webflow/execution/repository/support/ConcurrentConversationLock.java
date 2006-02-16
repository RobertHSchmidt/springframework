/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.execution.repository.support;

import org.springframework.webflow.execution.repository.ConversationLock;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

/**
 * A conversation lock that relies on a {@link ReentrantLock} within Doug Lea's
 * <code>util.concurrent</code> package.  For use on JDK 1.3 and 1.4.
 * 
 * @author Keith Donald
 */
class ConcurrentConversationLock implements ConversationLock {

	private static boolean utilConcurrentPresent;
	
	static {
		try {
			Class.forName("EDU.oswego.cs.dl.util.concurrent.ReentrantLock");
			utilConcurrentPresent = true;
		}
		catch (ClassNotFoundException ex) {
			utilConcurrentPresent = false;
		}
	}

	/**
	 * Returns a flag indicating if <code>util.concurrent</code> is present in the classpath.
	 */
	public static boolean isUtilConcurrentPresent() {
		return utilConcurrentPresent;
	}
	
	/**
	 * The lock.
	 */
	private ReentrantLock lock = new ReentrantLock();

	public void lock() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			
		}
	}

	public void unlock() {
		lock.release();
	}
}