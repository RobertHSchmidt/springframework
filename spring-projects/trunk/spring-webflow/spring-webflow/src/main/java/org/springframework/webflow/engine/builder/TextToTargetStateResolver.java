package org.springframework.webflow.engine.builder;

import org.springframework.binding.convert.ConversionContext;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;

/**
 * Converter that takes an encoded string representation and produces a
 * corresponding {@link TargetStateResolver} object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"stateId" - will result in a TargetStateResolver that always resolves
 * the same state. </li>
 * <li>"${stateIdExpression} - will result in a TargetStateResolver that
 * resolves the target state by evaluating an expression against the request
 * context.</li>
 * <li>"bean:&lt;id&gt;" - will result in usage of a custom TargetStateResolver
 * bean implementation configured in an external context.</li>
 * </ul>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTargetStateResolver extends AbstractConverter {

	/**
	 * Prefix used when the user wants to use a custom TransitionCriteria
	 * implementation managed by a factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Locator to use for loading custom TransitionCriteria beans.
	 */
	private FlowServiceLocator flowServiceLocator;

	/**
	 * Create a new converter that converts strings to transition target state
	 * resovler objects. The given conversion service will be used to do all
	 * necessary internal conversion (e.g. parsing expression strings).
	 */
	public TextToTargetStateResolver(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { TargetStateResolver.class };
	}

	protected Object doConvert(Object source, Class targetClass, ConversionContext context) throws Exception {
		String targetStateId = (String)source;
		if (flowServiceLocator.getExpressionParser().isDelimitedExpression(targetStateId)) {
			Expression expression = flowServiceLocator.getExpressionParser().parseExpression(targetStateId);
			return new DefaultTargetStateResolver(expression);
		}
		else if (targetStateId.startsWith(BEAN_PREFIX)) {
			return flowServiceLocator.getTargetStateResolver(targetStateId.substring(BEAN_PREFIX.length()));
		}
		else {
			return new DefaultTargetStateResolver(targetStateId);
		}
	}
}