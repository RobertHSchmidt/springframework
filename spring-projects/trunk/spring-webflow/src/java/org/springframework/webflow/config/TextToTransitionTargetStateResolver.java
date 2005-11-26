/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.config;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.webflow.Transition;
import org.springframework.webflow.Transition.StaticTargetStateResolver;
import org.springframework.webflow.Transition.TargetStateResolver;

/**
 * Converter that takes an encoded string representation and produces a
 * corresponding <code>Transition.TargetStateResolver</code> object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"stateId" - will result in a TargetStateResolver that always resolves to
 * the same state, an instance of ({@link org.springframework.webflow.Transition.StaticTargetStateResolver})
 * </li>
 * <li>"bean:&lt;id&gt;" - will result in usage of a custom TargetStateResolver
 * bean implementation.</li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionTargetStateResolver extends AbstractConverter {

	/**
	 * Prefix used when the user wants to use a custom TransitionCriteria
	 * implementation managed by a factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Parser to user for parsing transition criteria expressions.
	 */
	private ExpressionParser expressionParser = ExpressionParserUtils.getDefaultExpressionParser();

	/**
	 * Locator to use for loading custom TransitionCriteria beans.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * Create a new converter that converts strings to transition target state
	 * resovler objects. The given conversion service will be used to do all
	 * necessary internal conversion (e.g. parsing expression strings).
	 */
	public TextToTransitionTargetStateResolver(FlowArtifactFactory artifactLocator) {
		this.flowArtifactFactory = artifactLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Transition.TargetStateResolver.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedCriteria = (String)source;
		if (expressionParser.isExpression(encodedCriteria)) {
			throw new UnsupportedOperationException("Expression-based resolvers not yet supported");
		}
		else if (encodedCriteria.startsWith(BEAN_PREFIX)) {
			return flowArtifactFactory.getTargetStateResolver(encodedCriteria.substring(BEAN_PREFIX.length()));
		}
		else {
			return createStaticTargetStateResolver(encodedCriteria);
		}
	}

	/**
	 * Hook method subclasses can overridw.
	 * @param stateId the stateid
	 * @return the target state resolver
	 * @throws ConversionException when something goes wrong
	 */
	protected TargetStateResolver createStaticTargetStateResolver(String stateId) throws ConversionException {
		return new StaticTargetStateResolver(stateId);
	}
}