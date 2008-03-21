package org.springframework.webflow.engine.model.builder.xml;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.engine.model.AbstractStateModel;
import org.springframework.webflow.engine.model.AttributeModel;
import org.springframework.webflow.engine.model.FlowModel;
import org.springframework.webflow.engine.model.SecuredModel;
import org.springframework.webflow.engine.model.TransitionModel;
import org.springframework.webflow.engine.model.VarModel;
import org.springframework.webflow.engine.model.ViewStateModel;
import org.springframework.webflow.engine.model.builder.FlowModelBuilder;
import org.springframework.webflow.engine.model.registry.DefaultFlowModelHolder;
import org.springframework.webflow.engine.model.registry.FlowModelConstructionException;
import org.springframework.webflow.engine.model.registry.FlowModelRegistry;
import org.springframework.webflow.engine.model.registry.FlowModelRegistryImpl;

public class XmlFlowModelBuilderTests extends TestCase {

	private FlowModelRegistry registry;

	protected void setUp() {
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean("bean", new Object());
		registry = new FlowModelRegistryImpl();
	}

	// public void testBuildIncompleteFlow() {
	// ClassPathResource resource = new ClassPathResource("flow-incomplete.xml", getClass());
	// builder = new XmlModelBuilder(resource);
	// try {
	// builder.parse();
	// fail("Should have failed");
	// } catch (FlowBuilderException e) {
	// }
	// }

