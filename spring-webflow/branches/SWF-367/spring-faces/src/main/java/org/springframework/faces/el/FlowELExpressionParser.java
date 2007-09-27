package org.springframework.faces.el;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.springframework.binding.expression.el.DefaultELContextFactory;
import org.springframework.binding.expression.el.DefaultELResolver;
import org.springframework.binding.expression.el.ELExpressionParser;
import org.springframework.faces.webflow.el.DelegatingAppContextELResolver;
import org.springframework.faces.webflow.el.FlowELResolver;
import org.springframework.faces.webflow.el.ExternalContextELResolver;
import org.springframework.faces.webflow.el.RequestContextELResolver;

/**
 * An ExpressionParser that allows Spring and Web Flow managed beans to be referenced in expressions in the
 * FlowDefinition.
 * 
 * @author Jeremy Grelle
 */
public class FlowELExpressionParser extends ELExpressionParser {

	/**
	 * Creates a JSF 1.2 expression parser
	 * @param expressionFactory the unified EL expression factory implementation to use
	 */
	public FlowELExpressionParser(ExpressionFactory expressionFactory) {
		super(expressionFactory, new FlowELContextFactory());
	}

	/**
	 * Simple little helper that grabs the current EL context from the faces context to support EL expression
	 * evaluation.
	 */
	private static class FlowELContextFactory extends DefaultELContextFactory {

		public ELContext getEvaluationContext(Object target) {
			ELContext flowELContext = super.getEvaluationContext(null);
			DefaultELResolver resolver = (DefaultELResolver) flowELContext.getELResolver();
			resolver.add(new RequestContextELResolver());
			resolver.add(new FlowELResolver());
			resolver.add(new ExternalContextELResolver());
			resolver.add(new DelegatingAppContextELResolver());
			return flowELContext;
		}
	}

}
