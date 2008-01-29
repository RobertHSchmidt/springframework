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
package org.springframework.batch.itemreader;

public class MockItemReader implements ItemReader {

	private final int returnItemCount;

	private int returnedItemCount;
	
	private boolean markSupported = false;

	public MockItemReader() {
		this(-1);
	}

	public MockItemReader(int returnItemCount) {
		this.returnItemCount = returnItemCount;
	}
	
	public void setMarkSupported(boolean markSupported) {
	    this.markSupported = markSupported;
    }

	public void close() {
	}

	public void mark() {
		if(!markSupported) {
			throw new UnsupportedOperationException("The mark() method is not supported");
		}
	}

	public boolean markSupported() {
		return markSupported;
	}

	public Object read() {
		if (returnItemCount < 0 || returnedItemCount < returnItemCount) {
			return String.valueOf(returnedItemCount++);
		}
		return null;
	}

}
