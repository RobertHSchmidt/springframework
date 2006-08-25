package org.springframework.webflow.samples.sellitem;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class SellItemAction extends AbstractAction {

	// this does nothing. We're just showing how in the JSF integration, while
	// an action is not needed for binding, you can still add in an action to do
	// something if you need to
	protected Event doExecute(RequestContext context) throws Exception {
		Sale sale = (Sale)context.getFlowScope().getRequired("sale", Sale.class);
		sale.getAmount();
		return success();
	}

}