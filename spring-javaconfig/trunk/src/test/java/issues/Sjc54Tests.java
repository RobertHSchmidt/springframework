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
package issues;

import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SJC-54 came up because of an intentional break in backward compatibility from
 * 1.0M2 to 1.0M3. The issue was resolve as WONT FIX, so the test here is set up
 * to expect the exception described in the bug.
 * 
 * @author Volodymyr Zhabiuk
 * @author Chris Beams
 */
public class Sjc54Tests {

	/**
	 * We actually expect this to throw because the config class that defines
	 * the customerService bean is an abstract class. This is consistent with
	 * the changes made in SJC-54, see that issue for details.
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void repro() {
		ApplicationContext pac = new ClassPathXmlApplicationContext("issues/Sjc54.xml");
		JavaConfigApplicationContext applicationContext = new JavaConfigApplicationContext();
		applicationContext.setConfigClasses(ServicesBeanRepository.class, SystemBeanRepository.class);
		applicationContext.setParent(pac);
		applicationContext.refresh();
		applicationContext.getBean("customerService"); // throws
	}

	static interface CustomerService {

	}

	static class CustomerServiceJpaImpl implements CustomerService {

	}

	@Configuration
	static abstract class ServicesBeanRepository extends ConfigurationSupport {
		@Bean(autowire = Autowire.BY_TYPE)
		public CustomerService customerService() {
			CustomerService customerService = new CustomerServiceJpaImpl();
			return customerService;
		}
	}

	@Configuration
	static class SystemBeanRepository {

	}
}
