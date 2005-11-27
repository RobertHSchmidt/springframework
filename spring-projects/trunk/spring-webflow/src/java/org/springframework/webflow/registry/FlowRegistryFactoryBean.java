package org.springframework.webflow.registry;

import java.util.Iterator;
import java.util.List;

/**
 * A factory bean that produces a populated flow registry using a configured
 * list of {@link FlowRegistrar} objects.
 * <p>
 * This class is also <code>BeanFactoryAware</code> and when used with Spring
 * will automatically create a configured
 * {@link FlowRegistryFlowArtifactFactory} for loading Flow artifacts like
 * Actions from the Spring bean factory during the Flow registration process.
 * <p>
 * Usage example:
 * 
 * <pre>
 *     &lt;bean id=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.registry.FlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowRegistrars&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;bean class=&quot;example.MyFlowRegistrar&quot;/&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author Keith Donald
 */
public class FlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean {

	/**
	 * The flow registrars that will perform the definition registrations.
	 */
	private List flowRegistrars;

	/**
	 * Creates a xml flow registry factory bean.
	 */
	public FlowRegistryFactoryBean() {
	}

	/**
	 * Creates a xml flow registry factory bean, for programmatic usage only.
	 * @param beanFactory the bean factory to use for locating flow artifacts.
	 */
	public FlowRegistryFactoryBean(List flowRegistrars) {
		setFlowRegistrars(flowRegistrars);
	}

	/**
	 * Returns the list of flow registrars that will register flow definitions.
	 */
	public List getFlowRegistrars() {
		return flowRegistrars;
	}

	/**
	 * Sets the list of flow registrars that will register flow definitions.
	 * @param flowRegistrars the flow registrars
	 */
	public void setFlowRegistrars(List flowRegistrars) {
		this.flowRegistrars = flowRegistrars;
	}

	protected void doPopulate(FlowRegistry registry) {
		if (flowRegistrars != null) {
			Iterator it = flowRegistrars.iterator();
			while (it.hasNext()) {
				FlowRegistrar registrar = (FlowRegistrar)it.next();
				registrar.registerFlows(registry, getFlowArtifactFactory());
			}
		}
	}
}