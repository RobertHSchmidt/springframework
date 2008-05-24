package issues;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Type;
import org.springframework.asm.commons.EmptyVisitor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.context.DefaultJavaConfigBeanFactory;
import org.springframework.config.java.context.JavaConfigBeanFactory;
import org.springframework.config.java.model.JavaConfigBeanDefinitionReader;
import org.springframework.config.java.model.ReflectiveJavaConfigBeanDefinitionReader;
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

	protected abstract JavaConfigBeanDefinitionReader getBeanDefinitionReader(JavaConfigBeanFactory registry, String configurationBeanName);

	public static class ReflectiveSjc87Tests extends Sjc87Tests {
		@Override
		protected JavaConfigBeanDefinitionReader getBeanDefinitionReader(JavaConfigBeanFactory registry, String configurationBeanName) {
			return new ReflectiveJavaConfigBeanDefinitionReader(registry, new ArrayList<ClassPathResource>());
		}
	}

	public @Test void populateAndRenderModel() {
		Resource classResource = new ClassPathResource(ClassUtils.convertClassNameToResourcePath(MyConfig.class.getName()));

		ConfigurableListableBeanFactory extBf = new DefaultListableBeanFactory();
		JavaConfigBeanFactory bf = new DefaultJavaConfigBeanFactory(extBf);
		RootBeanDefinition rbd = new RootBeanDefinition();
		String beanClassName = MyConfig.class.getName();
		rbd.setBeanClassName(beanClassName);
		((BeanDefinitionRegistry) bf).registerBeanDefinition(beanClassName, rbd);
		getBeanDefinitionReader(bf, beanClassName).loadBeanDefinitions(classResource);

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
