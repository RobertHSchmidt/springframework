package org.springframework.config.java.model;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class BeanDefinitionJavaConfigurationModelRendererTests {

	private BeanDefinitionJavaConfigurationModelRenderer renderer;
	private BeanDefinitionRegistry registry;
	private JavaConfigurationModel model;

	@Before
	public void setUp() {
		registry = createMock(BeanDefinitionRegistry.class);
		renderer = new BeanDefinitionJavaConfigurationModelRenderer(registry);
		model = new JavaConfigurationModel();
	}

	public @Test void renderModel() {
		model.addConfigurationClass(new ConfigurationClass("com.acme.OrderConfig").addBeanMethod(new BeanMethod("order")));
		model.assertIsValid();

		expect(registry.getBeanDefinitionCount()).andReturn(0);

		registry.registerBeanDefinition("com.acme.OrderConfig",
			rootBeanDefinition("com.acme.OrderConfig").getBeanDefinition());
		expectLastCall();

		registry.registerBeanDefinition("order",
			rootBeanDefinition((String)null)
				.setFactoryBean("com.acme.OrderConfig", "order")
				.getBeanDefinition());
		expectLastCall();

		expect(registry.getBeanDefinitionCount()).andReturn(2);

		replay(registry);

		assertEquals(2, renderer.renderModel(model));

		verify(registry);
	}

}
