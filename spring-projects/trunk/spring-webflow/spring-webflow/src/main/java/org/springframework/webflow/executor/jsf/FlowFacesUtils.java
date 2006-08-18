package org.springframework.webflow.executor.jsf;

import javax.faces.context.FacesContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;

/**
 * Trivial helper utility class for SWF within a JSF environment.
 * @author Keith Donald
 */
public class FlowFacesUtils {

	private static final String REPOSITORY_BEAN_NAME = "flowExecutionRepository";

	private static final String FLOW_DEFINITION_LOCATOR_BEAN_NAME = "flowDefinitionLocator";

	private static final String FLOW_EXECUTION_FACTORY_BEAN_NAME = "flowExecutionFactory";

	public static FlowExecutionRepository getExecutionRepository(FacesContext context) {
		ApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(context);
		return (FlowExecutionRepository)ac.getBean(REPOSITORY_BEAN_NAME, FlowExecutionRepository.class);
	}

	public static FlowExecutionFactory getExecutionFactory(FacesContext context) {
		ApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(context);
		return (FlowExecutionFactory)ac.getBean(FLOW_EXECUTION_FACTORY_BEAN_NAME, FlowExecutionFactory.class);
	}

	public static FlowDefinitionLocator getDefinitionLocator(FacesContext context) {
		ApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(context);
		return (FlowDefinitionLocator)ac.getBean(FLOW_DEFINITION_LOCATOR_BEAN_NAME, FlowDefinitionLocator.class);
	}

}