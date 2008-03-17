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
import java.util.List;

import org.springframework.batch.chunk.Chunk;

public class StubChunkFactory {

	public static Chunk<LineItem> getChunk(int size) {
		List<LineItem> items = new ArrayList<LineItem>(size);
		for (int i = 0; i < size; i++) {
			items.add(new LineItem(i, "Item " + i));
		}
		return new Chunk<LineItem>(0, items);
	}
}
