package org.springframework.webflow.samples.sellitem;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.action.AbstractAction;

public class SellItemAction extends AbstractAction {

    protected Event doExecute(RequestContext context) throws Exception {
        Scope request = context.getRequestScope();
        
        Sale sale = (Sale) request.get("sale");
        
        Scope flowScope = context.getFlowScope();
        //flowScope.put()
        // TODO Auto-generated method stub
        return null;
    }

}
