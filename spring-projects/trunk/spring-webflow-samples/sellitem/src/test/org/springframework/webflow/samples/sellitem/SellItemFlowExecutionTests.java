package org.springframework.webflow.samples.sellitem;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.config.BeanFactoryFlowArtifactLocator;
import org.springframework.webflow.config.FlowArtifactLocator;
import org.springframework.webflow.config.registry.FlowRegistryImpl;
import org.springframework.webflow.config.registry.XmlFlowRegistrar;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.test.AbstractFlowExecutionTests;

public class SellItemFlowExecutionTests extends AbstractFlowExecutionTests {

	protected String flowId() {
		return "sellItem";
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/webflow/samples/sellitem/applicationContext.xml" };
	}

	protected FlowLocator createFlowLocator() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		FlowArtifactLocator artifactLocator = new BeanFactoryFlowArtifactLocator(applicationContext);
		Resource[] locations = new Resource[] { new FileSystemResource("WEB-INF/sellItem-flow.xml") };
		XmlFlowRegistrar registrar = new XmlFlowRegistrar(artifactLocator, locations);
		registrar.registerFlowDefinitions(registry);
		return registry;
	}

	@Override
	protected void onSetupFlowExecution(FlowExecution flowExecution) {
		// turn off transactionality for test run
		getFlow().setProperty(Flow.TRANSACTIONAL_PROPERTY, false);
	}

	public void testStartFlow() {
		ViewDescriptor nextView = startFlow();
		assertViewNameEquals("priceAndItemCountForm", nextView);
	}

	public void testSubmitPriceAndItemCount() {
		testStartFlow();
		Map parameters = new HashMap(2);
		parameters.put("itemCount", 4);
		parameters.put("price", 25);
		ViewDescriptor nextView = signalEvent(event("submit", parameters));
		assertViewNameEquals("categoryForm", nextView);
	}

	public void testSubmitCategoryForm() {
		testSubmitPriceAndItemCount();
		Map parameters = new HashMap(1);
		parameters.put("category", "A");
		ViewDescriptor nextView = signalEvent(event("submit", parameters));
		assertViewNameEquals("costOverview", nextView);
		assertFlowExecutionEnded();
	}

	public void testSubmitCategoryFormWithShipping() {
		testSubmitPriceAndItemCount();
		Map parameters = new HashMap(1);
		parameters.put("category", "A");
		parameters.put("shipping", "true");
		ViewDescriptor nextView = signalEvent(event("submit", parameters));
		assertViewNameEquals("shippingDetailsForm", nextView);
	}

	public void testSubmitShippingDetailsForm() {
		testSubmitCategoryFormWithShipping();
		Map parameters = new HashMap(1);
		parameters.put("shippingType", "E");
		ViewDescriptor nextView = signalEvent(event("submit", parameters));
		assertViewNameEquals("costOverview", nextView);
		assertFlowExecutionEnded();
	}
}