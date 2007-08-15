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
package org.springframework.batch.item.provider;

import junit.framework.TestCase;

import org.springframework.batch.io.file.FieldSet;
import org.springframework.batch.io.file.FieldSetInputSource;
import org.springframework.batch.io.file.support.SimpleFlatFileInputSource;
import org.springframework.core.io.ByteArrayResource;

/**
 * @author Dave Syer
 *
 */
public class AbstractFieldSetItemProviderTests extends TestCase {

	public void testNotOpen() throws Exception {
		TestItemProvider provider = new TestItemProvider();
		provider.setSource(getInputSource("one\ntwo\nthree"));
		assertNotNull(provider.next());
	}

	public void testAfterPropertiesSet() throws Exception {
		TestItemProvider provider = new TestItemProvider();
		try {
			provider.afterPropertiesSet();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	private static class TestItemProvider extends AbstractFieldSetItemProvider {
		protected Object transform(FieldSet fieldSet) {
			return fieldSet.toString();
		}
	}

	private FieldSetInputSource getInputSource(String data) throws Exception {
		SimpleFlatFileInputSource template = new SimpleFlatFileInputSource();
		template.setResource(new ByteArrayResource(data.getBytes()));
		template.afterPropertiesSet();
		return template;
	}
}
