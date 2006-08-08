package org.springframework.webflow.registry;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.internal.builder.FlowAssembler;
import org.springframework.webflow.execution.internal.builder.RefreshableFlowDefinitionHolder;
import org.springframework.webflow.execution.internal.builder.xml.XmlFlowBuilder;

public class RefreshableFlowHolderTests extends TestCase {

	public void testNoRefreshOnNoChange() {
		File parent = new File("src/test/java/org/springframework/webflow/registry");
		Resource location = new FileSystemResource(new File(parent, "flow.xml"));
		XmlFlowBuilder flowBuilder = new XmlFlowBuilder(location);
		FlowAssembler assembler = new FlowAssembler("flow", flowBuilder);
		RefreshableFlowDefinitionHolder holder = new RefreshableFlowDefinitionHolder(assembler);
		assertEquals("flow", holder.getFlowId());
		assertSame(flowBuilder, holder.getFlowBuilder());
		assertEquals(0, holder.getLastModified());
		assertTrue(!holder.isAssembled());
		holder.getFlow();
		assertTrue(holder.isAssembled());
		long lastModified = holder.getLastModified();
		assertTrue(lastModified != -1);
		assertTrue(lastModified > 0);
		holder.getFlow();
		assertEquals(lastModified, holder.getLastModified());
	}

	public void testNoRefreshOnChange() {
		File parent = new File("src/test/java/org/springframework/webflow/registry");
		Resource location = new FileSystemResource(new File(parent, "flow.xml"));
		XmlFlowBuilder flowBuilder = new XmlFlowBuilder(location);
		FlowAssembler assembler = new FlowAssembler("flow", flowBuilder);
		RefreshableFlowDefinitionHolder holder = new RefreshableFlowDefinitionHolder(assembler);
		holder.getFlow();
		assertTrue(holder.isAssembled());
		long lastModified = holder.getLastModified();
		holder.getFlow();
		assertEquals(lastModified, holder.getLastModified());
	}
}