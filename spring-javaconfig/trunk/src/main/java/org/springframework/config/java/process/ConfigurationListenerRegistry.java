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

package org.springframework.config.java.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.config.java.util.DependencyUtils;

/**
 * Registers all internally-required {@link ConfigurationListener} instances.
 * Users can can extend this functionality by subclassing and implementing a
 * constructor that uses
 * {@link #registerConfigurationListener(ConfigurationListener)} to register any
 * custom listeners, and doing so will not adversely affect default registration
 * of internal listeners.
 * 
 * @author Chris Beams
 * @author Rod Johnson
 * 
 * @see ConfigurationProcessor#setConfigurationListenerRegistry(ConfigurationListenerRegistry)
 * @see ConfigurationPostProcessor#setConfigurationListenerRegistry(ConfigurationListenerRegistry)
 */
public class ConfigurationListenerRegistry {

	private final List<ConfigurationListener> configurationListeners = new ArrayList<ConfigurationListener>();

	public ConfigurationListenerRegistry() {
		registerConfigurationListener(new AutoBeanConfigurationListener());
		registerConfigurationListener(new ResourceBundlesConfigurationListener());
		registerConfigurationListener(new ScopedProxyConfigurationListener());
		if (DependencyUtils.isAopAvailable()) {
			registerConfigurationListener(new HotSwapConfigurationListener());
			registerConfigurationListener(new AspectJAdviceConfigurationListener());
		}

	}

	public final void registerConfigurationListener(ConfigurationListener listener) {
		this.configurationListeners.add(listener);
	}

	public final List<ConfigurationListener> getConfigurationListeners() {
		return Collections.unmodifiableList(configurationListeners);
	}

}
