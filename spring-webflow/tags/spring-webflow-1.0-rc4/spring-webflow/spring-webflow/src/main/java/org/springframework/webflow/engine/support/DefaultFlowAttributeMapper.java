/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.engine.support;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.execution.support.FlowScopeExpression;

/**
 * Generic flow attribute mapper implementation that allows mappings to be
 * configured in a declarative fashion.
 * <p>
 * Two types of mappings may be configured, input mappings and output mappings:
 * <ol>
 * <li>Input mappings define the rules for mapping attributes in a parent flow
 * to a spawning subflow.
 * <li>Output mappings define the rules for mapping attributes returned from an
 * ended subflow into the resuming parent.
 * </ol>
 * <p>
 * The mappings defined using the configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.nestedPropName"). When the <i>from</i> mapping string is
 * enclosed in "${...}", it will be interpreted as an expression that will be
 * evaluated against the flow execution request context.
 * 
 * @see org.springframework.webflow.execution.RequestContext
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class DefaultFlowAttributeMapper extends AbstractFlowAttributeMapper implements Serializable {

	/**
	 * Logger, usable in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());;

	/**
	 * The expression parser that will parse input and output attribute
	 * expressions.
	 */
	private ExpressionParser expressionParser = DefaultExpressionParserFactory.getExpressionParser();

	/**
	 * The mapper that maps attributes into a spawning subflow.
	 */
	private DefaultAttributeMapper inputMapper = new DefaultAttributeMapper();

	/**
	 * The mapper that maps attributes returned by an ended subflow.
	 */
	private DefaultAttributeMapper outputMapper = new DefaultAttributeMapper();

	/**
	 * Set the expression parser responsible for parsing expression strings into
	 * evaluatable expression objects.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		Assert.notNull(expressionParser, "The expression parser is required");
		this.expressionParser = expressionParser;
	}

	/**
	 * Adds a new input mapping. Use when you need full control over defining
	 * how a subflow input attribute mapping will be perfomed.
	 * @param inputMapping the input mapping
	 * @return this, to support call chaining
	 */
	public DefaultFlowAttributeMapper addInputMapping(Mapping inputMapping) {
		inputMapper.addMapping(inputMapping);
		return this;
	}

	/**
	 * Adds a collection of input mappings. Use when you need full control over
	 * defining how an subflow input attribute mapping will be perfomed.
	 * @param inputMappings the input mappings
	 */
	public void addInputMappings(Mapping[] inputMappings) {
		inputMapper.addMappings(inputMappings);
	}

	/**
	 * Adds an input mapping that maps a single attribute in parent <i>flow scope</i>
	 * into the subflow input map. For instance: "x" will result in the "x" attribute
	 * in parent flow scope being mapped into the subflow input map as "x".
	 * @param inputAttributeName the attribute in flow scope to map into the
	 * subflow
	 * @return this, to support call chaining
	 */
	public DefaultFlowAttributeMapper addInputAttribute(String inputAttributeName) {
		PropertyExpression expr = expressionParser.parsePropertyExpression(inputAttributeName);
		inputMapper.addMapping(new Mapping(new FlowScopeExpression(expr), expr, null));
		return this;
	}

	/**
	 * Adds a collection of input mappings that map attributes in parent <i>flow
	 * scope</i> into the subflow input map. For instance: "x" will result in the "x"
	 * attribute in parent flow scope being mapped into the subflow input map as "x".
	 * @param inputAttributeNames the attributes in flow scope to map into the
	 * subflow
	 */
	public void addInputAttributes(String[] inputAttributeNames) {
		if (inputAttributeNames == null) {
			return;
		}
		for (int i = 0; i < inputAttributeNames.length; i++) {
			addInputAttribute(inputAttributeNames[i]);
		}
	}

	/**
	 * Adds a new output mapping. Use when you need full control over defining
	 * how a subflow output attribute mapping will be perfomed.
	 * @param outputMapping the output mapping
	 * @return this, to support call chaining
	 */
	public DefaultFlowAttributeMapper addOutputMapping(Mapping outputMapping) {
		outputMapper.addMapping(outputMapping);
		return this;	
	}

	/**
	 * Adds a collection of output mappings. Use when you need full control over
	 * defining how a subflow output attribute mapping will be perfomed.
	 * @param outputMappings the output mappings
	 */
	public void addOutputMappings(Mapping[] outputMappings) {
		outputMapper.addMappings(outputMappings);
	}

	/**
	 * Adds an output mapping that maps a single subflow output attribute into
	 * the <i>flow scope</i> of the resuming parent flow. For instance: "y" will
	 * result in the "y" attribute of the subflow output map being mapped into
	 * the flowscope of the resuming parent flow as "y".
	 * @param outputAttributeName the subflow output attribute to map into the
	 * parent flow scope
	 * @return this, to support call chaining
	 */
	public DefaultFlowAttributeMapper addOutputAttribute(String outputAttributeName) {
		Expression source = expressionParser.parseExpression(outputAttributeName);
		PropertyExpression target = expressionParser.parsePropertyExpression(outputAttributeName);
		outputMapper.addMapping(new Mapping(source, new FlowScopeExpression(target), null));
		return this;
	}

	/**
	 * Adds a collection of output mappings that map subflow output attributes
	 * into the scope of the resuming parent flow. For instance: "y" will
	 * result in the "y" attribute of the subflow output map being mapped into
	 * the flowscope of the resuming parent flow as "y".
	 * @param outputAttributeNames the subflow output attributes to map into the
	 * parent flow
	 */
	public void addOutputAttributes(String[] outputAttributeNames) {
		if (outputAttributeNames == null) {
			return;
		}
		for (int i = 0; i < outputAttributeNames.length; i++) {
			addOutputAttribute(outputAttributeNames[i]);
		}
	}

	/**
	 * Returns a typed-array of configured input mappings.
	 * @return the configured input mappings
	 */
	public Mapping[] getInputMappings() {
		return inputMapper.getMappings();
	}

	/**
	 * Returns a typed-array of configured output mappings.
	 * @return the configured output mappings
	 */
	public Mapping[] getOutputMappings() {
		return outputMapper.getMappings();
	}

	/**
	 * Factory method that returns a mapping builder helper for building
	 * {@link Mapping} objects.
	 * @return the mapping builder
	 */
	protected MappingBuilder mapping() {
		return new MappingBuilder(expressionParser);
	}

	protected AttributeMapper getInputMapper() {
		return inputMapper;
	}

	protected AttributeMapper getOutputMapper() {
		return outputMapper;
	}

	public String toString() {
		return new ToStringCreator(this).append("inputMapper", inputMapper).append("outputMapper", outputMapper)
				.toString();
	}
}