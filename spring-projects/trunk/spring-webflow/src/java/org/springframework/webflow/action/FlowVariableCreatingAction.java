package org.springframework.webflow.action;

import java.util.Iterator;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.FlowVariable;

/**
 * A action that creates one or more variables in flow scope when executed.
 * @author Keith Donald
 */
public class FlowVariableCreatingAction extends AbstractAction {

	/**
	 * The variables to create
	 */
	private Set variables = CollectionFactory.createLinkedSetIfPossible(3);

	/**
	 * Creates a new flow variable creating action.
	 */
	public FlowVariableCreatingAction() {

	}

	/**
	 * Creates a new flow variable creating action.
	 * @param variable the var
	 */
	public FlowVariableCreatingAction(FlowVariable variable) {
		addVariable(variable);
	}

	/**
	 * Creates a new flow variable creating action.
	 * @param variables the vars
	 */
	public FlowVariableCreatingAction(FlowVariable[] variables) {
		addVariables(variables);
	}

	/**
	 * Adds a flow variable.
	 * @param variable the var
	 */
	public void addVariable(FlowVariable variable) {
		variables.add(variable);
	}

	/**
	 * Adds the flow variables.
	 * @param variables the vars
	 */
	public void addVariables(FlowVariable[] variables) {
		if (variables == null) {
			return;
		}
		for (int i = 0; i < variables.length; i++) {
			addVariable(variables[i]);
		}
	}

	/**
	 * Returns the flow variables.
	 */
	public FlowVariable[] getVariables() {
		return (FlowVariable[])variables.toArray(new FlowVariable[variables.size()]);
	}

	protected Event doExecute(RequestContext context) throws Exception {
		createFlowVariables(context);
		return success();
	}

	/**
	 * Creates the flow variables.
	 * @param context the request context
	 */
	protected void createFlowVariables(RequestContext context) {
		Iterator it = this.variables.iterator();
		while (it.hasNext()) {
			FlowVariable var = (FlowVariable)it.next();
			var.create(context);
		}
	}
}