package issues;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Type;
import org.springframework.asm.commons.EmptyVisitor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/*
 * TODO:
 * 
 * Using the format in MethodInvokingFactoryBean, register [Composite?]ComponentDefinitions with source
 * references that Spring IDE can use to get back to the original @Bean method
 * 
 * See:
 *  ComponentScanBeanDefinitionParser
 *  MethodInvokingFactoryBean
 *  ComponentDefinition
 *  CompositeComponentDefinition
 * 
 * First, simply create a ClassVisitor that can find @Bean-annotated methods.
 */
public abstract class Sjc87Tests {
	@Ignore
	@Test
	public void test() throws IOException {
		ClassReader classReader = new ClassReader("issues.Sjc87Tests$Config");
		classReader.accept(new EmptyVisitor() {

			@Override
			public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
				System.out.printf("visit anno: {arg0=%s,arg1=%s}\n", arg0, arg1);
				return super.visitAnnotation(arg0, arg1);
			}

			@Override
			public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
				System.out.printf("inner {arg0=%s,arg1=%s,arg2=%s,arg3=%d}\n", arg0, arg1, arg2, arg3);
				super.visitInnerClass(arg0, arg1, arg2, arg3);
			}

			@Override
			public void visitOuterClass(String arg0, String arg1, String arg2) {
				System.out.println("outer");
				super.visitOuterClass(arg0, arg1, arg2);
			}

			@Override
			public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
				System.out.println("begin");
				super.visit(arg0, arg1, arg2, arg3, arg4, arg5);
			}

			@Override
			public void visit(String arg0, Object arg1) {
				System.out.println("begin2");
				super.visit(arg0, arg1);
			}

			@Override
			public void visitEnd() {
				System.out.println("end");
				super.visitEnd();
			}

			@Override
			public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
				System.out.printf("visit param: {arg0=%d,arg1=%s,arg2=%s}\n", arg0, arg1, arg2);
				return super.visitParameterAnnotation(arg0, arg1, arg2);
			}

			@Override
			public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3, String[] arg4) {
				System.out.printf("visit method: {arg0=%d,arg1=%s,arg2=%s,arg3=%s}\n", arg0, arg1, arg2, arg3);
				return super.visitMethod(arg0, arg1, arg2, arg3, arg4);
			}
		}, true);

		/*
		MetadataReader metadataReader = new SimpleMetadataReaderFactory().getMetadataReader(Config.class.getName());
		AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
		Set<String> annotationTypes = annotationMetadata.getAnnotationTypes();
		for(String s : annotationTypes)
			System.out.println(s);
		 */
	}

	@Ignore public @Test void createSingleBeanDefForConfigurationClass() throws IOException {

		BeanFactory bf = new DefaultListableBeanFactory();

		ClassReader classReader = new ClassReader("issues.Sjc87Tests$Config");
		classReader.accept(new ConfigurationVisitor(bf), true);

		//assertEquals("issues.Sjc87Tests$Config", rbd.getBeanClassName());

		Config config = (Config) bf.getBean("abc123");

		assertNotNull(config);
	}

	protected abstract JavaConfigBeanDefinitionReader getBeanDefinitionReader(BeanDefinitionRegistry registry, String configurationBeanName);

	public static class ReflectiveSjc87Tests extends Sjc87Tests {
		@Override
		protected JavaConfigBeanDefinitionReader getBeanDefinitionReader(BeanDefinitionRegistry registry, String configurationBeanName) {
			return new ReflectiveJavaConfigBeanDefinitionReader(registry, configurationBeanName);
		}
	}

	public @Test void populateAndRenderModel() {

		Resource classResource = new ClassPathResource(ClassUtils.convertClassNameToResourcePath(MyConfig.class.getName()));

		BeanFactory bf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition();
		String beanClassName = MyConfig.class.getName();
		rbd.setBeanClassName(beanClassName);
		((BeanDefinitionRegistry) bf).registerBeanDefinition(beanClassName, rbd);
		getBeanDefinitionReader((BeanDefinitionRegistry) bf, beanClassName).loadBeanDefinitions(classResource);

		// ensure that the configuration class was registered as a bean
		MyConfig config = (MyConfig) bf.getBean(MyConfig.class.getName());
		assertNotNull(config);

		// ensure that the configuration class's username() bean method was registered
		String username = (String) bf.getBean("username");
		assertNotNull(username);
		assertEquals("alice", username);

		// ensure than singleton beans are actually singletons
		String username2 = (String) bf.getBean("username");
		assertSame(username, username2);
	}


	@Configuration
	public static class Config {
		/*
		public Config(@ExternalValue String url) {

		}
		 */

		public @Bean(scope="whatever", autowire=Autowire.BY_TYPE) IDataSource dataSource(@ExternalValue("ev") String url) { return null; }

		/*
		public @Bean IDataSource dataSource(
				@ExternalValue String url,
				@ExternalValue String username,
				@ExternalValue String password) {
			return new SimpleDataSource(url, username, password);
		}
		 */

		public static class Inner { }
	}
}

/**
 * Populates a given {@link BeanDefinitionRegistry} based on metadata within a
 * given {@link JavaConfigurationModel}
 *
 * @author Chris Beams
 */
class JavaConfigurationModelBeanDefinitionGenerator {

