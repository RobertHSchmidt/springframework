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
package org.springframework.webflow.action;

import java.util.Collections;
import java.util.Map;

import org.springframework.binding.mapping.AttributeMapper;
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
 * If you use this, you also need to specify the <code>sourceExpression</code>.</td>
 * </tr>
 * <tr>
 * <td>valueConverter</td>
 * <td><i>null</i></td>
 * <td>Set a value converter to use during the mapping. This is optional and
 * will only be used if you do not explicitly set the mapper or mapping to use,
 * but instead used the <code>sourceExpression</code> and
 * <code>targetExpression</code> properties.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.binding.mapping.AttributeMapper
 * @see org.springframework.webflow.RequestContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class AttributeMapperAction extends AbstractAction {

	/**
	 * The attribute mapper strategy.
	 */
	private AttributeMapper attributeMapper;

	public AttributeMapperAction(AttributeMapper attributeMapper) {
		this.attributeMapper = attributeMapper;
	}
	
	protected Event doExecute(RequestContext context) throws Exception {
		if (attributeMapper != null) {
			// map arbitrary attributes from the request context to the request context
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