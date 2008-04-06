package issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * 
 * need to test:
 * 
 * ExternalValue as constructor-param
 * ExternalValue as Bean method-param
 * ExternalValue directly on field?
 * 
 * prove that this works in conjunction with:
 * 
 * base package scanning
 * class literal passing
 * Import annotation
 * JavaConfigWebApplicationContext
 * 
 * 
 * ExternalValue with value and without (latter requires ASMification)
 * 
 * Bagus ResourceBundles path throws an exception (doesn't currently)
 * 
 * What happens when constructor args are not annotatated?  (it should be an error)
 * 
 * What happens when a constructor is private? (error - bad for CGLIB)
 * 
 * What happens when a constructor is private? (error - bad for CGLIB)
 * 
 * What happens when there's more than one constructor? (it's an ambiguity for SJC and thus an error)
 * 
 * What happens when I override an ExternalBean method in a subclass?  What if I don't annotate it?  (should work anyway, as it's Inherited)
 * 
 * What happens when I supply a body for an ExternalMethod? (should return that value as a default if there's no property value found
 * 
 * What happens when the ResourceBundles path(s) don't resolve?
 * 
 * What happens when the ExternalValue is not a String? (should trigger looking for a proper PropertyEditor)
 * 
 * What happens when I subclass a Configuration that's already annotated with ResourceBundles? (should be inherited?  what if I 'add' one in the subclass?)
 * 
 * @author Chris Beams
 */
public class Sjc74Tests {

	// ----------------------------------------------------

	@Ignore
	public @Test void externalValueConstructorParameterAnnotationWithDefaultValueUsesParameterName() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(ConstructorParameterAnnotationWithDefaultValue.class);
		String username = ctx.getBean(String.class);
		assertEquals("scott", username);
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class ConstructorParameterAnnotationWithDefaultValue {
		private final String username;
		public ConstructorParameterAnnotationWithDefaultValue(@ExternalValue String username) { this.username = username; }
		public @Bean String username() { return username; }
	}

	// ----------------------------------------------------

	public @Test void importClassWithExternalValueConstructorArgs() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(ImporterConfig.class);
		SimpleDataSource ds = ctx.getBean(SimpleDataSource.class);
		assertNotNull(ds);
		assertEquals("jdbc:url", ds.url);
	}

	@Configuration
	@Import(ImporteeConfig.class)
	static class ImporterConfig {
		public @Bean String foo() { return "foobean"; }
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class ImporteeConfig {
		private String url;

		public ImporteeConfig(@ExternalValue("database.url") String url) {
			this.url = url;
		}

		public @Bean SimpleDataSource dataSource() {
			return new SimpleDataSource(url, "username", "password");
		}
	}

	// ----------------------------------------------------

	@Test
	public void testThreeConstructorArgs() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(OrderServiceConfig.class);
		OrderService orderService = ctx.getBean(OrderService.class);
		assertNotNull(orderService);

		SimpleDataSource ds = ctx.getBean(SimpleDataSource.class);
		assertEquals("jdbc:url", ds.url);
		assertEquals("scott", ds.username);
		assertEquals("tiger", ds.password);
	}

	@Test
	public void testTwoConstructorArgs() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(OrderServiceConfig2.class);
		SimpleDataSource ds = ctx.getBean(SimpleDataSource.class);
		assertEquals("jdbc:url", ds.url);
		assertEquals("scott", ds.username);
		assertEquals("bogus", ds.password);
	}

	// ----------------------------------------------------

	public @Test void oneConstructorAllowed() {
		new JavaConfigApplicationContext(SingleConstructorConfig.class);
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class SingleConstructorConfig {
		public SingleConstructorConfig(@ExternalValue("database.url") String url) { }
	}

	// ----------------------------------------------------

	@Test(expected=IllegalArgumentException.class) // TODO: better exception
	public void multipleConstructorsNotAllowed() {
		new JavaConfigApplicationContext(MultiConstructorConfig.class);
	}
}

@Configuration class MultiConstructorConfig {
	public MultiConstructorConfig(String one, String two) { }
	public MultiConstructorConfig(String one, String two, String three) { }
}

@Configuration
@ResourceBundles("classpath:issues/Sjc74Tests")
class OrderServiceConfig2 {
	private String url;
	private String username;
	private String password;

	public OrderServiceConfig2(
			@ExternalValue("database.url") String url,
			@ExternalValue("database.username") String username) {
		this.url = url;
		this.username = username;
		this.password = "bogus";
	}

	public @Bean IDataSource dataSource() {
		return new SimpleDataSource(url, username, password);
	}
}

@Configuration
@ResourceBundles("classpath:issues/Sjc74Tests")
class OrderServiceConfig {
	private String url;
	private String username;
	private String password;

	public OrderServiceConfig(
			@ExternalValue("database.url") String url,
			@ExternalValue("database.username") String username,
			@ExternalValue("database.password") String password) {
		this.password = password;
		this.url = url;
		this.username = username;
	}

	public @Bean OrderService orderService() {
		return new OrderServiceImpl(orderRepository());
	}

	public @Bean OrderRepository orderRepository() {
		return new JdbcOrderRepository(dataSource());
	}

	public @Bean IDataSource dataSource() {
		return new SimpleDataSource(url, username, password);
	}
}

interface OrderService { }

class OrderServiceImpl implements OrderService {
	private final OrderRepository orderRepos;

	public OrderServiceImpl(OrderRepository orderRepos) {
		this.orderRepos = orderRepos;
	}
}

interface OrderRepository { }

class JdbcOrderRepository implements OrderRepository {
	private final IDataSource dataSource;

	public JdbcOrderRepository(IDataSource dataSource) {
		this.dataSource = dataSource;
	}
}

interface IDataSource { }

class SimpleDataSource implements IDataSource, InitializingBean {

	final String url;
	final String username;
	final String password;

	public SimpleDataSource(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public void afterPropertiesSet() throws Exception {
		if(url == null)
			throw new IllegalStateException("url was not set");
		if(username == null)
			throw new IllegalStateException("username was not set");
		if(password == null)
			throw new IllegalStateException("password was not set");
	}

}