	/**
	 * @param registry
	 * @param model
	 * @return number of bean definitions generated
	 */
	public int generateBeanDefinitionsFromModel(BeanDefinitionRegistry registry, JavaConfigurationModel model) {

		int initialBeanDefCount = registry.getBeanDefinitionNames().length;

		for(ConfigurationClass configClass : model.getConfigurationClasses()) {
			String configClassName = configClass.getClassName();
			RootBeanDefinition configBeanDef = new RootBeanDefinition();
			configBeanDef.setBeanClassName(configClassName);
			// @Configuration classes' bean names are always their fully-qualified classname
			registry.registerBeanDefinition(configClassName, configBeanDef);

			for(BeanMethod beanMethod : configClass.getBeanMethods()) {
				RootBeanDefinition beanDef = new RootBeanDefinition();
				beanDef.setFactoryBeanName(configClassName);
				beanDef.setFactoryMethodName(beanMethod.getMethodName());
				// TODO: plug in NamingStrategy here
				registry.registerBeanDefinition(beanMethod.getMethodName(), beanDef);
			}
		}

		return registry.getBeanDefinitionNames().length - initialBeanDefCount;
	}

}

/**
 * An abstract representation of a set of user-provided "Configuration classes",
 * usually but not necessarily annotated with {@link Configuration @Configuration}.
 * The model is populated with a
 * {@link org.springframework.config.java.process.ConfigurationProcessor} implementation,
 * which may be reflection-based or ASM-based.  Once a model has been populated, it
 * can then be rendered out to a set of BeanDefinitions.  The model provides an important
 * layer of indirection between the complexity of parsing a set of classes and the complexity
 * of representing the contents of those classes as BeanDefinitions.
 *
 * @author Chris Beams
 */
class JavaConfigurationModel {
	private Set<ConfigurationClass> configurationClasses = new LinkedHashSet<ConfigurationClass>();

	/**
	 * Add a {@link Configuration @Configuration} class to the model.  Classes
	 * may be added at will and without any particular validation.  Malformed
	 * classes will be caught and errors processed during a later phase.
	 * 
	 * @param configurationClass user-supplied Configuration class
	 */
	public void addConfigurationClass(ConfigurationClass configurationClass) {
		configurationClasses.add(configurationClass);
	}

	public ConfigurationClass[] getConfigurationClasses() {
		return configurationClasses.toArray(new ConfigurationClass[] {});
	}
}

/**
 * Abstract representation of a user-definied {@link Configuration @Configuration}
 * class.  Includes a set of Bean methods, AutoBean methods, ExternalBean methods,
 * ExternalValue methods, etc.  Includes all such methods defined in the ancestry of
 * the class, in a 'flattened-out' manner.  Note that each BeanMethod representation
 * does still contain source information about where it was originally detected (for
 * the purpose of tooling with Spring IDE).
 * @author cbeams
 *
 */
class ConfigurationClass {
	private String className;
	private Set<BeanMethod> beanMethods = new LinkedHashSet<BeanMethod>();
	private ConfigurationClass importedBy;
	private Set<ResourceBundles> resourceBundles = new LinkedHashSet<ResourceBundles>();

	/**
	 * bean methods may be locally declared within this class, or discovered
	 * in a superclass.  The contract for processing overlapping bean methods
	 * is a last-in-wins model, so it is important that any configuration
	 * class processor is careful to add bean methods in a superclass-first,
	 * top-down fashion.
	 */
	public void addBeanMethod(BeanMethod beanMethod) {
		beanMethods.add(beanMethod);
	}

	public BeanMethod[] getBeanMethods() {
		return beanMethods.toArray(new BeanMethod[] { });
	}

	/**
	 * ResourceBundles may be locally declared on on this class, or discovered
	 * in a superclass.  The contract for processing multiple ResourceBundles
	 * annotations will be to combine them all into a single list of basenames
	 * with duplicates eliminated. This list will be ordered according to the
	 * order in which the ResourceBundles were added.  Therefore it is important
	 * that any configuration class processor is careful to add ResourceBundles
	 * in a superclass-first, top-down fashion.  In this way, the most concrete
	 * class will have precedence and thus be able to 'override' superclass behavior
	 */
	public void addResourceBundle(ResourceBundles resourceBundle) {
		resourceBundles.add(resourceBundle);
	}

	/** fully-qualified classname for this Configuration class */
	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	/** must declare at least one Bean method, etc */
	public boolean isWellFormed() {
		return true;
	}
}

class BeanMethod {
	private Bean beanAnnotation;
	private final String methodName;

	public BeanMethod(String methodName) {
		this.methodName = methodName;
	}

	public Bean getBeanAnnotation() {
		return beanAnnotation;
	}

	public String getMethodName() {
		return methodName;
	}

}

@Configuration class MyConfig {
	public @Bean String username() {
		return "alice";
	}
}


/**
 * These methods are candidates for inclusion in the core ClassUtils class
 *
 * @author Chris Beams
 */
class Util {

	/**
	 * @param java bytecode type descriptor
	 * @return fully qualified, dot-delimited class name
	 */
	public static String convertTypeDescriptorToClassName(String typeDesc) {
		return ClassUtils.convertResourcePathToClassName(Type.getType(typeDesc).getInternalName());
	}

}
