/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubItemWriter implements ItemWriter<LineItem> {

	private final Logger logger = LoggerFactory.getLogger(StubItemWriter.class);

	private final List<LineItem> items = new ArrayList<LineItem>();

	public List<LineItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public int getItemCount() {
		return items.size();
	}

	public void write(LineItem item) throws ItemWriteException {
		items.add(item);
		logger.info("Received item {}", item.getId());
	}

}
