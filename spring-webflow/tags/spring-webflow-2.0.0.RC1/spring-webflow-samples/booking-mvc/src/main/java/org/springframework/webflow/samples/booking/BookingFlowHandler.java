package org.springframework.webflow.samples.booking;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

public class BookingFlowHandler extends AbstractFlowHandler {
    public String handleExecutionOutcome(String outcome, AttributeMap output, HttpServletRequest request,
	    HttpServletResponse response) {
	return "hotels/index";
    }

    @Override
    public String handleException(FlowException e, HttpServletRequest request, HttpServletResponse response) {
	if (e instanceof NoSuchFlowExecutionException) {
	    return "hotels/index";
	} else {
	    throw e;
	}
    }

}
