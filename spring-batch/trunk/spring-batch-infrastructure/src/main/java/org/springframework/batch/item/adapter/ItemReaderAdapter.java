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

package org.springframework.batch.item.adapter;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.MarkFailedException;
import org.springframework.batch.item.ResetFailedException;

/**
 * Invokes a custom method on a delegate plain old Java object which itself
 * provides an item.
 * 
 * @author Robert Kasanicky
 */
public class ItemReaderAdapter<T> extends AbstractMethodInvokingDelegator<T> implements ItemReader<T> {

	/**
	 * @return return value of the target method.
	 */
	public T read() throws Exception {
		return invokeDelegateMethod();
	}

	/**
	 * No-op.
	 */
	public void mark() throws MarkFailedException {
	}

	/**
	 * No-op.
	 */
	public void reset() throws ResetFailedException {
	}
}
