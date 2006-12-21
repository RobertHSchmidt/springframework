package org.springframework.webflow.test;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.webflow.Flow;
import org.springframework.webflow.registry.FlowRegistryImpl;
import org.springframework.webflow.registry.RegistryBackedFlowArtifactFactory;
import org.springframework.webflow.registry.StaticFlowHolder;

/**
 * A stub flow artifact factory implementation suitable for a test environment.
 * <p>
 * Allows programmatic registration of subflows needed by a flow execution being
 * tested, see {@link #registerSubflow(Flow)}.
 * <p>
 * Also supports programmatic registration of additional custom artifacts needed
 * by a flow (such as Actions) managed in a backing Spring {@link ConfigurableBeanFactory};
 * see {@link #registerBean(String, Object)}.  Beans registered are typically mocks or 
 * stubs of business services invoked by the flow.
 * 
 * @author Keith Donald
 */
public class MockFlowArtifactFactory extends RegistryBackedFlowArtifactFactory {

	/**
	 * Creates a new mock flow artifact factory.
	 */
	public MockFlowArtifactFactory() {
		super(new FlowRegistryImpl(), new StaticListableBeanFactory());
	}

	/**
	 * Register a subflow definition in this factory; typically to support a
	 * flow execution test.
	 * @param subflow the subflow
	 */
	public void registerSubflow(Flow subflow) {
		getSubflowRegistry().registerFlow(new StaticFlowHolder(subflow));
	}

	/**
	 * Register a bean in this factory; typically to support a flow execution
	 * test. If this bean is a service object used as an Action, it is often a
	 * stub or dynamic mock.
	 * @param beanName the bean name
	 * @param bean the singleton instance
	 */
	public void registerBean(String beanName, Object bean) {
		((StaticListableBeanFactory)getBeanFactory()).addBean(beanName, bean);
	}
}