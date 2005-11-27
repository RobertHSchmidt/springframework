/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.registry;

import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.Flow;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilder;

/**
 * Factory bean that acts as a director for assembling flows, delegating to a
 * <code>FlowBuilder</code> builder to construct the Flow. This is the core
 * top level class for assembling a <code>Flow</code> from configuration
 * information.
 * <p>
 * As an example, a Spring-managed FlowFactoryBean definition might look like
 * this:
 * 
 * <pre>
 *     &lt;bean id=&quot;user.RegistrationFlow&quot; class=&quot;org.springframework.webflow.registry.FlowFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowBuilder&quot;&gt;
 *             &lt;bean class=&quot;com.mycompany.myapp.webflow.user.UserRegistrationFlowBuilder&quot;/&gt;
 *          &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * The above definition is configured with a specific, Java-based FlowBuilder
 * implementation. An XmlFlowBuilder could instead be used, for example:
 * 
 * <pre>
 *    &lt;bean id=&quot;user.RegistrationFlow&quot; class=&quot;org.springframework.webflow.config.FlowFactoryBean&quot;&gt;
 *        &lt;property name=&quot;flowBuilder&quot;&gt;
 *            &lt;bean class=&quot;org.springframework.webflow.config.XmlFlowBuilder&quot;&gt;
 *                &lt;constructor-arg value=&quot;/WEB-INF/userRegistrationFlow.xml&quot;/&gt;
 *             &lt;/bean&gt;
 *        &lt;/property&gt;
 *    &lt;/bean&gt;
 * </pre>
 * 
 * In both cases the constructed flow will be assigned the id of the defining
 * FlowFactoryBean, e.g. "user.RegistrationFlow".
 * <p>
 * Flow factory beans, as POJOs, can also be used outside of a Spring bean
 * factory, in a standalone, programmatic fashion:
 * 
 * <pre>
 *     FlowBuilder builder = ...;
 *     Flow flow = new FlowFactoryBean(builder).getFlow();
 * </pre>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowFactoryBean implements FactoryBean, BeanNameAware, InitializingBean {

	/**
	 * Name of this bean in the defining bean factory. Will be used as the
	 * default flow id if no other flow id is specified.
	 */
	private String beanName;

	/**
	 * The id that will be assigned to the constructed flow. This is typically
	 * the id of of this bean in the defining bean factory.
	 */
	private String flowId;

	/**
	 * Additional properties to assign to the constructed flow.
	 */
	private Map flowProperties;

	/**
	 * Flow builder used by this flow factory bean.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * The flow assembler used by this flow factory bean as a helper to direct
	 * flow construction by the builder.
	 */
	private FlowHolder flowHolder;

	/**
	 * Create a new flow factory bean.
	 * @see #setFlowBuilder(FlowBuilder)
	 */
	public FlowFactoryBean() {
	}

	/**
	 * Create a new flow factory bean using the specified builder strategy.
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowFactoryBean(FlowBuilder flowBuilder) {
		this.flowBuilder = flowBuilder;
	}

	/**
	 * Returns the builder the factory uses to build flows.
	 */
	protected FlowBuilder getFlowBuilder() {
		return this.flowBuilder;
	}

	/**
	 * Set the builder the factory will use to build flows.
	 */
	public void setFlowBuilder(FlowBuilder flowBuilder) {
		this.flowBuilder = flowBuilder;
	}

	/**
	 * Returns the id that will be assigned to the constructed flow. By default
	 * the name of this bean in the defining bean factory is used.
	 */
	public String getFlowId() {
		return StringUtils.hasText(flowId) ? flowId : beanName;
	}

	/**
	 * Sets the id that will be assigned to the constructed flow.
	 */
	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	/**
	 * Sets the name of this bean in the defining bean factory. This name will
	 * be used as id of the constructed flow if no other id has been specified
	 * using the "flowId" property.
	 */
	public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Returns additional properties to be assigned to the constructed flow.
	 */
	public Map getFlowProperties() {
		return flowProperties;
	}

	/**
	 * Sets additional properties to be assigned to the constructed flow.
	 */
	public void setFlowProperties(Map flowProperties) {
		this.flowProperties = flowProperties;
	}

	public void afterPropertiesSet() {
		Assert.notNull(flowBuilder, "The flow builder is required to assemble the flow produced by this factory");
	}

	public Class getObjectType() {
		return Flow.class;
	}

	public boolean isSingleton() {
		// the holder could refresh the flow definition
		return false;
	}

	public Object getObject() throws Exception {
		return getFlowHolder().getFlow();
	}

	/**
	 * Returns the flow assembler used by this flow factory bean. The flow
	 * assembler is a helper that directs flow construction.
	 */
	public FlowHolder getFlowHolder() {
		if (flowHolder == null) {
			flowHolder = createFlowHolder();
		}
		return flowHolder;
	}

	protected FlowHolder createFlowHolder() {
		return new RefreshableFlowHolder(new FlowAssembler(getFlowId(), getFlowProperties(), getFlowBuilder()));
	}	
}