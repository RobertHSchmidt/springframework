package org.springframework.binding.mapping;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyExpression;

/**
 * A mapping that is required.
 * 
 * @author Keith Donald
 */
public class RequiredMapping extends Mapping {
	
	/**
	 * Creates a required mapping.
	 * @param sourceExpression the source mapping expression
	 * @param targetPropertyExpression the target property expression
	 * @param typeConverter a type converter
	 */
	public RequiredMapping(Expression sourceExpression, PropertyExpression targetPropertyExpression,
			ConversionExecutor typeConverter) {
		super(sourceExpression, targetPropertyExpression, typeConverter, true);
	}
}
