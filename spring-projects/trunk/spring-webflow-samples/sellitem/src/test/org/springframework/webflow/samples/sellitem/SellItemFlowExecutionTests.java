package org.springframework.webflow.samples.sellitem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.test.AbstractXmlFlowExecutionTests;

public class SellItemFlowExecutionTests extends AbstractXmlFlowExecutionTests {

	@Override
	protected String flowId() {
		return "sellitem";
	}
	
	@Override
	protected Resource getFlowLocation() {
		File flowDir = new File("src/webapp/WEB-INF");
		return new FileSystemResource(new File(flowDir, "sellitem.xml"));
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/webflow/samples/sellitem/applicationContext.xml" };
	}

	public void testStartFlow() {
		ViewSelection selectedView = startFlow();
		assertModelAttributeNotNull("sale", selectedView);
		assertViewNameEquals("priceAndItemCountForm", selectedView);
	}

	public void testSubmitPriceAndItemCount() {
		testStartFlow();
		Map parameters = new HashMap(2);
		parameters.put("itemCount", "4");
		parameters.put("price", "25");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("categoryForm", selectedView);
	}

	public void testSubmitCategoryForm() {
		testSubmitPriceAndItemCount();
		Map parameters = new HashMap(1);
		parameters.put("category", "A");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();
	}

	public void testSubmitCategoryFormWithShipping() {
		testSubmitPriceAndItemCount();
		Map parameters = new HashMap(1);
		parameters.put("category", "A");
		parameters.put("shipping", "true");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("shippingDetailsForm", selectedView);
	}

	public void testSubmitShippingDetailsForm() {
		testSubmitCategoryFormWithShipping();
		Map parameters = new HashMap(1);
		parameters.put("shippingType", "E");
		ViewSelection selectedView = signalEvent("submit", parameters);
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();
	}
}