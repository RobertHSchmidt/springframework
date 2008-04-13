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
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.process.MalformedJavaConfigurationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SJC-74 introduces support for annotating Configuration class constructor
 * params and Bean method params with ExternalValue.
 * 
 * @author Chris Beams
 */
public abstract class Sjc74Tests {

	public static class Sjc74JavaConfigApplicationContextTests extends Sjc74Tests {
		@Override
		protected ConfigurableJavaConfigApplicationContext newContext() {
			return new org.springframework.config.java.context.JavaConfigApplicationContext();
		}
	}

	public static class Sjc74JavaConfigWebApplicationContextTests extends Sjc74Tests {
		@Override
		protected ConfigurableJavaConfigApplicationContext newContext() {
			return new org.springframework.config.java.context.JavaConfigWebApplicationContext();
		}
	}

	private ConfigurableJavaConfigApplicationContext ctx;

	public @Before void initContext() {
		ctx = newContext();
	}

	protected abstract ConfigurableJavaConfigApplicationContext newContext();


	// ----------------------------------------------------
	// Happy path - a well-formed configuration with a single constructor
	// ----------------------------------------------------
	public @Test void wellFormed() {
		ctx.addConfigClass(SingleConstructorConfig.class);
		ctx.refresh();
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class SingleConstructorConfig {
		public SingleConstructorConfig(@ExternalValue("database.url") String url) {
		}
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
	// What happens if constructor params are non-String types?
	// TODO: should also be tested for method- and field- level
	// ----------------------------------------------------
	@Test
	public void nonStringExternalValue() {
		ctx.addConfigClass(NonStringExternalValue.class);
		ctx.refresh();
		assertEquals(100, ctx.getBean(Cache.class).size);
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class NonStringExternalValue {
		private final int cacheSize;
		public NonStringExternalValue(@ExternalValue int cacheSize) { this.cacheSize = cacheSize; }
		public @Bean Cache cache() { return new Cache(cacheSize); }
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
		ctx.addConfigClass(ConstructorParameterAnnotationWithDefaultValue.class);
		ctx.refresh();
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
		ctx.addConfigClass(ImporterConfig.class);
		ctx.refresh();
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
	// What happens if multiple constructor arguments (3) are passed?
	// ----------------------------------------------------
	public @Test void threeConstructorArgs() {
		ctx.addConfigClass(OrderServiceConfig.class);
		ctx.refresh();
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
		ctx.addConfigClass(OrderServiceConfig2.class);
		ctx.refresh();
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
	@Test
	public void multipleConstructorsWithNoArg() {
		ctx.addConfigClass(MultiConstructorWithNoArg.class);
		ctx.refresh();
	}

	// this is a well-formed configuration because it exposes a no-arg constructor
	@Configuration
	static class MultiConstructorWithNoArg {
		public MultiConstructorWithNoArg() { }
		public MultiConstructorWithNoArg(String one, String two) { }
		public MultiConstructorWithNoArg(String one, String two, String three) { }
		public @Bean String foo() { return "foo"; }
	}

	@Test(expected = MalformedJavaConfigurationException.class)
	public void multipleConstructorsButNoCandidates() {
		ctx.addConfigClass(MultiConstructorWithoutExternalValueOrNoArg.class);
		ctx.refresh();
	}

	// this is a malformed configuration because it does not expose at least one
	// constructor that either a) is no-arg or b) has all @ExternalValue parameters
	@Configuration
	static class MultiConstructorWithoutExternalValueOrNoArg {
		public MultiConstructorWithoutExternalValueOrNoArg(String one, String two) { }
		public MultiConstructorWithoutExternalValueOrNoArg(String one, String two, String three) { }
		public @Bean String foo() { return "foo"; }
	}

	@Test
	public void multipleConstructorsWithSingleButAmbiguousCandidate() {
		ctx.addConfigClass(MultiConstructorWithSingleButAmbiguousCandidate.class);
		ctx.refresh();
		assertEquals("CustomCacheName", ctx.getBean("cacheName"));
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class MultiConstructorWithSingleButAmbiguousCandidate {
		private String cacheName;

		// the first two constructors are type-ambiguous at runtime and the second
		// will be mis-set with ExternalValues intended for the first if explicit
		// type information is not provided by ConfigurationProcessor
		public MultiConstructorWithSingleButAmbiguousCandidate(@ExternalValue int cacheSize, @ExternalValue String cacheName) { this.cacheName = cacheName; }
		public MultiConstructorWithSingleButAmbiguousCandidate(String one, String two) { }
		public MultiConstructorWithSingleButAmbiguousCandidate(String one, String two, String three) { }
		public @Bean String cacheName() { return cacheName; }
	}

	@Test
	public void multipleConstructorsWithSingleCandidate() {
		ctx.addConfigClass(MultiConstructorWithSingleCandidate.class);
		ctx.refresh();
		assertEquals("CustomCacheName", ctx.getBean("cacheName"));
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class MultiConstructorWithSingleCandidate {
		private String cacheName;
		public MultiConstructorWithSingleCandidate(@ExternalValue int cacheSize, @ExternalValue String cacheName) { this.cacheName = cacheName; }
		public MultiConstructorWithSingleCandidate(String one, String two, String three) { }
		public @Bean String cacheName() { return cacheName; }
	}


	@Test(expected=MalformedJavaConfigurationException.class)
	public void multipleConstructorsWithMultipleCandidate() {
		ctx.addConfigClass(MultiConstructorWithMultipleCandidates.class);
		ctx.refresh();
		assertEquals("CustomCacheName", ctx.getBean("cacheName"));
	}

	// configuration is malformed, because it would be ambiguous to JavaConfig
	// which constructor to call (they both request ExternalValue parameters)
	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class MultiConstructorWithMultipleCandidates {
		private String cacheName;
		public MultiConstructorWithMultipleCandidates(@ExternalValue String cacheName) { this.cacheName = cacheName; }
		public MultiConstructorWithMultipleCandidates(@ExternalValue String one, @ExternalValue String two) { }
		public @Bean String cacheName() { return cacheName; }
	}

	@Test(expected=MalformedJavaConfigurationException.class)
	public void multipleConstructorsWithPartialCandidate() {
		ctx.addConfigClass(MultiConstructorWithPartialCandidate.class);
		ctx.refresh();
		assertEquals("CustomCacheName", ctx.getBean("cacheName"));
	}

	@Configuration
	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class MultiConstructorWithPartialCandidate {
		private String cacheName;
		public MultiConstructorWithPartialCandidate(@ExternalValue String cacheName, String two) { this.cacheName = cacheName; }
		public @Bean String cacheName() { return cacheName; }
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


	// ----------------------------------------------------
	// What happens in XML bootstrapping cases?
	// ----------------------------------------------------
	@Test
	public void bootstrapFromXml() {
		ApplicationContext ctx =
			new ClassPathXmlApplicationContext("Sjc74Tests.xml", getClass());
		Cache cache = (Cache) ctx.getBean("cache");
		assertEquals(1000, cache.size);
		assertEquals("DefaultCacheName", cache.name);
	}

	@Test
	public void bootstrapFromJava() {
		ctx.addConfigClass(BootstrapThisConfigFromXml.class);
		ctx.refresh();
		assertEquals(100, ctx.getBean(Cache.class).size);
		assertEquals("CustomCacheName", ctx.getBean(Cache.class).name);
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class BootstrapThisConfigFromXml {
		private final int cacheSize;
		private String cacheName = "DefaultCacheName";
		public BootstrapThisConfigFromXml(int cacheSize) { this.cacheSize = cacheSize; }
		public BootstrapThisConfigFromXml(@ExternalValue int cacheSize, @ExternalValue String cacheName) {
			this(cacheSize);
			this.cacheName = cacheName;
		}
		public @Bean Cache cache() { return new Cache(cacheSize, cacheName); }
	}

	// ----------------------------------------------------
	// Works in conjunction with base package scanning?
	// ----------------------------------------------------
	public @Test void wellFormedWithBasePackageScanning() {
		ctx.setBasePackages(issues.sjc74.scanning.WellFormedConfig.class.getPackage().getName());
		ctx.refresh();
		assertEquals("jdbc:url", ctx.getBean("url"));
	}


	// ====================================================
	// @ExternalValue as bean-method param
	// ====================================================

	// ----------------------------------------------------
	// Well-formed
	// ----------------------------------------------------
	public @Test void externalValueBeanMethodParam() {
		ctx.addConfigClass(ExternalValueBeanMethodParam.class);
		ctx.refresh();
		assertEquals("jdbc:url", ctx.getBean("url"));
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class ExternalValueBeanMethodParam {
		public @Bean String url(@ExternalValue("database.url") String url) {
			return url;
		}
	}

	// ----------------------------------------------------
	// Malformed: omit @ExternalValue from parameter
	// ----------------------------------------------------
	@Test(expected=IllegalArgumentException.class)
	public void externalValueAnnotationOmitted() {
		ctx.addConfigClass(ExternalValueAnnotationOmitted.class);
		ctx.refresh();
		assertEquals("jdbc:url", ctx.getBean("url"));
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class ExternalValueAnnotationOmitted {
		public @Bean String url(String url) {
			return url;
		}
	}

	// ----------------------------------------------------
	// Malformed: omit one @ExternalValue from just one parameter
	// ----------------------------------------------------
	@Test(expected=IllegalArgumentException.class)
	public void externalValueAnnotationOmittedOnJustOneParam() {
		ctx.addConfigClass(ExternalValueAnnotationOmittedOnJustOneParam.class);
		ctx.refresh();
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class ExternalValueAnnotationOmittedOnJustOneParam {
		public @Bean String url(@ExternalValue("database.url") String url, String bogus) {
			return url;
		}
	}

	// ----------------------------------------------------
	// Well Formed: omit value from @ExternalValue (should default to parameter name)
	// ----------------------------------------------------
	@Test
	public void externalValueAnnotationPresentButValueOmitted() {
		ctx.addConfigClass(ExternalValuePresentButValueOmitted.class);
		ctx.refresh();
		assertEquals("alice", ctx.getBean("name"));
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class ExternalValuePresentButValueOmitted {
		public @Bean String name(@ExternalValue String usernameParam) {
			return usernameParam;
		}
	}

	// ----------------------------------------------------
	// Well Formed: multiple @ExternalValue parameters (all defaulting to param names)
	// ----------------------------------------------------
	@Test
	public void multipleExternalValueParameters() {
		ctx.addConfigClass(MultipleExternalValueParameters.class);
		ctx.refresh();
		assertEquals("alice", ctx.getBean(Object[].class)[0]);
		assertEquals(100, ctx.getBean(Object[].class)[1]);
	}

	@ResourceBundles("classpath:issues/Sjc74Tests")
	static class MultipleExternalValueParameters {
		public @Bean Object[] params(@ExternalValue String usernameParam, @ExternalValue int cacheSize) {
			// just return a tuple of the injected values for easy testing
			return new Object[] { usernameParam, cacheSize };
		}
	}



}


// --- types used in multiple tests above ---------

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

class Cache {
	final int size;
	String name;
	public Cache(int size) { this.size = size; }
	public Cache(int size, String name) { this(size); this.name = name; }
}

