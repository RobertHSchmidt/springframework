package org.springframework.config.java.model;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Primary;

/**
 * TODO: clean up duplication between the two tests herein
 *
 * @author Chris Beams
 */
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

	public static interface MethodAnnotationPrototype { void targetMethod(); }
	public static <A extends Annotation> A extractAnnotation(Class<A> targetAnno, Class<? extends MethodAnnotationPrototype> prototype) {
		try {
			return prototype.getDeclaredMethod("targetMethod").getAnnotation(targetAnno);
		} catch (Exception ex) { throw new RuntimeException(ex); }
	}

	public @Test void renderWithPrimaryBean() {
		String configClassName = "com.acme.OrderConfig";
		String beanName = "order";

		// create a simple configuration model
		Bean metadata = extractAnnotation(Bean.class, new MethodAnnotationPrototype() {
			@Bean(primary=Primary.TRUE)
			public void targetMethod() { }
		}.getClass());
		model.add(new ConfigurationClass(configClassName).add(new BeanMethod(beanName, metadata)));
		model.assertIsValid();

		// encode expectations
		expect(registry.getBeanDefinitionCount()).andReturn(0);

		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configClassName);
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
		registry.registerBeanDefinition(configClassName, configBeanDef);
		expectLastCall();


		RootBeanDefinition rbd = new RootBeanDefinition();
		rbd.setFactoryBeanName(configClassName);
		rbd.setFactoryMethodName(beanName);
		rbd.setPrimary(true);
		registry.registerBeanDefinition(beanName, rbd);
		expectLastCall();

		expect(registry.getBeanDefinitionCount()).andReturn(2);

		replay(registry);

		// execute assertions and verifications
		assertEquals(2, renderer.render(model));

		verify(registry);
	}

}
