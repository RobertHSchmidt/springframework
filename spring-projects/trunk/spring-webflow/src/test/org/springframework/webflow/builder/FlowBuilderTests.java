package org.springframework.webflow.builder;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.Flow;

public class FlowBuilderTests {
	public void testBuildNewXmlFlow() {
		ClassPathResource resource = new ClassPathResource("testFlow1.xml", getClass());
		XmlFlowBuilder builder = new XmlFlowBuilder(resource);
		FlowAssembler assembler = new FlowAssembler("testFlow", builder);
		assembler.assembleFlow();
		Flow result = builder.getResult();
	}
}
