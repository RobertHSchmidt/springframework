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
package org.springframework.batch.chunkprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkContext {

	private final Map context = new HashMap();

	private final List skippedItems = new ArrayList();

	public void addSkippedItem(Object item) {
		skippedItems.add(item);
	}

	public boolean containsSkippedItem(Object item) {
		return skippedItems.contains(item);
	}

	public List getSkippedItems() {
		return new ArrayList(skippedItems);
	}

	public boolean containsKey(Object key) {
		return context.containsKey(key);
	}

	public Object get(Object key) {
		return context.get(key);
	}

	public Object put(Object arg0, Object arg1) {
		return context.put(arg0, arg1);
	}

	public void putAll(Map arg0) {
		context.putAll(arg0);
	}

	public Object remove(Object key) {
		return context.remove(key);
	}

}
