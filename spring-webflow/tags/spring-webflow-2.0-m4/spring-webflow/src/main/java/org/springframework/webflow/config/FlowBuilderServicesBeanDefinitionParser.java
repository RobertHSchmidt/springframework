package org.springframework.webflow.config;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.util.StringUtils;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.expression.DefaultExpressionParserFactory;
import org.springframework.webflow.mvc.MvcViewFactoryCreator;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the <code>&lt;flow-builder-services&gt;</code> tag.
 * 
 * @author Jeremy Grelle
 */
class FlowBuilderServicesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String CONVERSION_SERVICE_ATTRIBUTE = "conversion-service";

	private static final String EXPRESSION_PARSER_ATTRIBUTE = "expression-parser";

	private static final String VIEW_FACTORY_CREATOR_ATTRIBUTE = "view-factory-creator";

	private static final String CONVERSION_SERVICE_PROPERTY = "conversionService";

	private static final String EXPRESSION_PARSER_PROPERTY = "expressionParser";

	private static final String VIEW_FACTORY_CREATOR_PROPERTY = "viewFactoryCreator";

	protected Class getBeanClass(Element element) {
		return FlowBuilderServices.class;
	}

	protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		parseConversionService(element, builder, context);
		parseExpressionParser(element, builder, context);
		parseViewFactoryCreator(element, builder, context);
	}

	private void parseConversionService(Element element, BeanDefinitionBuilder definitionBuilder, ParserContext context) {
		String conversionService = element.getAttribute(CONVERSION_SERVICE_ATTRIBUTE);
		if (StringUtils.hasText(conversionService)) {
			definitionBuilder.addPropertyReference(CONVERSION_SERVICE_PROPERTY, conversionService);
		} else {
			definitionBuilder.addPropertyValue(CONVERSION_SERVICE_PROPERTY, new DefaultConversionService());
		}
	}

	private void parseExpressionParser(Element element, BeanDefinitionBuilder definitionBuilder, ParserContext context) {
		String expressionParser = element.getAttribute(EXPRESSION_PARSER_ATTRIBUTE);
		if (StringUtils.hasText(expressionParser)) {
			definitionBuilder.addPropertyReference(EXPRESSION_PARSER_PROPERTY, expressionParser);
		} else {
			definitionBuilder.addPropertyValue(EXPRESSION_PARSER_PROPERTY, DefaultExpressionParserFactory
					.getExpressionParser());
		}
	}

	private void parseViewFactoryCreator(Element element, BeanDefinitionBuilder definitionBuilder, ParserContext context) {
		String viewFactoryCreator = element.getAttribute(VIEW_FACTORY_CREATOR_ATTRIBUTE);
		if (StringUtils.hasText(viewFactoryCreator)) {
			definitionBuilder.addPropertyReference(VIEW_FACTORY_CREATOR_PROPERTY, viewFactoryCreator);
		} else {
			definitionBuilder.addPropertyReference(VIEW_FACTORY_CREATOR_PROPERTY, createBeanDefinitionForClass(
					MvcViewFactoryCreator.class, context).getBeanName());
		}
	}

	private BeanDefinitionHolder createBeanDefinitionForClass(Class clazz, ParserContext context) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
		BeanDefinitionHolder holder = new BeanDefinitionHolder(builder.getBeanDefinition(), BeanDefinitionReaderUtils
				.generateBeanName(builder.getBeanDefinition(), context.getRegistry()));
		registerBeanDefinition(holder, context.getRegistry());
		return holder;
	}

	public static BeanDefinitionHolder registerDefaultFlowBuilderServicesBeanDefinition(ParserContext context) {
		FlowBuilderServicesBeanDefinitionParser parser = new FlowBuilderServicesBeanDefinitionParser();
		BeanDefinitionBuilder defaultBuilder = BeanDefinitionBuilder.genericBeanDefinition(FlowBuilderServices.class);
		defaultBuilder.addPropertyValue(CONVERSION_SERVICE_PROPERTY, new DefaultConversionService());
		defaultBuilder.addPropertyValue(EXPRESSION_PARSER_PROPERTY, DefaultExpressionParserFactory
				.getExpressionParser());
		defaultBuilder.addPropertyReference(VIEW_FACTORY_CREATOR_PROPERTY, parser.createBeanDefinitionForClass(
				MvcViewFactoryCreator.class, context).getBeanName());
		BeanDefinitionHolder holder = new BeanDefinitionHolder(defaultBuilder.getBeanDefinition(),
				BeanDefinitionReaderUtils.generateBeanName(defaultBuilder.getBeanDefinition(), context.getRegistry()));
		parser.registerBeanDefinition(holder, context.getRegistry());
		return holder;
	}
}
