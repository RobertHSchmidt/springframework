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

public class LineItem implements Item {

	private final long id;

	private final String line;

	public LineItem(long id, String line) {
		this.id = id;
		this.line = line;
	}

	public Object getId() {
		return id;
	}

	public String getLine() {
		return line;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LineItem)) {
			return false;
		}
		LineItem other = (LineItem) obj;
		return line.equals(other.line);
	}

	@Override
	public int hashCode() {
		return line.hashCode();
	}

	@Override
	public String toString() {
		return "Line: [" + id + "] " + line;
	}
}
