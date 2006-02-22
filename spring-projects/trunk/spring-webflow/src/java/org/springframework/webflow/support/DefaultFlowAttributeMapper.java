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
package org.springframework.webflow.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.map.AttributeMap;
import org.springframework.binding.map.UnmodifiableAttributeMap;
import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;

/**
 * Generic flow attribute mapper implementation that allows mappings to be
 * configured in a declarative fashion.
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>inputAttribute(s)</td>
 * <td><i>null</i></td>
 * <td>Sets the name of input attributes in flow scope to map to the subflow.</td>
 * </tr>
 * <tr>
 * <td>inputMapper</td>
 * <td><i>null</i></td>
 * <td>The AttributeMapper strategy responsible for mapping starting subflow
 * input attributes from a suspending parent flow.</td>
 * </tr>
 * <tr>
 * <td>inputMapping(s)</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping <i>input data</i> from the parent flow
 * to a newly spawned sub flow. Each list item must be a String, a Map or a
 * List. If the list item is a simple String value, the attribute will be mapped
 * as having the same name in the parent flow and in the sub flow. If the list
 * item is a Map, each map entry must be a String key naming the attribute in
 * the parent flow, and a String value naming the attribute in the child flow.
 * If the list item is itself a List, then that list is evaluated recursively,
 * and must contain Strings, Lists or Maps.</td>
 * </tr>
 * <tr>
 * <td>inputMappingsMap</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping <i>input data</i> from the parent flow
 * to a newly spawned sub flow. The keys in given map are the names of entries
 * in the parent model that will be mapped. The value associated with a key is
 * the name of the target entry that will be placed in the subflow model.</td>
 * </tr>
 * <tr>
 * <td>outputAttribute(s)</td>
 * <td><i>null</i></td>
 * <td>ets the name of output attributes in flow scope to map to the parent
 * flow.</td>
 * </tr>
 * <tr>
 * <td>outputMapper</td>
 * <td><i>null</i></td>
 * <td>The AttributeMapper strategy responsible for mapping ending subflow
 * output attributes to a resuming parent flow as output.</td>
 * </tr>
 * <tr>
 * <td>outputMapping(s)</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping subflow <i>output</i> data back to the
 * parent flow (once the subflow ends and the parent flow resumes). Each list
 * item must be a String, a List or a Map. If the list item is a simple String
 * value, the attribute will be mapped as having the same name in the parent
 * flow and in the child flow. If the list item is a Map, each map entry must be
 * a String key naming the attribute in the sub flow, and a String value naming
 * the attribute in the parent flow. If the list item is itself a List, then
 * that list is evaluated recursively, and must contain Strings, Lists or Maps.</td>
 * </tr>
 * <tr>
 * <td>outputMappingsMap</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping subflow <i>output</i> data back to the
 * parent flow (once the subflow ends and the parent flow resumes). The keys in
 * given map are the names of entries in the subflow model that will be mapped.
 * The value associated with a key is the name of the target entry that will be
 * placed in the parent flow model.</td>
 * </tr>
 * </table>
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.propName"). When the <i>from</i> mapping string is
 * enclosed in "${...}", it will be interpreted as an expression that will be
 * evaluated against the request execution context.
 * 
 * @see org.springframework.webflow.RequestContext
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class DefaultFlowAttributeMapper implements FlowAttributeMapper, Serializable {

	/**
	 * Logger, usable in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());;

	/**
	 * The mapper that maps attributes into the spawning subflow.
	 */
	private DefaultAttributeMapper inputMapper = new DefaultAttributeMapper();

	/**
	 * The mapper that maps attributes out from an ending subflow.
	 */
	private DefaultAttributeMapper outputMapper = new DefaultAttributeMapper();

	public void addInputMapping(Mapping inputMapping) {
		inputMapper.addMapping(inputMapping);
	}

	public void addInputMappings(Mapping[] inputMappings) {
		inputMapper.addMappings(inputMappings);
	}

	public void addInputAttribute(String inputAttributeName) {
		PropertyExpression expr = ExpressionFactory.parsePropertyExpression(inputAttributeName);
		inputMapper.addMapping(new Mapping(new FlowScopeExpression(expr), expr, null));
	}

	public void addInputAttributes(String[] inputAttributeNames) {
		if (inputAttributeNames == null) {
			return;
		}
		for (int i = 0; i < inputAttributeNames.length; i++) {
			addInputAttribute(inputAttributeNames[i]);
		}
	}

	public void addOutputMapping(Mapping outputMapping) {
		outputMapper.addMapping(outputMapping);
	}

	public void addOutputMappings(Mapping[] outputMappings) {
		outputMapper.addMappings(outputMappings);
	}

	public void addOutputAttribute(String outputAttributeName) {
		outputMapper.addMapping(new MappingBuilder().source(outputAttributeName).value());
	}

	public void addOutputAttributes(String[] outputAttributeNames) {
		if (outputAttributeNames == null) {
			return;
		}
		for (int i = 0; i < outputAttributeNames.length; i++) {
			addOutputAttribute(outputAttributeNames[i]);
		}
	}

	public Mapping[] getInputMappings() {
		return inputMapper.getMappings();
	}

	public Mapping[] getOutputMappings() {
		return outputMapper.getMappings();
	}

	public AttributeMap createSubflowInput(RequestContext context) {
		if (inputMapper != null) {
			AttributeMap input = new AttributeMap();
			// map from request context to input map
			inputMapper.map(context, input, getMappingContext(context));
			return input;
		}
		else {
			// an empty, but modifiable map
			return new AttributeMap();
		}
	}

	public void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		if (outputMapper != null && subflowOutput != null) {
			// map from request context to parent flow scope
			outputMapper.map(subflowOutput, context.getFlowExecutionContext().getActiveSession().getScope(),
					getMappingContext(context));
		}
	}

	/**
	 * Returns a map of contextual data available during mapping.
	 */
	protected Map getMappingContext(RequestContext context) {
		return Collections.EMPTY_MAP;
	}

	public String toString() {
		return new ToStringCreator(this).append("inputMapper", inputMapper).append("outputMapper", outputMapper)
				.toString();
	}
}