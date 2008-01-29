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

package org.springframework.batch.item.reader;

import org.springframework.batch.item.ItemReader;

/**
 * Base class for {@link ItemReader} implementations.
 * @author Dave Syer
 *
 */
public abstract class AbstractItemReader implements ItemReader {

	/**
	 * Simply returns the item itself. Will be adequate for many purposes, but
	 * not (for example) if the item is a message - in which case the identifier
	 * should be used.
	 * 
	 * @see org.springframework.batch.item.ItemReader#getKey(java.lang.Object)
	 */
	public Object getKey(Object item) {
		return item;
	}
	
	public void close() throws Exception {		
	}

}
