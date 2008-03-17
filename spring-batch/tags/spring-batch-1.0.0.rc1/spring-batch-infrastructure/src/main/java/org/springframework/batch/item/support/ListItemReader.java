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

package org.springframework.batch.item.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.support.AopUtils;
import org.springframework.batch.item.AbstractItemReader;
import org.springframework.batch.item.ItemReader;

/**
 * An {@link ItemReader} that pulls data from a list. Useful for testing.
 * 
 * @author Dave Syer
 * 
 */
public class ListItemReader extends AbstractItemReader {

	private List list;

	public ListItemReader(List list) {
		// If it is a proxy we assume it knows how to deal with its own state.
		// (It's probably transaction aware.)
		if (AopUtils.isAopProxy(list)) {
			this.list = list;
		}
		else {
			this.list = new ArrayList(list);
		}
	}

	public Object read() {
		if (!list.isEmpty()) {
			return list.remove(0);
		}
		return null;
	}

}
