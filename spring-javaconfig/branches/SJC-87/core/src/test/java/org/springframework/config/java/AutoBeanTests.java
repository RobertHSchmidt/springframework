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
import org.junit.Test;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.process.MalformedJavaConfigurationException;

// TODO: rename as AutoBeanIntegrationTests
public class AutoBeanTests {

	// XXX: [@AutoBean]
	public @Test void basicConstructorAutowiring() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(BasicConstructorAutowiring.class);
		Assert.assertNotNull(ctx.getBean("a"));
	}
    static abstract class BasicConstructorAutowiring {
    	public abstract @AutoBean TestBean a();
    }


	// XXX: [@AutoBean]
	public @Test void constructorAutowiring() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(ConstructorAutowiring.class);
		Service service = ctx.getBean(Service.class);
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.repos);
	}
    static abstract class ConstructorAutowiring {
    	public @Bean Service service() {
    		Service service = new Service();
    		service.setRepository(repos());
    		return service;
    	}

    	public abstract @AutoBean Repository repos();
    }

	// XXX: [@AutoBean]
    @Test(expected = MalformedJavaConfigurationException.class)
    public void interfaceAutoBeanIsMalformed() {
    	new JavaConfigApplicationContext(InterfaceAutoBeanConfig.class);
    }
    static abstract class InterfaceAutoBeanConfig {
    	public abstract @AutoBean ITestBean alice();
    }
}


class Service {
	Repository repos;

	Service() { }

	void setRepository(Repository repos) { this.repos = repos; }
}
class Repository { }