	public void testBuildFlowWithEndState() {
		ClassPathResource resource = new ClassPathResource("flow-endstate.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertNull(flow.getStartStateId());
		assertEquals("end", ((AbstractStateModel) flow.getStates().get(0)).getId());
	}

	public void testBuildFlowWithDefaultStartState() {
		ClassPathResource resource = new ClassPathResource("flow-startstate-default.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertNull(flow.getStartStateId());
		assertEquals("end", ((AbstractStateModel) flow.getStates().get(0)).getId());
	}

	public void testBuildFlowWithStartStateAttribute() {
		ClassPathResource resource = new ClassPathResource("flow-startstate-attribute.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertEquals("end", flow.getStartStateId());
	}

	public void testCustomFlowAttribute() {
		ClassPathResource resource = new ClassPathResource("flow-custom-attribute.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertEquals("bar", ((AttributeModel) flow.getAttributes().get(0)).getValue());
		assertEquals("number", ((AttributeModel) flow.getAttributes().get(1)).getName());
	}

	public void testPersistenceContextFlow() {
		ClassPathResource resource = new ClassPathResource("flow-persistencecontext.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertNotNull(flow.getPersistenceContext());
	}

	// public void testFlowInputOutputMapping() {
	// ClassPathResource resource = new ClassPathResource("flow-inputoutput.xml", getClass());
	// FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
	// builder.init();
	// builder.build();
	// FlowModel flow = builder.getFlowModel();
	// FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
	// FlowExecution execution = factory.createFlowExecution(flow);
	// MockExternalContext context = new MockExternalContext();
	// MutableAttributeMap input = new LocalAttributeMap();
	// input.put("foo", "bar");
	// input.put("number", "3");
	// input.put("required", "9");
	// execution.start(input, context);
	// Event outcome = execution.getOutcome();
	// assertEquals("end", outcome.getId());
	// assertEquals("bar", outcome.getAttributes().get("foo"));
	// assertEquals("bar", outcome.getAttributes().get("differentName"));
	// assertEquals(new Integer(3), outcome.getAttributes().get("number"));
	// assertEquals(new Integer(3), outcome.getAttributes().get("required"));
	// assertEquals("a literal", outcome.getAttributes().get("literal"));
	// assertNull(outcome.getAttributes().get("notReached"));
	// }

	// public void testFlowRequiredInputMapping() {
	// ClassPathResource resource = new ClassPathResource("flow-inputoutput.xml", getClass());
	// FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
	// builder.init();
	// builder.build();
	// FlowModel flow = builder.getFlowModel();
	// FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
	// FlowExecution execution = factory.createFlowExecution(flow);
	// MockExternalContext context = new MockExternalContext();
	// MutableAttributeMap input = new LocalAttributeMap();
	// try {
	// execution.start(input, context);
	// fail("Should have failed");
	// } catch (FlowExecutionException e) {
	// RequiredMappingException me = (RequiredMappingException) e.getRootCause();
	// }
	// }

	// public void testFlowRequiredOutputMapping() {
	// ClassPathResource resource = new ClassPathResource("flow-inputoutput.xml", getClass());
	// FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
	// builder.init();
	// builder.build();
	// FlowModel flow = builder.getFlowModel();
	// FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
	// FlowExecution execution = factory.createFlowExecution(flow);
	// MockExternalContext context = new MockExternalContext();
	// MutableAttributeMap input = new LocalAttributeMap();
	// input.put("required", "yo");
	// try {
	// execution.start(input, context);
	// fail("Should have failed");
	// } catch (FlowExecutionException e) {
	// RequiredMappingException me = (RequiredMappingException) e.getRootCause();
	// }
	// }

	public void testFlowSecured() {
		ClassPathResource resource = new ClassPathResource("flow-secured.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		SecuredModel secured = flow.getSecured();
		assertNotNull(secured);
		assertEquals("ROLE_USER", secured.getAttributes());
	}

	public void testFlowSecuredState() {
		ClassPathResource resource = new ClassPathResource("flow-secured-state.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		SecuredModel secured = ((AbstractStateModel) flow.getStates().get(0)).getSecured();
		assertNotNull(secured);
		assertEquals("ROLE_USER", secured.getAttributes());
	}

	public void testFlowSecuredTransition() {
		ClassPathResource resource = new ClassPathResource("flow-secured-transition.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		SecuredModel secured = ((TransitionModel) flow.getGlobalTransitions().get(0)).getSecured();
		assertNotNull(secured);
		assertEquals("ROLE_USER", secured.getAttributes());
	}

	public void testFlowVariable() {
		ClassPathResource resource = new ClassPathResource("flow-var.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertEquals("flow-foo", ((VarModel) flow.getVars().get(0)).getName());
		assertEquals(null, ((VarModel) flow.getVars().get(0)).getScope());
		assertEquals("conversation-foo", ((VarModel) flow.getVars().get(1)).getName());
		assertEquals("conversation", ((VarModel) flow.getVars().get(1)).getScope());
	}

	public void testViewStateVariable() {
		ClassPathResource resource = new ClassPathResource("flow-viewstate-var.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertEquals("foo", ((VarModel) ((ViewStateModel) flow.getStates().get(0)).getVars().get(0)).getName());
	}

	public void testViewStateRedirect() {
		ClassPathResource resource = new ClassPathResource("flow-viewstate-redirect.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertEquals("true", ((ViewStateModel) flow.getStates().get(0)).getRedirect());
	}

	public void testViewStatePopup() {
		ClassPathResource resource = new ClassPathResource("flow-viewstate-popup.xml", getClass());
		FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
		builder.init();
		builder.build();
		FlowModel flow = builder.getFlowModel();
		assertEquals("true", ((ViewStateModel) flow.getStates().get(0)).getPopup());
	}

	// public void testViewStateFlowRedirect() {
	// ClassPathResource resource = new ClassPathResource("flow-viewstate-flowredirect.xml",
	// getClass());
	// FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
	// builder.init();
	// builder.build();
	// FlowModel flow = builder.getFlowModel();
	// ViewFactory vf = ((ViewState) flow.getStateInstance("view")).getViewFactory();
	// assertTrue(vf instanceof ActionExecutingViewFactory);
	// ActionExecutingViewFactory avf = (ActionExecutingViewFactory) vf;
	// assertTrue(avf.getAction() instanceof FlowDefinitionRedirectAction);
	// }

	// public void testViewStateExternalRedirect() {
	// ClassPathResource resource = new ClassPathResource("flow-viewstate-externalredirect.xml",
	// getClass());
	// FlowModelBuilder builder = new XmlFlowModelBuilder(resource, registry);
	// builder.init();
	// builder.build();
	// FlowModel flow = builder.getFlowModel();
	// ViewFactory vf = ((ViewState) flow.getStateInstance("view")).getViewFactory();
	// assertTrue(vf instanceof ActionExecutingViewFactory);
	// ActionExecutingViewFactory avf = (ActionExecutingViewFactory) vf;
	// assertTrue(avf.getAction() instanceof ExternalRedirectAction);
	// }

	public void testMerge() {
		ClassPathResource resourceChild = new ClassPathResource("flow-inheritance-child.xml", getClass());
		ClassPathResource resourceParent = new ClassPathResource("flow-inheritance-parent.xml", getClass());
		registry
				.registerFlowModel(new DefaultFlowModelHolder(new XmlFlowModelBuilder(resourceChild, registry), "child"));
		registry.registerFlowModel(new DefaultFlowModelHolder(new XmlFlowModelBuilder(resourceParent, registry),
				"parent"));
		FlowModel flow = registry.getFlowModel("child");
		assertEquals(1, flow.getGlobalTransitions().size());
		assertEquals(2, flow.getStates().size());
		assertEquals("view", ((AbstractStateModel) flow.getStates().get(0)).getId());
	}

	public void testMergeParentNotFound() {
		ClassPathResource resourceChild = new ClassPathResource("flow-inheritance-child.xml", getClass());
		ClassPathResource resourceParent = new ClassPathResource("flow-inheritance-parent.xml", getClass());
		registry
				.registerFlowModel(new DefaultFlowModelHolder(new XmlFlowModelBuilder(resourceChild, registry), "child"));
		registry.registerFlowModel(new DefaultFlowModelHolder(new XmlFlowModelBuilder(resourceParent, registry),
				"parent-id-not-matching"));
		try {
			registry.getFlowModel("child");
			fail("A FlowModelConstructionException was expected");
		} catch (FlowModelConstructionException e) {
			// we want this
		}
	}

}
