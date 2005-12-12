package org.springframework.webflow.action;

import java.util.Iterator;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.FlowVariable;

/**
 * An action that creates one or more variables in flow scope when executed.  Typically 
 * used as part of flow startup action logic.
 * <p>
 * Sample usage:
 * 
 * <pre>
 *    FlowVariableCreatingAction action = new FlowVariableCreationAction();
 *    action.addVariable(new FlowVariable(&quot;reservation&quot;, Reservation.class);
 *    MockRequestContext context = new MockRequestContext();
 *    action.execute(context);
 * </pre>
 * 
 * @see FlowVariable
 * @see Flow#getStartActionList()
 * 
 * @author Keith Donald
 */
public class FlowVariableCreatingAction extends AbstractAction {

	/**
	 * The flow variables to create.
	 */
	private Set variables = CollectionFactory.createLinkedSetIfPossible(3);

	/**
	 * Creates a new flow variable creating action, intially with an empty
	 * variable set.
	 * @see #addVariable(FlowVariable)
	 */
	public FlowVariableCreatingAction() {

	}

	/**
	 * Creates a new flow variable creating action that creates a single
	 * variable.
	 * @param variable the variable
	 */
	public FlowVariableCreatingAction(FlowVariable variable) {
		addVariable(variable);
	}

	/**
	 * Creates a new flow variable creating action that creates the set of
	 * variables.
	 * @param variables the variables
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
		return (FlowVariable[])variables.toArray(new FlowVariable[0]);
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
		Iterator it = variables.iterator();
		while (it.hasNext()) {
			FlowVariable var = (FlowVariable)it.next();
			var.create(context);
		}
	}
	
	public String toString() {
		return new ToStringCreator(this).append("variables", getVariables()).toString();
	}
}