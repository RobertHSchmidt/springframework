/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.definition.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;

/**
 * <pre>
 *     &lt;bean id=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.registry.FlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;registrars&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;bean class=&quot;example.MyFlowRegistrar&quot;/&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author Keith Donald
 */
public class FlowDefinitionRegistryFactoryBean extends AbstractFlowDefinitionRegistryFactoryBean {

	/**
	 * The flow registrars that will perform the definition registrations.
	 */
	private List registrars;

	/**
	 * Sets the list of flow registrars to contain only the single flow
	 * registrar provided. Convenience setter for when registry population is
	 * driven by a single registrar.
	 * @param flowRegistrar the flow registrar
	 */
	public void setRegistrar(FlowDefinitionRegistrar registrar) {
		Assert.notNull(registrar, "The flow definition registrar is required");
		registrars = Collections.singletonList(registrar);
	}

	/**
	 * Sets the list of flow registrars that will register flow definitions.
	 * @param registrars the flow registrars
	 */
	public void setRegistrars(FlowDefinitionRegistrar[] registrars) {
		Assert.notNull(registrars, "The flow definition registrars is required");
		this.registrars = Arrays.asList(registrars);
	}

	protected void doPopulate(FlowDefinitionRegistry registry) {
		if (registrars != null) {
			Iterator it = registrars.iterator();
			while (it.hasNext()) {
				FlowDefinitionRegistrar registrar = (FlowDefinitionRegistrar)it.next();
				registrar.registerFlowDefinitions(registry);
			}
		}
	}
}