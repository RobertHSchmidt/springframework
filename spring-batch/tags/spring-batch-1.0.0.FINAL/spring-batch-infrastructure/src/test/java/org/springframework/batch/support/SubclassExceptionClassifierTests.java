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

package org.springframework.batch.support;

import java.util.Collections;
import java.util.LinkedHashMap;

import org.springframework.batch.support.SubclassExceptionClassifier;

import junit.framework.TestCase;

public class SubclassExceptionClassifierTests extends TestCase {

	SubclassExceptionClassifier classifier = new SubclassExceptionClassifier();

	public void testClassifyNullIsDefault() {
		assertEquals(classifier.classify(null), classifier.getDefault());
	}

	public void testClassifyRandomException() {
		assertEquals(classifier.classify(new IllegalStateException("Foo")), classifier.getDefault());
	}

	public void testIllegalMapWithNonClass() {
		try {
			classifier.setTypeMap(Collections.singletonMap("bar", "foo"));
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testIllegalMapWithClass() {
		try {
			classifier.setTypeMap(Collections.singletonMap(String.class, "foo"));
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testClassifyExactMatch() {
		classifier.setTypeMap(Collections.singletonMap(IllegalStateException.class, "foo"));
		assertEquals("foo", classifier.classify(new IllegalStateException("Foo")));
	}

	public void testClassifySubclassMatch() {
		classifier.setTypeMap(Collections.singletonMap(RuntimeException.class, "foo"));
		assertEquals("foo", classifier.classify(new IllegalStateException("Foo")));
	}

	public void testClassifySuperclassDoesNotMatch() {
		classifier.setTypeMap(Collections.singletonMap(IllegalStateException.class, "foo"));
		assertEquals(classifier.getDefault(), classifier.classify(new RuntimeException("Foo")));
	}

	public void testClassifyAncestorMatch() {
		classifier.setTypeMap(new LinkedHashMap() {{
			put(Exception.class, "bar");
			put(IllegalArgumentException.class, "foo");
			put(RuntimeException.class, "bucket");
		}});
		assertEquals("bucket", classifier.classify(new IllegalStateException("Foo")));
	}
}
