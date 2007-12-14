package org.springframework.config.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

public class ParameterizedBeanMethodTests {

	@Test
	public void testSimpleParameterizedBeanCreation() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(SimpleConfig.class);
		TestBean testBean = context.getBean(TestBean.class);
		assertThat(testBean.getName(), equalTo("foo"));
	}

	@Test
	public void testReferenceArchitectureParameterizedBeanCreation() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(AppConfig.class, InfraConfig.class);
		OrderService orderService = context.getBean(OrderService.class);
		assertThat(orderService.orderRepository, notNullValue());
	}

	/**
	 * Extremely simple example of parameterized bean definition methods.
	 */
	@Configuration
	static class SimpleConfig {
		@Bean
		public TestBean testBean(String name) {
			return new TestBean(name);
		}

		@Bean
		public String name() {
			return "foo";
		}
	}

	/**
	 * The more common case: modularized configuration where one or more bean
	 * definitions depends on beans that are not defined locally. In this case,
	 * DataSource is going to need to be provided by some other source of bean
	 * definitions; could be another JavaConfig Configuration class, could be
	 * coming from XML
	 */
	@Configuration
	static class AppConfig {
		@Bean
		public OrderService orderService(OrderRepository orderRepository) {
			return new OrderService(orderRepository);
		}

		@Bean
		public OrderRepository orderRepository(DataSource dataSource) {
			return new JdbcOrderRepository(dataSource);
		}

	}

	@Configuration
	static class InfraConfig {
		@Bean
		public DataSource dataSource() {
			return new DataSource();
		}
	}

	static class OrderService {
		private final OrderRepository orderRepository;

		OrderService(OrderRepository orderRepository) {
			this.orderRepository = orderRepository;
		}
	}

	static interface OrderRepository {
	}

	static class JdbcOrderRepository implements OrderRepository {
		private final DataSource dataSource;

		JdbcOrderRepository(DataSource dataSource) {
			this.dataSource = dataSource;
		}
	}

	// represents the notion of a javax.jdbc.DataSource without actually having
	// to deal with creating one
	static class DataSource {

	}

}
