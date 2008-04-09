package issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.config.java.MalformedJavaConfigurationException;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * need to test:
 * 
 *  [*] ExternalValue as constructor-param
 * 
 *  [ ] ExternalValue as Bean method-param
 *
 *  [ ] ExternalValue directly on field?
 * 
 * 
 * prove that this works in conjunction with:
 * 
 * 	[ ] base package scanning
 * 
 * 	[*] class literal
 * 
 * 	[ ] passing Import annotation
 * 
 * 	[ ] JavaConfigWebApplicationContext
 * 
 *  [ ] XML (ConfigurationPostProcessor)
 * 
 * 
 * [ ] What happens when there's more than one constructor? (it's an ambiguity for
 *     SJC and thus an error) Actually: should probably look for one that has all
 *     ExternalValue-annotated params
 * 
 * [ ] (see above) account for the fact that a Configuration class might legitimately
 *     have multiple constructors in the case of being bootstrapped from XML.  this
 *     is an edge case to say the least, but nevertheless.  Also, in the case of XML
 *     bootstrapping, the ExternalValue params may (will?) be supplied by XML.
 *     Create some tests around this.
 * 
 * [ ] What happens when I supply a body for an ExternalMethod? (should return that
 *     value as a default if there's no property value found (this is a diff issue)
 * 
 * [ ] Consider swapping default MessageSourceValueSource for PropertiesValueSource
 *     (is locale really necessary?  Probably mostly confuses people)
 * 
 * [ ] Bogus ResourceBundles path throws an exception (doesn't currently)
 *     What happens when the ResourceBundles path(s) don't resolve?  Perhaps
 *     this can be improved if we go the PropertiesValueSource route? (see above)
 * 
 * [ ] TODO: create new feature issue regarding supporting custom registration of
 *     PropertyEditors (equiv. of CustomPropetyEditorConfigurer in XML)
 * 
 * [ ] What happens when I override an ExternalBean method in a subclass? What if I
 *     don't annotate it? (should work anyway, as it's Inherited)
 *     TODO: this is really a different issue, right?
 * 
 * [*] What happens when the ExternalValue is not a String? (should trigger looking
 *     for a proper PropertyEditor)
 * 
 * [*] What happens when I subclass a Configuration that's already annotated with
 *     ResourceBundles? (should be inherited? what if I 'add' one in the subclass?)
 * 
 * [*] What happens if I forget to supply ResourceBundles?
 * 
 * [*] What happens when constructor args are not annotatated? (it should be an error)
 * 
 * [*] What happens when a constructor is private? (error - bad for CGLIB)
 * 
 * @author Chris Beams
 */
public class Sjc74Tests {

	private JavaConfigApplicationContext ctx;

	public @Before void initContext() {
		ctx = new JavaConfigApplicationContext();
	}


	// ----------------------------------------------------
	// Happy path - a well-formed configuration with a single constructor
	// ----------------------------------------------------
	public @Test void wellFormed() {
		new JavaConfigApplicationContext(SingleConstructorConfig.class);
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
	// What happens if a constructor is private?
	// (not strictly related to SJC-74)
	// ----------------------------------------------------
	@Test(expected=BeanCreationException.class)
	public void privateConstructor() {
		ctx.addConfigClass(PrivateConstructor.class);
		ctx.refresh();
	}


	static class PrivateConstructor {
		private final String username;
		private PrivateConstructor(@ExternalValue String username) { this.username = username; }
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

		public @Bean SimpleDataSource dataSource() {
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

