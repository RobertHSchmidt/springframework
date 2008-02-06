/*
 * Copyright 2002-2008 the original author or authors.
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

import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.valuesource.MessageSourceValueSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;

/**
 * Configuration listener that registers autowired bean definitions in the
 * factory for classes configured with the {@link ResourceBundles} annotation
 * 
 * @author Rod Johnson
 */
class ResourceBundlesConfigurationListener extends ConfigurationListenerSupport {

	@Override
	public boolean understands(Class<?> configurerClass) {
		return configurerClass.isAnnotationPresent(ResourceBundles.class);
	}

	public void handleEvent(Reactor reactor, ClassEvent event) {
		Class<?> configurationClass = event.clazz;
		ResourceBundles rbs = configurationClass.getAnnotation(ResourceBundles.class);
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setResourceLoader(((ConfigurationProcessor) reactor).getResourceLoader());
		ms.setBasenames(rbs.value());
		log.info(String.format("Adding resource bundles [%s] defined in class", StringUtils
				.arrayToCommaDelimitedString(rbs.value()), configurationClass.getName()));
		((ConfigurationProcessor) reactor).addValueSource(new MessageSourceValueSource(ms));
		// TODO how do we know the properties were found?
	}
}
