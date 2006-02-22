package org.springframework.binding.mapping;

import java.util.Collections;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.util.Assert;

public class MappingBuilder {
	private Expression sourceExpression;

	private PropertyExpression targetExpression;

	private Class sourceType;

	private Class targetType;

	private ExpressionParser expressionParser = ExpressionParserUtils.getDefaultExpressionParser();

	private ConversionService conversionService = new DefaultConversionService();

	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public MappingBuilder source(String expressionString) {
		sourceExpression = expressionParser.parseExpression(expressionString, Collections.EMPTY_MAP);
		return this;
	}

	public MappingBuilder target(String expressionString) {
		targetExpression = (PropertyExpression)expressionParser.parseExpression(expressionString, Collections.EMPTY_MAP);
		return this;
	}

	public MappingBuilder from(Class sourceType) {
		Assert.notNull(sourceType, "The source type is required");
		this.sourceType = sourceType;
		return this;
	}

	public MappingBuilder to(Class targetType) {
		Assert.notNull(sourceType, "The target type is required");
		this.targetType = targetType;
		return this;
	}

	public Mapping value() {
		Assert.notNull(sourceExpression, "The source expression must be set at a minimum");
		if (targetExpression == null) {
			targetExpression = (PropertyExpression)sourceExpression;
		}
		ConversionExecutor typeConverter = null;
		if (sourceType != null) {
			Assert.notNull(targetType, "The target type is required when the source type is specified");
			typeConverter = conversionService.getConversionExecutor(sourceType, targetType);
		}
		Mapping result = new Mapping(sourceExpression, targetExpression, typeConverter);
		sourceExpression = null;
		targetExpression = null;
		sourceType = null;
		targetType = null;
		return result;
	}
}