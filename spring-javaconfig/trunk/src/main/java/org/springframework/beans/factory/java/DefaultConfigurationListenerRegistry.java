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

package org.springframework.beans.factory.java;

import java.util.LinkedList;
import java.util.List;

/**
 * Default ConfigurationListenerRegistry implementation.
 * @author Rod Johnson
 */
public class DefaultConfigurationListenerRegistry implements ConfigurationListenerRegistry {
	
	private List<ConfigurationListener> configurationListeners = new LinkedList<ConfigurationListener>();
	
	public DefaultConfigurationListenerRegistry() {
		registerConfigurationListener(new AutoBeanConfigurationListener());
		registerConfigurationListener(new HotSwapConfigurationListener());
		registerConfigurationListener(new SpringAdvisorConfigurationListener());
		registerConfigurationListener(new SpringAdviceConfigurationListener());
		registerConfigurationListener(new AspectJAdviceConfigurationListener());
		registerConfigurationListener(new ConversationScopedConfigurationListener());
	}


	public void registerConfigurationListener(ConfigurationListener listener) {
		this.configurationListeners.add(listener);
	}

	public List<ConfigurationListener> getConfigurationListeners() {
		return configurationListeners;
	}

}
