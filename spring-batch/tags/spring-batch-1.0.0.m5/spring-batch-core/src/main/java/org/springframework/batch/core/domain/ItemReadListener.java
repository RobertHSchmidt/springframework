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
package org.springframework.batch.core.domain;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

/**
 * Listener interface around the reading of an item.
 * 
 * @author Lucas Ward
 *
 */
public interface ItemReadListener extends BatchListener {

	/**
	 * Called before {@link ItemReader#read()}
	 */
	void beforeRead();
	
	/**
	 * Called after {@link ItemReader#read()}
	 * 
	 * @param item returned from read()
	 */
	void afterRead(Object item);
	
	/**
	 * Called if an error occurs while trying to write.
	 * 
	 * @param ex thrown from {@link ItemWriter}
	 */
	void onReadError(Exception ex);
}
