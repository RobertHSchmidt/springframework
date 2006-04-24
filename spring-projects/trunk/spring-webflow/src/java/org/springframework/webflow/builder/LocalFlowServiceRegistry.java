package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.webflow.Flow;

/**
 * Simple value object that holds a reference to a local artifact registry
 * of a flow definition that is in the process of being constructed.
 * @author Keith Donald
 */
class LocalFlowServiceRegistry {

	/**
	 * The locations of the registry resource definitions. 
	 */
	private Resource[] resources;

	/**
	 * The local registry holding the artifacts local to the flow.
	 */
	private GenericApplicationContext context;

	/**
	 * The flow for which this registry is for.
	 */
	private Flow flow;

	/**
	 * Create new registry
	 * @param context the local registry
	 */
	public LocalFlowServiceRegistry(Flow flow, Resource[] resources) {
		this.flow = flow;
		this.resources = resources;
	}

	public Resource[] getResources() {
		return resources;
	}

	public ApplicationContext getContext() {
		return context;
	}

	public Flow getFlow() {
		return flow;
	}

	public void init(LocalFlowServiceLocator localFactory, FlowServiceLocator rootFactory) {
		BeanFactory parent = null;
		if (localFactory.isEmpty()) {
			try {
				parent = rootFactory.getBeanFactory();
			}
			catch (UnsupportedOperationException e) {

			}
		}
		else {
			parent = localFactory.top().context;
		}
		context = createLocalFlowContext(parent, rootFactory);
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(resources);
		context.refresh();
	}

	private GenericApplicationContext createLocalFlowContext(BeanFactory parent, FlowServiceLocator rootFactory) {
		if (parent instanceof WebApplicationContext) {
			GenericWebApplicationContext context = new GenericWebApplicationContext();
			context.setServletContext(((WebApplicationContext)parent).getServletContext());
			context.setParent((WebApplicationContext)parent);
			context.setResourceLoader(rootFactory.getResourceLoader());
			return context;
		}
		else {
			GenericApplicationContext context = new GenericApplicationContext();
			if (parent instanceof ApplicationContext) {
				context.setParent((ApplicationContext)parent);
			}
			else {
				if (parent != null) {
					context.getBeanFactory().setParentBeanFactory(parent);
				}
			}
			context.setResourceLoader(rootFactory.getResourceLoader());
			return context;
		}
	}
}