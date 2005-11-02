package org.springframework.webflow.samples.sellitem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.test.AbstractFlowExecutionTests;

public class SellItemFlowExecutionTests extends AbstractFlowExecutionTests {

	protected String flowId() {
		return "sellitem";
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/webflow/samples/sellitem/applicationContext.xml" };
	}

	protected FlowLocator createFlowLocator() {
		XmlFlowRegistryFactoryBean factory = new XmlFlowRegistryFactoryBean(applicationContext);
		File parent = new File("src/webapp/WEB-INF");
		Resource[] locations = new Resource[] { new FileSystemResource(new File(parent, "sellItem.xml")) };
		factory.setDefinitionLocations(locations);
		return factory.populateFlowRegistry();
	}

	@Override
	protected void onSetupFlowExecution(FlowExecution flowExecution) {
		// turn off transactionality for test run
		getFlow().setProperty(Flow.TRANSACTIONAL_PROPERTY, false);
	}

	public void testStartFlow() {
		ViewSelection selectedView = startFlow();
		assertModelAttributeNotNull("sale", selectedView);
		assertViewNameEquals("priceAndItemCountForm", selectedView);
	}

	public void testSubmitPriceAndItemCount() {
		testStartFlow();
		Map parameters = new HashMap(2);
		parameters.put("itemCount", 4);
		parameters.put("price", 25);
		ViewSelection selectedView = signalEvent(event("submit", parameters));
		assertViewNameEquals("categoryForm", selectedView);
	}

	public void testSubmitCategoryForm() {
		testSubmitPriceAndItemCount();
		Map parameters = new HashMap(1);
		parameters.put("category", "A");
		ViewSelection selectedView = signalEvent(event("submit", parameters));
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();
	}

	public void testSubmitCategoryFormWithShipping() {
		testSubmitPriceAndItemCount();
		Map parameters = new HashMap(1);
		parameters.put("category", "A");
		parameters.put("shipping", "true");
		ViewSelection selectedView = signalEvent(event("submit", parameters));
		assertViewNameEquals("shippingDetailsForm", selectedView);
	}

	public void testSubmitShippingDetailsForm() {
		testSubmitCategoryFormWithShipping();
		Map parameters = new HashMap(1);
		parameters.put("shippingType", "E");
		ViewSelection selectedView = signalEvent(event("submit", parameters));
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();
	}
}