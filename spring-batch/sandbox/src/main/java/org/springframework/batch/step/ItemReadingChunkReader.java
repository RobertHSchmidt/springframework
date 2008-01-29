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
package org.springframework.batch.step;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.util.Assert;

/**
 * 
 * @author Ben Hale
 */
public class ItemReadingChunkReader implements ChunkReader {

	private final ItemReader itemReader;

	private long chunkCounter = 0;

	private ReadFailurePolicy readFailurePolicy = new AlwaysSkipReadFailurePolicy();

	public ItemReadingChunkReader(ItemReader itemReader) {
		this.itemReader = itemReader;
	}

	public void setReadFailurePolicy(ReadFailurePolicy readFailurePolicy) {
		this.readFailurePolicy = readFailurePolicy;
	}

	public void close() throws Exception {
		itemReader.close();
	}

	public Chunk read(int size, ReadContext readContext) throws ReadFailureException {
		Assert.isTrue(size > 0, "Chunk size must be greater than 0");

		int counter = 0;
		List items = new ArrayList(size);

		Object item;
		while (counter < size) {
			if (!readFailurePolicy.shouldContinue(readContext)) {
				throw readFailurePolicy.getException(readContext);
			}

			try {
				item = itemReader.read();
				if (item == null) {
					break;
				}
				items.add(item);
				counter++;
			} catch (Exception e) {
				readFailurePolicy.registerFailure(e, readContext);
			}
		}

		if (items.size() == 0) {
			return null;
		}

		return new Chunk(getChunkId(), items);
	}

	private synchronized Long getChunkId() {
		return new Long(chunkCounter++);
	}

}
