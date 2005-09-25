package org.springframework.webflow.samples.sellitem;

import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.test.AbstractFlowExecutionTests;

public class SellItemFlowExecutionTests extends AbstractFlowExecutionTests {

	protected String flowId() {
		return "sellItem";
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/webflow/samples/sellitem/applicationContext.xml" };
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