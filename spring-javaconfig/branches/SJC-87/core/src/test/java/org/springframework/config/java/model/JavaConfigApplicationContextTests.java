package org.springframework.config.java.model;

import static org.springframework.util.ClassUtils.convertClassNameToResourcePath;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.ClassPathScanningConfigurationProviderFactory;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.TypeSafeBeanFactoryUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.ClassPathResource;


public class JavaConfigApplicationContextTests  {

	private XJavaConfigApplicationContext ctx;

	// --------------------------------------------
	public @Test void singleClass() {
		ctx = new XJavaConfigApplicationContext(SimpleConfig.class);

		TestBean alice = ctx.getBean(TestBean.class);

		Assert.assertNotNull(alice);
		Assert.assertEquals("alice", alice.getName());
		// prove singleton semantics are respected
		Assert.assertSame(alice, ctx.getBean(TestBean.class));
	}
	static class SimpleConfig { public @Bean TestBean alice() { return new TestBean("alice"); } }


	// --------------------------------------------

	/**
	 * re-implementing a simplified version of the context.  This will be swapped
	 * in for the existing JCAC when feature-complete.
	 */
	public static class XJavaConfigApplicationContext extends AbstractRefreshableApplicationContext
													 implements ConfigurableJavaConfigApplicationContext{

		// TODO: is the factory really necessary?
		private final ClassPathScanningCandidateComponentProvider scanner =
			new ClassPathScanningConfigurationProviderFactory().getProvider(this);

		// TODO: should be LinkedHashSet?
		private ArrayList<ClassPathResource> configClassResources = new ArrayList<ClassPathResource>();

		public XJavaConfigApplicationContext() { }

		public XJavaConfigApplicationContext(Class<?>... configClasses) {
			addConfigClasses(configClasses);
			refresh();
		}

		public XJavaConfigApplicationContext(String... basePackages) {
			addBasePackages(basePackages);
			refresh();
		}

		@Override
		protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
			BeanDefinitionReader reader = new ReflectingJavaConfigBeanDefinitionReader(beanFactory);
			for(ClassPathResource configClassResource : configClassResources)
				reader.loadBeanDefinitions(configClassResource);
		}

		public void addConfigClasses(Class<?>... classes) {
			for(Class<?> configClass : classes)
				addConfigClassAsResource(configClass.getName());
		}

		public void addBasePackages(String... basePackages) {
			for (String basePackage : basePackages)
				for(BeanDefinition beanDef : scanner.findCandidateComponents(basePackage))
					addConfigClassAsResource(beanDef.getBeanClassName());
		}

		private void addConfigClassAsResource(String fqClassName) {
			configClassResources.add(new ClassPathResource(convertClassNameToResourcePath(fqClassName)));
		}

		@Deprecated
		public void addConfigClass(Class<?> cls) { addConfigClasses(cls); }

		@Deprecated
		public void setBasePackages(String... basePackages) { addBasePackages(basePackages); }

		@Deprecated
		public void setConfigClasses(Class<?>... classes) { addConfigClasses(classes); }

		public <T> T getBean(Class<T> type) {
			return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type);
		}

		public <T> T getBean(Class<T> type, String beanName) {
			return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type);
		}

	}

}
