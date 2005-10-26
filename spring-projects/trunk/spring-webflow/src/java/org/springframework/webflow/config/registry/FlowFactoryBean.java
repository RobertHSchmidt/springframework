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
package org.springframework.webflow.config.registry;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.FlowBuilder;

/**
 * A FlowAssembler that is also a Spring FactoryBean, for easy deployment within
 * a Spring BeanFactory.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowFactoryBean extends FlowAssembler implements FactoryBean {

	/**
	 * Creates a new flow factory bean.
	 * @param flowBuilder the flow builder to use to assemble this flow
	 */
	public FlowFactoryBean(FlowBuilder flowBuilder) {
		super(flowBuilder);
	}

	public Object getObject() throws Exception {
		return getFlow();
	}

	public Class getObjectType() {
		return Flow.class;
	}

	public boolean isSingleton() {
		return true;
	}
}