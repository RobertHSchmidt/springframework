package org.springframework.config.java;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.support.ConfigurationSupport;

public class AutoBeanTests {
	@Test
	public void basicConstructorAutowiring() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(BasicConstructorAutowiring.class);
		Assert.assertNotNull(ctx.getBean("a"));
	}

	// see SJC-85
	@Ignore
	@Test
	public void constructorAutowiring() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(ConstructorAutowiring.class);
		Service service = ctx.getBean(Service.class);
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.repos);
	}
}

abstract class BasicConstructorAutowiring extends ConfigurationSupport {
	@AutoBean
	public abstract TestBean a();
}

abstract class ConstructorAutowiring extends ConfigurationSupport {
	@Bean
	public Service service() {
		Service service = new Service();
		service.setRepository(repos());
		return service;
	}

	@AutoBean
	public abstract Repository repos();
}

class Service {
	Repository repos;

	Service() {
	}

	void setRepository(Repository repos) {
		this.repos = repos;
	}
}

class Repository {

}