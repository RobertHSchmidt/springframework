/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.listener.registry.ConfigurationListenerRegistry;
import org.springframework.config.java.listener.registry.DefaultConfigurationListenerRegistry;
import org.springframework.config.java.process.ConfigurationProcessor;

/**
 * Tests for instance based configuration.
 * 
 * @author Costin Leau
 * 
 */
public class InstanceConfigurationTests extends TestCase {

	@Configuration
	private static class SomeConfig implements Serializable {

		private String string = SomeConfig.class.getName();

		public SomeConfig() {
		}

		public SomeConfig(String name) {
			this.string = name;
		}

		@Bean
		public String string() {
			return string;
		}
	}

	private ConfigurationListenerRegistry clr;

	private DefaultListableBeanFactory bf;

	private ConfigurationProcessor configurationProcessor;

	@Override
	protected void setUp() throws Exception {
		clr = new DefaultConfigurationListenerRegistry();
		bf = new DefaultListableBeanFactory();
		configurationProcessor = new ConfigurationProcessor(bf, clr);
	}

	@Override
	protected void tearDown() throws Exception {
		bf.destroySingletons();
		bf = null;
		clr = null;
		configurationProcessor = null;
	}

	public void testStaticConfiguration() {
		configurationProcessor.process(SomeConfig.class);
		assertTrue(bf.containsBean("string"));
		assertEquals(SomeConfig.class.getName(), bf.getBean("string"));
	}

	public void testInstanceConfiguration() {
		String name = "electric";
		// create an instance
		SomeConfig instance = new SomeConfig(name);
		configurationProcessor.process(instance);
		assertTrue(bf.containsBean("string"));
		assertEquals(name, bf.getBean("string"));
	}

	public void testSerializationForInstances() throws Exception {
		String name = "whereverImayRoam";
		SomeConfig instance = new SomeConfig(name);
		configurationProcessor.process(instance);
		assertTrue(bf.containsBean("string"));
		assertEquals(name, bf.getBean("string"));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new ObjectOutputStream(out).writeObject(instance);
		SomeConfig loadedConfig = (SomeConfig) new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();

		DefaultListableBeanFactory anotherBF = new DefaultListableBeanFactory();
		ConfigurationProcessor newConfigProcess = new ConfigurationProcessor(anotherBF, clr);
		newConfigProcess.process(loadedConfig);

		// repeat test
		assertTrue(bf.containsBean("string"));
		assertEquals(name, bf.getBean("string"));
	}
}
