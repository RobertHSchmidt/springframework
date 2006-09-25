package org.springframework.webflow.samples.sellitem;

import org.easymock.MockControl;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.test.MockFlowServiceLocator;
import org.springframework.webflow.test.MockParameterMap;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

public class SellItemFlowExecutionTests extends AbstractXmlFlowExecutionTests {

	private String flowDir = "src/main/webapp/WEB-INF/flows";

	private MockControl saleProcessorControl;

	private SaleProcessor saleProcessor;

	@Override
	protected FlowDefinitionResource getFlowDefinitionResource() {
		return createFlowDefinitionResource(flowDir, "sellitem-flow.xml");
	}

	public void testStartFlow() {
		ApplicationView selectedView = applicationView(startFlow());
		assertModelAttributeNotNull("sale", selectedView);
		assertViewNameEquals("priceAndItemCountForm", selectedView);
	}

	public void testSubmitPriceAndItemCount() {
		testStartFlow();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("itemCount", "4");
		parameters.put("price", "25");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("categoryForm", selectedView);
	}

	public void testSubmitCategoryForm() {
		testSubmitPriceAndItemCount();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("category", "A");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();
	}

	public void testSubmitCategoryFormWithShipping() {
		testSubmitPriceAndItemCount();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("category", "A");
		parameters.put("shipping", "true");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("shippingDetailsForm", selectedView);
	}

	public void testSubmitShippingDetailsForm() {
		testSubmitCategoryFormWithShipping();

		saleProcessor.process((Sale)getRequiredFlowAttribute("sale", Sale.class));
		saleProcessorControl.replay();

		MockParameterMap parameters = new MockParameterMap();
		parameters.put("shippingType", "E");
		parameters.put("shipDate", "12/06/2006");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();

		saleProcessorControl.verify();
	}

	@Override
	protected void registerMockServices(MockFlowServiceLocator serviceRegistry) {
		saleProcessorControl = MockControl.createControl(SaleProcessor.class);
		saleProcessor = (SaleProcessor)saleProcessorControl.getMock();
		serviceRegistry.registerBean("saleProcessor", saleProcessor);

		// we'll use real shipping flow
		FlowDefinitionResource shipping = createFlowDefinitionResource(flowDir, "shipping-flow.xml");
		serviceRegistry.registerSubflow(createFlow(shipping, serviceRegistry));
	}
}