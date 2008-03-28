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

import org.springframework.batch.retry.RetryContext;

/**
 * Map-based implementation of {@link RetryContextCache}. The map backing the
 * cache of contexts is sytchronized.
 * 
 * @author Dave Syer
 * 
 */
public class MapRetryContextCache implements RetryContextCache {

	private Map map = Collections.synchronizedMap(new HashMap());

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public RetryContext get(Object key) {
		return (RetryContext) map.get(key);
	}

	public void put(Object key, RetryContext context) {
		map.put(key, context);
	}

	public void remove(Object key) {
		map.remove(key);
	}

}
