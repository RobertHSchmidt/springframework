/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.engine.model;

import org.springframework.webflow.engine.model.VarModel;

import junit.framework.TestCase;

/**
 * Unit tests for {@link VarModel}.
 */
public class VarModelTests extends TestCase {

	public void testMerge() {
		VarModel child = new VarModel("child", "childclass");
		VarModel parent = new VarModel("parent", "parentclass");
		child.merge(parent);
		assertEquals("child", child.getName());
	}

	public void testMergeNullParent() {
		VarModel child = new VarModel("child", "childclass");
		VarModel parent = null;
		child.merge(parent);
		assertEquals("child", child.getName());
	}

	public void testMergeOverrideMatch() {
		VarModel child = new VarModel("child", "childclass");
		VarModel parent = new VarModel("child", "childclass", "childscope");
		child.merge(parent);
		assertEquals("childscope", child.getScope());
	}

	public void testMergeOverrideMatchFailed() {
		VarModel child = new VarModel("child", "childclass");
		VarModel parent = new VarModel("parent", "parentclass", "parentscope");
		child.merge(parent);
		assertEquals(null, child.getScope());
	}

}
