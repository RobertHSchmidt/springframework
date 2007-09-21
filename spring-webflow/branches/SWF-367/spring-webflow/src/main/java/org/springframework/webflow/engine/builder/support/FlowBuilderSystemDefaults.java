package org.springframework.webflow.engine.builder.support;

import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.webflow.action.BeanInvokingActionFactory;

public class FlowBuilderSystemDefaults {
	private FlowBuilderServices defaultServices;

	public FlowBuilderSystemDefaults() {
		defaultServices = new FlowBuilderServices();
		defaultServices.setFlowArtifactFactory(new FlowArtifactFactory());
		defaultServices.setBeanInvokingActionFactory(new BeanInvokingActionFactory());
		defaultServices.setConversionService(new DefaultConversionService());
		defaultServices.setExpressionParser(createOgnlExpressionParser());
		defaultServices.setResourceLoader(new DefaultResourceLoader());
		defaultServices.setBeanFactory(new StaticListableBeanFactory());
	}

	public FlowBuilderServices createBuilderServices() {
		FlowBuilderServices builderServices = new FlowBuilderServices();
		applyDefaults(builderServices);
		return builderServices;
	}

	public void applyDefaults(FlowBuilderServices services) {
		services.setFlowArtifactFactory(defaultServices.getFlowArtifactFactory());
		services.setBeanInvokingActionFactory(defaultServices.getBeanInvokingActionFactory());
		services.setConversionService(defaultServices.getConversionService());
		services.setExpressionParser(defaultServices.getExpressionParser());
		services.setResourceLoader(defaultServices.getResourceLoader());
		services.setBeanFactory(defaultServices.getBeanFactory());
	}

	private ExpressionParser createOgnlExpressionParser() {
		try {
			Class.forName("ognl.Ognl");
			return new WebFlowOgnlExpressionParser();
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"Unable to load the default expression parser: OGNL could not be found in the classpath.  "
							+ "Please add OGNL 2.x to your classpath or set the default ExpressionParser instance to something that is in the classpath.  "
							+ "Details: " + e.getMessage());
		} catch (NoClassDefFoundError e) {
			throw new IllegalStateException(
					"Unable to construct the default expression parser: ognl.Ognl could not be instantiated.  "
							+ "Please add OGNL 2.x to your classpath or set the default ExpressionParser instance to something that is in the classpath.  "
							+ "Details: " + e);
		}
	}

	public static FlowBuilderServices get() {
		return new FlowBuilderSystemDefaults().createBuilderServices();
	}
}