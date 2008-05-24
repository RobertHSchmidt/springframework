package org.springframework.config.java.context;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.model.ConfigurationClass;

public class ConfigurationEnhancingBeanFactoryPostProcessorTests {


	// TODO: [refactor] bpp's are scheduled for refactoring.  This is currently
	// failing because InternalBeanFactoryEstablishingPostProcessor needs to run
	// before ConfigurationEnhancingBPP does.  This coupling will be factored out
	// shortly.
	@Ignore
	public @Test void postProcessBeanFactory() {
		ConfigurableListableBeanFactory beanFactory = new DefaultListableBeanFactory();

		String nonConfigClassName = "com.acme.OrderService";
		String originalClassName = "com.acme.OrderServiceConfig";
		String enhancedClassName = "com.acme.OrderServiceConfig_ENHANCED";

		// set up a valid 'configuration bean definition'
		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(originalClassName);
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
		((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(configBeanDef.getBeanClassName(), configBeanDef);

		// add another bean def, a 'non-config bean definition'
		RootBeanDefinition nonConfigBeanDef = new RootBeanDefinition();
		nonConfigBeanDef.setBeanClassName(nonConfigClassName);
		((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(nonConfigBeanDef.getBeanClassName(), nonConfigBeanDef);

		// mock up an enhancer instance
		ConfigurationEnhancer enhancer = createMock(ConfigurationEnhancer.class);
		expect(enhancer.enhance(originalClassName)).andReturn(enhancedClassName);
		replay(enhancer);

		// execute the scenario
		ConfigurationEnhancingBeanFactoryPostProcessor processor = new ConfigurationEnhancingBeanFactoryPostProcessor();
		processor.setConfigurationEnhancer(enhancer);
		processor.postProcessBeanFactory(beanFactory);

		// did our mock get called as expected?
		verify(enhancer);

		// the configuration bean definition's className property should now be changed to the enhanced name
		assertEquals(2, beanFactory.getBeanDefinitionCount());
		// beanDef name should still be the original class name - this ensures that it remains addressable by clients
		assertEquals("class name was not replaced",
			enhancedClassName, // expected
			beanFactory.getBeanDefinition(originalClassName).getBeanClassName()); // actual
	}

}
