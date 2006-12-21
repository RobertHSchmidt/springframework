/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.engine.builder;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.builder.xml.XmlFlowBuilder;

public class RefreshableFlowHolderTests extends TestCase {

	public void testNoRefreshOnNoChange() {
		File parent = new File("src/test/java/org/springframework/webflow/engine/builder/xml");
		Resource location = new FileSystemResource(new File(parent, "flow.xml"));
		XmlFlowBuilder flowBuilder = new XmlFlowBuilder(location);
		FlowAssembler assembler = new FlowAssembler("flow", flowBuilder);
		RefreshableFlowDefinitionHolder holder = new RefreshableFlowDefinitionHolder(assembler);
		assertEquals("flow", holder.getFlowDefinitionId());
		assertSame(flowBuilder, holder.getFlowBuilder());
		assertEquals(0, holder.getLastModified());
		assertTrue(!holder.isAssembled());
		FlowDefinition flow1 = holder.getFlowDefinition();
		assertTrue(holder.isAssembled());
		long lastModified = holder.getLastModified();
		assertTrue(lastModified != -1);
		assertTrue(lastModified > 0);
		FlowDefinition flow2 = holder.getFlowDefinition();
		assertEquals("flow", flow2.getId());
		assertEquals(lastModified, holder.getLastModified());
		assertSame(flow1, flow2);
	}
}