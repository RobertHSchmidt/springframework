package org.springframework.webflow.samples.sellitem;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.action.AbstractAction;

public class SellItemAction extends AbstractAction {

	// this does nothing. We're just showing how an action can do something if it wants to
	protected Event doExecute(RequestContext context) throws Exception {
        Scope flow = context.getFlowScope();
        
        Sale sale = (Sale) flow.get("sale");
        sale.getAmount();
        
        return success();
    }

}
