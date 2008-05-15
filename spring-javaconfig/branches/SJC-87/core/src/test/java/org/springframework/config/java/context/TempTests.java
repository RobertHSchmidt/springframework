package org.springframework.config.java.context;

import java.util.LinkedHashSet;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.process.NewConfigurationPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class TempTests {
	@Ignore
	public @Test void testXml() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("TempTests.xml", TempTests.class);
		TestBean foo = (TestBean) ctx.getBean("foo");
		TestBean bar = (TestBean) ctx.getBean("bar");
		Assert.assertEquals(bar, foo.getSpouse());
		Assert.assertSame(bar, foo.getSpouse());
	}

	public @Test void testJava() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(Config.class);
		TestBean foo = (TestBean) ctx.getBean("foo");
		TestBean bar = (TestBean) ctx.getBean("bar");
		Assert.assertEquals(bar, foo.getSpouse());
		Assert.assertSame(bar, foo.getSpouse());
	}

	public @Test void testCPXACLogging() {
		new ClassPathXmlApplicationContext().refresh();
	}

	public @Test void testSJCLogging() {
		new JavaConfigApplicationContext(Config.class);
	}

	public @Test void foo() {
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("TempTests2.xml", TempTests.class);
	}


	@Configuration
	public static class Config {
		public @Bean TestBean foo() { return new TestBean(bar()); }
		public @Bean TestBean bar() { return new TestBean("bar"); }
	}

	public @Test void tryBPP() {
		GenericApplicationContext ac = new GenericApplicationContext();
		//InternalBeanFactoryCreator bfpp = new InternalBeanFactoryCreator();
		//bfpp.setApplicationContext(ac);
		//ac.addBeanFactoryPostProcessor(bfpp);
		ac.refresh();
	}

	public static class InternalBeanFactoryCreator  {

		private final AbstractApplicationContext externalContext;

		private DefaultListableBeanFactory internalBeanFactory;

		public InternalBeanFactoryCreator(AbstractApplicationContext externalContext) {
			this.externalContext = externalContext;
		}

		public DefaultListableBeanFactory createInternalBeanFactory() {
			if(internalBeanFactory != null)
				return internalBeanFactory;

			internalBeanFactory = new DefaultListableBeanFactory() {
				@Override
				public boolean isCurrentlyInCreation(String beanName) {
					if(super.isCurrentlyInCreation(beanName))
						return true;

					if(this.getParentBeanFactory() != null)
						return this.getParentBeanFactory().isCurrentlyInCreation(beanName);

					return super.isCurrentlyInCreation(beanName);
				}

				@Override
				public String[] getBeanDefinitionNames() {
					LinkedHashSet<String> names = new LinkedHashSet<String>();

					for(String name : super.getBeanDefinitionNames())
						names.add(name);

					if(this.getParentBeanFactory() != null)
    					for(String name : this.getParentBeanFactory().getBeanDefinitionNames())
    						names.add(name);

					return names.toArray(new String[names.size()]);
				}

				@Override
				public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
					if(containsLocalBean(beanName))
						return super.getBeanDefinition(beanName);

					if(this.getParentBeanFactory() != null)
						return this.getParentBeanFactory().getBeanDefinition(beanName);

					return super.getBeanDefinition(beanName);
				}

				@Override
				public ConfigurableListableBeanFactory getParentBeanFactory() {
					return (ConfigurableListableBeanFactory) super.getParentBeanFactory();
				}

			};

			final ConfigurableApplicationContext internalContext = new GenericApplicationContext(internalBeanFactory);

    		externalContext.addApplicationListener(new ApplicationListener() {
    			public void onApplicationEvent(ApplicationEvent event) {
    				if(event.getSource() == internalContext) return;
    				if(!(event instanceof ContextRefreshedEvent)) return;

    				/*
    				System.out.println("copying BFPPs");
            		for(Object bfpp : externalContext.getBeanFactoryPostProcessors())
            			if(!bfpp.getClass().equals(MyConfigurationPostProcessor.class))
            				internalContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);
            			*/

    				System.out.println("refreshing child");
    				internalContext.setParent(externalContext);
    				internalContext.refresh();
    			}
    		});
			//org.springframework.util.Assert.isTrue(internalBeanFactory.getParentBeanFactory() == externalContext.getBeanFactory());

			return internalBeanFactory;
		}

	}

	public @Test void myCPP() {
		GenericApplicationContext ac = new GenericApplicationContext();

		String configClassName = MyConfig.class.getName();
		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configClassName);
		//configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
		ac.registerBeanDefinition(configClassName, configBeanDef);

		NewConfigurationPostProcessor ncpp = new NewConfigurationPostProcessor();
		ncpp.setApplicationContext(ac);
		ac.addBeanFactoryPostProcessor(ncpp);

		ac.refresh();

		TestBean a1 = (TestBean) ac.getBean("alice");
		TestBean a2 = (TestBean) ac.getBean("alice");
		Assert.assertSame(a1, a2);
		Assert.assertEquals("cheshire", a1.getSpouse().getName());
	}
	public @Test void testWithXML() {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("TempTests.xml", TempTests.class);
		TestBean a1 = (TestBean) ac.getBean("alice");
		TestBean a2 = (TestBean) ac.getBean("alice");
		Assert.assertSame(a1, a2);
		Assert.assertEquals("cheshire", a1.getSpouse().getName());
	}
	public @Test void testWithJCAC() {
		JavaConfigApplicationContext ac = new JavaConfigApplicationContext(MyConfig.class);
		TestBean a1 = (TestBean) ac.getBean("alice");
		TestBean a2 = (TestBean) ac.getBean("alice");
		Assert.assertSame(a1, a2);
		Assert.assertEquals("cheshire", a1.getSpouse().getName());
	}
	@Configuration
	public static class MyConfig {
		@Bean TestBean cheshire() { return new TestBean("cheshire"); }
		public @Bean TestBean alice() { return new TestBean(cheshire()); }
	}


	public @Test void quick() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(XConfig.class);
		TestBean alice = ctx.getBean(TestBean.class, "alice");
	}
	public static class XConfig {
		public @Bean TestBean alice() { return new TestBean("alice"); }
	}

}