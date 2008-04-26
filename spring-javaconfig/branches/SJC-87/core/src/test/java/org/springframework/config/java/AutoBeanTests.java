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
package org.springframework.config.java;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.context.LegacyJavaConfigApplicationContext;
import org.springframework.config.java.support.ConfigurationSupport;

public class AutoBeanTests {
	// TODO: [@AutoBean]
	public @Test void basicConstructorAutowiring() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(BasicConstructorAutowiring.class);
		Assert.assertNotNull(ctx.getBean("a"));
	}

	@Ignore // see SJC-85
	public @Test void constructorAutowiring() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(ConstructorAutowiring.class);
		Service service = ctx.getBean(Service.class);
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.repos);
	}
}

abstract class BasicConstructorAutowiring extends ConfigurationSupport {
	public abstract @AutoBean TestBean a();
}

abstract class ConstructorAutowiring extends ConfigurationSupport {
	public @Bean Service service() {
		Service service = new Service();
		service.setRepository(repos());
		return service;
	}

	public abstract @AutoBean Repository repos();
}

class Service {
	Repository repos;

	Service() { }

	void setRepository(Repository repos) { this.repos = repos; }
}

class Repository { }