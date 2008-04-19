package org.springframework.config.java.model;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

public class BeanDefinitionRegisteringConfigurationModelRendererTests {

	private BeanDefinitionRegisteringConfigurationModelRenderer renderer;
	private BeanDefinitionRegistry registry;
	private ConfigurationModel model;

	@Before
	public void setUp() {
		registry = createMock(BeanDefinitionRegistry.class);
		renderer = new BeanDefinitionRegisteringConfigurationModelRenderer(registry);
		model = new ConfigurationModel();
	}

	public @Test void render() {
		String configClassName = "com.acme.OrderConfig";
		String beanName = "order";

		// create a simple configuration model
		model.add(new ConfigurationClass(configClassName).add(new BeanMethod(beanName)));
		model.assertIsValid();

		// encode expectations
		expect(registry.getBeanDefinitionCount()).andReturn(0);

		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configClassName);
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
		registry.registerBeanDefinition(configClassName, configBeanDef);
		expectLastCall();

		registry.registerBeanDefinition(beanName,
			rootBeanDefinition((String)null)
				.setFactoryBean(configClassName, beanName)
				.getBeanDefinition());
		expectLastCall();

		expect(registry.getBeanDefinitionCount()).andReturn(2);

		replay(registry);

		// execute assertions and verifications
		assertEquals(2, renderer.render(model));

		verify(registry);
	}

}
