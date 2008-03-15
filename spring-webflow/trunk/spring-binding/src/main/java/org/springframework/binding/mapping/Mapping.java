/*
 * Copyright 2004-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.binding.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageResolver;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A single mapping definition, encapsulating the information necessary to map the result of evaluating an expression on
 * a source object to a property on a target object, optionally applying a type conversion during the mapping process.
 * 
 * @author Keith Donald
 */
public class Mapping {

	private static final Log logger = LogFactory.getLog(Mapping.class);

	/**
	 * The source expression to evaluate against a source object to map from.
	 */
	private final Expression sourceExpression;

	/**
	 * The target expression to set on a target object to map to.
	 */
	private final Expression targetExpression;

	/**
	 * A type converter to apply during the mapping process.
	 */
	private final ConversionExecutor typeConverter;

	/**
	 * Whether or not this is a required mapping; if true, the source expression must return a non-null value.
	 */
	private boolean required;

	/**
	 * Creates a new mapping.
	 * @param sourceExpression the source expression
	 * @param targetExpression the target expression
	 * @param typeConverter a type converter
	 * @param required whether or not this mapping is required
	 */
	public Mapping(Expression sourceExpression, Expression targetExpression, ConversionExecutor typeConverter,
			boolean required) {
		Assert.notNull(sourceExpression, "The source expression is required");
		Assert.notNull(targetExpression, "The target expression is required");
		this.sourceExpression = sourceExpression;
		this.targetExpression = targetExpression;
		this.typeConverter = typeConverter;
		this.required = required;
	}

	/**
	 * Map the <code>sourceAttribute</code> in to the <code>targetAttribute</code> target map, performing type
	 * conversion if necessary.
	 * @param source The source data structure
	 * @param target The target data structure
	 */
	public boolean map(Object source, Object target, MappingContext context) {
		Assert.notNull(source, "The source to map from is required");
		Assert.notNull(target, "The target to map to is required");
		Assert.notNull(context, "The mapping context is required");
		Object sourceValue = sourceExpression.getValue(source);
		if (required && (sourceValue == null || isEmptyString(sourceValue))) {
			String defaultText = "'" + targetExpression.getExpressionString() + "' is required";
			MessageResolver message = new MessageBuilder().error().source(targetExpression.getExpressionString())
					.codes(createMessageCodes("required", target, targetExpression)).defaultText(defaultText).build();
			context.getMessageContext().addMessage(message);
			return false;
		}
		Object targetValue;
		if (sourceValue != null && typeConverter != null) {
			try {
				targetValue = typeConverter.execute(sourceValue);
			} catch (ConversionException e) {
				e.printStackTrace();
				String defaultText = "The '" + targetExpression.getExpressionString() + "' value is the wrong type";
				MessageResolver message = new MessageBuilder().error().source(targetExpression.getExpressionString())
						.codes(createMessageCodes("typeMismatch", target, targetExpression)).defaultText(defaultText)
						.build();
				context.getMessageContext().addMessage(message);
				return false;
			}
		} else {
			targetValue = sourceValue;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Mapping '" + sourceExpression + "' value [" + sourceValue + "] to target '"
					+ targetExpression + "'; setting target value to [" + targetValue + "]");
		}
		targetExpression.setValue(target, targetValue);
		return true;
	}

	private boolean isEmptyString(Object sourceValue) {
		if (sourceValue instanceof CharSequence) {
			return ((CharSequence) sourceValue).length() == 0;
		} else {
			return false;
		}
	}

	private String[] createMessageCodes(String errorCode, Object target, Expression targetExpression) {
		String[] codes = new String[2];
		codes[0] = ClassUtils.getShortName(target.getClass()) + "." + targetExpression.getExpressionString();
		codes[1] = errorCode;
		return codes;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Mapping)) {
			return false;
		}
		Mapping other = (Mapping) o;
		return sourceExpression.equals(other.sourceExpression) && targetExpression.equals(other.targetExpression);
	}

	public int hashCode() {
		return sourceExpression.hashCode() + targetExpression.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append(sourceExpression + " -> " + targetExpression).toString();
	}
}