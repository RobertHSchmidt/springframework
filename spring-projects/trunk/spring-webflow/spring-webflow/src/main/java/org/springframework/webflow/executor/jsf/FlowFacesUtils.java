package org.springframework.webflow.executor.jsf;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepository;

/**
 * Trivial helper utility class for SWF within a JSF environment.
 * @author Keith Donald
 */
public class FlowFacesUtils {

	private static final String REPOSITORY_BEAN_NAME = "flowExecutionRepository";

	private static final String LOCATOR_BEAN_NAME = "flowDefinitionLocator";

	private static final String FACTORY_BEAN_NAME = "flowExecutionFactory";

	private static DefaultFlowExecutionRepository defaultRepository;

	private static FlowExecutionImplFactory defaultFactory;

	public static FlowDefinitionLocator getDefinitionLocator(FacesContext context) {
		ApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(context);
		try {
			return (FlowDefinitionLocator)ac.getBean(LOCATOR_BEAN_NAME, FlowDefinitionLocator.class);
		}
		catch (NoSuchBeanDefinitionException e) {
			String message = "No bean definition with id '" + LOCATOR_BEAN_NAME
					+ "' could be found; to use Spring Web Flow with JSF you must "
					+ "configure your context with a FlowDefinitionLocator "
					+ "exposing a registry of flow definitions.";
			throw new JsfFlowConfigurationException(message, e);
		}
	}

	public synchronized static FlowExecutionRepository getExecutionRepository(FacesContext context) {
		ApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(context);
		if (ac.containsBean(REPOSITORY_BEAN_NAME)) {
			return (FlowExecutionRepository)ac.getBean(REPOSITORY_BEAN_NAME, FlowExecutionRepository.class);
		}
		else {
			if (defaultRepository == null) {
				defaultRepository = new DefaultFlowExecutionRepository(new FlowExecutionImplStateRestorer(
						getDefinitionLocator(context)));
			}
			return defaultRepository;
		}
	}

	public synchronized static FlowExecutionFactory getExecutionFactory(FacesContext context) {
		ApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(context);
		if (ac.containsBean(FACTORY_BEAN_NAME)) {
			return (FlowExecutionFactory)ac.getBean(FACTORY_BEAN_NAME, FlowExecutionFactory.class);
		}
		else {
			if (defaultFactory == null) {
				defaultFactory = new FlowExecutionImplFactory();
			}
			return defaultFactory;
		}
	}
}