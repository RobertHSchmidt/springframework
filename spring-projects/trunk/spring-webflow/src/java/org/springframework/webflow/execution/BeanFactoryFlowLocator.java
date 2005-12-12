package org.springframework.webflow.execution;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;

/**
 * A simple flow locator that uses a bean factory as a registry for locating
 * flow definitions. Acts as an adapter that adapts the BeanFactory interface to
 * the FlowLocator interface. Is BeanFactoryAware, so will automatically be
 * injected a reference to the containing <code>BeanFactory</code> when
 * managed by Spring.
 * <p>
 * This is an alternative to using an explicit
 * {@link org.springframework.webflow.registry.FlowRegistry} to store Flow
 * definitions. A FlowRegistry is a FlowLocator.
 * 
 * Usage example:
 * 
 * <pre>
 *     &lt;!--
 *       Exposes flows for execution at a single request URL.
 *       The id of a flow to launch should be passed in by clients using
 *     	 the &quot;_flowId&quot; request parameter:
 *           e.g. /app.htm?_flowId=flow1
 *     --&gt;
 *     &lt;bean name=&quot;/app.htm&quot; class=&quot;org.springframework.webflow.mvc.FlowController&quot;&gt;
 *         &lt;constructor-arg&gt;
 *             &lt;bean class=&quot;org.springframework.webflow.execution.BeanFactoryFlowLocator&quot;/&gt;
 *         &lt;/constructor-arg&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class BeanFactoryFlowLocator implements FlowLocator, BeanFactoryAware {

	/**
	 * The bean factory to locate flow definitions in.
	 */
	private BeanFactory beanFactory;

	public Flow getFlow(String id) throws FlowArtifactException {
		try {
			return (Flow)beanFactory.getBean(id, Flow.class);
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new FlowArtifactException(Flow.class, id, "Could not locate flow in beanfactory with id '" + id
					+ "' - check your bean factory configuration", e);
		}
		catch (BeanNotOfRequiredTypeException e) {
			throw new FlowArtifactException(Flow.class, id, "A bean was found in the factory with id '" + id
					+ "' but it was not a Flow definition: check your bean factory configuration", e);
		}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}