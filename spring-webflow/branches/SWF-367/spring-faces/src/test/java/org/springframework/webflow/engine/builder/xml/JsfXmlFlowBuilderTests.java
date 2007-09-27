package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.faces.webflow.JSFMockHelper;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.support.DefaultFlowServiceLocator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class JsfXmlFlowBuilderTests extends TestCase {

	private XmlFlowBuilder builder;
	private DefaultFlowServiceLocator serviceLocator;
	private JSFMockHelper jsf = new JSFMockHelper();

	protected void setUp() throws Exception {
		jsf.setUp();
		FlowDefinitionLocator subflowLocator = new FlowDefinitionLocator() {
			public FlowDefinition getFlowDefinition(String flowId) throws NoSuchFlowDefinitionException,
					FlowDefinitionConstructionException {
				return new Flow(flowId);
			}
		};
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean("bean", new Object());
		FlowBuilderServices builderServices = new FlowBuilderServices();
		builderServices.setViewFactoryCreator(new JsfViewFactoryCreator());
		serviceLocator = new DefaultFlowServiceLocator(subflowLocator, builderServices);
	}

	public final void testBuildJsfFlow() {
		ClassPathResource resource = new ClassPathResource("jsf-flow.xml", getClass());
		builder = new XmlFlowBuilder(resource, serviceLocator);
		FlowAssembler assembler = new FlowAssembler("jsf-flow", builder, null);
		Flow flow = assembler.assembleFlow();
		assertEquals("jsf-flow", flow.getId());
		assertEquals("viewState1", flow.getStartState().getId());
	}
}
