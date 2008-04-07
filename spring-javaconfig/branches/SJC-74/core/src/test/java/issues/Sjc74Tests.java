package issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.config.java.MalformedJavaConfigurationException;
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
 *  ExternalValue as constructor-param
 * 
 *  ExternalValue as Bean method-param
 *
 *  ExternalValue directly on field?
 * 
 * 
 * prove that this works in conjunction with:
 * 
 * 	base package scanning
 * 
 * 	class literal
 * 
 * 	passing Import annotation
 * 
 * 	JavaConfigWebApplicationContext
 * 
 * 
 * Bagus ResourceBundles path throws an exception (doesn't currently)
 * 
 * What happens when constructor args are not annotatated? (it should be an
 * error)
 * 
 * What happens when a constructor is private? (error - bad for CGLIB)
 * 
 * What happens when a constructor is private? (error - bad for CGLIB)
 * 
 * What happens when there's more than one constructor? (it's an ambiguity for
 * SJC and thus an error)
 * 
 * What happens when I override an ExternalBean method in a subclass? What if I
 * don't annotate it? (should work anyway, as it's Inherited)
 * 
 * What happens when I supply a body for an ExternalMethod? (should return that
 * value as a default if there's no property value found
 * 
 * What happens if I forget to supply ResourceBundles?
 * 
 * What happens when the ResourceBundles path(s) don't resolve?
 * 
 * What happens when the ExternalValue is not a String? (should trigger looking
 * for a proper PropertyEditor)
 * 
 * What happens when I subclass a Configuration that's already annotated with
 * ResourceBundles? (should be inherited? what if I 'add' one in the subclass?)
 * 
 * TODO: decide what exception to throw when configuration class is malformed
 * (i.e.: multiple constructors exist, non-annotated constructor params, etc)
 * 
 * TODO: account for the fact that a Configuration class might legitimately have
 * multiple constructors in the case of being bootstrapped from XML.  this is an
 * edge case to say the least, but nevertheless.  Also, in the case of XML
 * bootstrapping, the ExternalValue params may (will?) be supplied by XML.  Create
 * some tests around this.
 * 
 * @author Chris Beams
 */
public class Sjc74Tests {

	private JavaConfigApplicationContext ctx;

	public @Before void initContext() {
		ctx = new JavaConfigApplicationContext();
	}


	// ----------------------------------------------------
	// What happens if I forget to supply @ResourceBundles?
	// ----------------------------------------------------
	@Test(expected=MalformedJavaConfigurationException.class)
	public void omitResourceBundles() {
		ctx.addConfigClass(ResourceBundlesOmitted.class);
		ctx.refresh();
	}

	static class ResourceBundlesOmitted {
		private final String username;
		public ResourceBundlesOmitted(@ExternalValue String username) { this.username = username; }
		public @Bean String username() { return username; }
	}


	// ----------------------------------------------------
	// Happy path - a well-formed configuration with a single constructor
	// ----------------------------------------------------
	public @Test void oneConstructorAllowed() {
		new JavaConfigApplicationContext(SingleConstructorConfig.class);
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class SingleConstructorConfig {
		public SingleConstructorConfig(@ExternalValue("database.url") String url) {
		}
	}


	// ----------------------------------------------------
	// What happens if constructor params are not annotated?
	// ----------------------------------------------------
	@Test(expected=MalformedJavaConfigurationException.class)
	public void omitConstructorParamAnnotations() {
		ctx.addConfigClass(ConstructorParamAnnotationsOmitted.class);
		ctx.refresh();
	}

	static class ConstructorParamAnnotationsOmitted {
		public ConstructorParamAnnotationsOmitted(String foo) { }
		public @Bean String username() { return "foo"; }
	}


	// ----------------------------------------------------
	// What happens if additional parameter annotations exist?
	// ----------------------------------------------------
	public @Test void additionalParamAnnotations() {
		ctx.addConfigClass(AdditionalParamAnnotations.class);
		ctx.refresh();
		assertEquals("alice", ctx.getBean(String.class));
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class AdditionalParamAnnotations {
		private final String username;
		public AdditionalParamAnnotations(@Other @ExternalValue String usernameParam) {
			this.username = usernameParam;
		}
		public @Bean String username() { return username; }
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target( ElementType.PARAMETER)
	static @interface Other { }


	// ----------------------------------------------------
	// What happens if @ExternalValue is used as a constructor
	// param without any value (should use param name)
	// ----------------------------------------------------
	public @Test void externalValueConstructorParameterAnnotationWithDefaultValueUsesParameterName() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(
				ConstructorParameterAnnotationWithDefaultValue.class);
		String username = ctx.getBean(String.class);
		assertEquals("alice", username);
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class ConstructorParameterAnnotationWithDefaultValue {
		private final String username;

		// 'usernameParam' is defined in Sjc74Tests.properties
		public ConstructorParameterAnnotationWithDefaultValue(@ExternalValue String usernameParam) {
			this.username = usernameParam;
		}

		public @Bean String username() { return username; }
	}

	// ----------------------------------------------------
	// What happens if one config imports another that declares a constructor?
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

		public @Bean
		SimpleDataSource dataSource() {
			return new SimpleDataSource(url, "username", "password");
		}
	}

	// ----------------------------------------------------
	// What happens if multiple constructor arguments (3) are passed?
	// ----------------------------------------------------
	public @Test void threeConstructorArgs() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(OrderServiceConfig.class);
		OrderService orderService = ctx.getBean(OrderService.class);
		assertNotNull(orderService);

		SimpleDataSource ds = ctx.getBean(SimpleDataSource.class);
		assertEquals("jdbc:url", ds.url);
		assertEquals("scott", ds.username);
		assertEquals("tiger", ds.password);
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class OrderServiceConfig {
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

	static interface OrderService { }

	static interface OrderRepository { }

	static class OrderServiceImpl implements OrderService {
		private final OrderRepository orderRepos;
		public OrderServiceImpl(OrderRepository orderRepos) { this.orderRepos = orderRepos; }
	}

	static class JdbcOrderRepository implements OrderRepository {
		private final IDataSource dataSource;
		public JdbcOrderRepository(IDataSource dataSource) { this.dataSource = dataSource; }
	}


	// ----------------------------------------------------
	// What happens if multiple constructor arguments (2) are passed?
	// ----------------------------------------------------
	public @Test void testTwoConstructorArgs() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(OrderServiceConfig2.class);
		SimpleDataSource ds = ctx.getBean(SimpleDataSource.class);
		assertEquals("jdbc:url", ds.url);
		assertEquals("scott", ds.username);
		assertEquals("bogus", ds.password);
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class OrderServiceConfig2 {
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


	// ----------------------------------------------------
	// What happens if multiple constructors are specified?
	// ----------------------------------------------------
	@Test(expected = MalformedJavaConfigurationException.class)
	public void multipleConstructorsNotAllowed() {
		ctx.addConfigClass(MultiConstructorConfig.class);
		ctx.refresh();
	}

	@Configuration
	static class MultiConstructorConfig {
		public MultiConstructorConfig(String one, String two) { }
		public MultiConstructorConfig(String one, String two, String three) { }
		public @Bean String foo() { return "foo"; }
	}


	// ----------------------------------------------------
	// What happens if a superclass declares @ResourceBundles?
	// ----------------------------------------------------
	public @Test void parentDeclaresResourceBundles() {
		ctx.addConfigClass(ChildConfig.class);
		ctx.refresh();
		assertEquals("scott", ctx.getBean("username"));
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class BaseConfig { }

	static class ChildConfig extends BaseConfig {
		private final String username;
		public ChildConfig(@ExternalValue("database.username") String username) { this.username = username; }
		public @Bean String username() { return username; }
	}


	// ----------------------------------------------------
	// What happens if the supplied basename(s) do not resolve?
	// ----------------------------------------------------
	// TODO: this currently cannot be determined


	// ----------------------------------------------------
	// What happens if @ResourceBundles declares no basenames?
	// ----------------------------------------------------
	@Test(expected=MalformedJavaConfigurationException.class)
	public void emptyResourceBundles() {
		ctx.addConfigClass(EmptyResourceBundles.class);
		ctx.refresh();
	}

	@ResourceBundles({})
	static class EmptyResourceBundles {
		public EmptyResourceBundles(@ExternalValue String foo) { }
		public @Bean String foo() { return "foo"; }
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
		if (url == null)
			throw new IllegalStateException("url was not set");
		if (username == null)
			throw new IllegalStateException("username was not set");
		if (password == null)
			throw new IllegalStateException("password was not set");
	}
}
