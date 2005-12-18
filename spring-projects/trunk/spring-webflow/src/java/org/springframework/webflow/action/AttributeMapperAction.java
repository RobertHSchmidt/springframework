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
package org.springframework.webflow.action;

import java.util.Collections;
import java.util.Map;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.ParameterizableAttributeMapper;
import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Action that executes an attribute mapper to map information in the request
 * context. Both the source and the target of the mapping will be the request
 * context. This allows for maximum flexibility when defining mappings,
 * typically using expressions (e.g. "${flowScope.someAttribute}").
 * <p>
 * This action always returns the
 * {@link org.springframework.webflow.action.AbstractAction#success() success}
 * event.
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>Name </b></td>
 * <td><b>Default </b></td>
 * <td><b>Description </b></td>
 * </tr>
 * <tr>
 * <td>mapping(s)</td>
 * <td><i>null</i></td>
 * <td>The mappings executed by this action.</td>
 * </tr>
 * <tr>
 * <td>attributeMapper</td>
 * <td><i>null</i></td>
 * <td>The custom mapping strategy used by this action.</td>
 * </tr>
 * <tr>
 * <td>sourceExpression</td>
 * <td><i>null</i></td>
 * <td>Set the expression which obtains the source attribute to map. If you use
 * this, you also need to specify the "targetExpression".</td>
 * </tr>
 * <tr>
 * <td>targetExpression</td>
 * <td><i>null</i></td>
 * <td>Set the expression used to set the target attribute during the mapping.
 * If you use this, you also need to specify the "sourceExpression".</td>
 * </tr>
 * <tr>
 * <td>valueConverter</td>
 * <td><i>null</i></td>
 * <td>Set a value converter to use during the mapping. This is optional and
 * will only be used if you do not explicitly set the mapper or mapping to use,
 * but instead used the "sourceExpression" and "targetExpression" properties.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.binding.AttributeMapper
 * @see org.springframework.webflow.RequestContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class AttributeMapperAction extends AbstractAction {

	/**
	 * The source value expression.
	 */
	private String sourceExpression;

	/**
	 * The target property expression.
	 */
	private String targetExpression;

	/**
	 * A type converter to apply to the source value.
	 */
	private ConversionExecutor valueConverter;

	/**
	 * The attribute mapper strategy.
	 */
	private AttributeMapper attributeMapper;

	/**
	 * Creates a new action with an initially empty mappings list.
	 */
	public AttributeMapperAction() {
	}

	/**
	 * Create a new action with the specified mapping.
	 * @param mapping the mapping
	 */
	public AttributeMapperAction(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Create a new action with the specified mappings.
	 * @param mappings the mappings
	 */
	public AttributeMapperAction(Mapping[] mappings) {
		setMappings(mappings);
	}

	/**
	 * Set the single mapping for this action.
	 * @param mapping the mapping
	 */
	public void setMapping(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Set the mappings for this action.
	 * @param mappings the mappings
	 */
	public void setMappings(Mapping[] mappings) {
		setAttributeMapper(new ParameterizableAttributeMapper(mappings));
	}

	/**
	 * Set to completely customize the attribute mapper strategy.
	 * @param mapper the mapping strategy
	 */
	public void setAttributeMapper(AttributeMapper mapper) {
		this.attributeMapper = mapper;
	}

	/**
	 * Set the expression which obtains the source attribute to map. If you use
	 * this, you also need to specify the "targetExpression".
	 */
	public void setSourceExpression(String sourceExpression) {
		this.sourceExpression = sourceExpression;
	}

	/**
	 * Set the expression used to set the target attribute during the mapping.
	 * If you use this, you also need to specify the "sourceExpression".
	 */
	public void setTargetExpression(String targetExpression) {
		this.targetExpression = targetExpression;
	}

	/**
	 * Set a value converter to use during the mapping. This is optional and
	 * will only be used if you do not explicitly set the mapper or mapping to
	 * use, but instead used the "sourceExpression" and "targetExpression"
	 * properties.
	 */
	public void setValueConverter(ConversionExecutor valueConverter) {
		this.valueConverter = valueConverter;
	}

	protected void initAction() {
		if (attributeMapper == null) {
			if (StringUtils.hasText(sourceExpression) && StringUtils.hasText(targetExpression)) {
				setAttributeMapper(new ParameterizableAttributeMapper(new Mapping(sourceExpression, targetExpression,
						valueConverter)));
			}
		}
	}

	protected Event doExecute(RequestContext context) throws Exception {
		if (attributeMapper != null) {
			// map from the request context to the request context
			attributeMapper.map(context, context, getMappingContext(context));
		}
		return success();
	}

	/**
	 * Returns a map containing extra data available during attribute mapping.
	 * The default implementation just returns an empty map. Subclasses can
	 * override this if necessary.
	 */
	protected Map getMappingContext(RequestContext context) {
		return Collections.EMPTY_MAP;
	}
}