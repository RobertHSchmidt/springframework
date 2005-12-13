/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.binding.support;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.springframework.binding.AttributeMapper;
import org.springframework.core.CollectionFactory;
import org.springframework.core.style.ToStringCreator;

/**
 * Generic attributes mapper implementation that allows mappings to be
 * configured programatically or in a Spring application context.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td>mappings</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping <i>input data </i> from a source
 * attributes collection to a target attributes collection. The provided list
 * contains the names of the attributes in the source to pass to the target for
 * access. The same name is used in both the source and target model.</td>
 * </tr>
 * <tr>
 * <td>mappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping <i>input data </i> from a source
 * attributes collection to a target attributes collection. The keys in given
 * map are the names of entries in the source model that will be mapped. The
 * value associated with a key is the name of the entry that will be placed in
 * the target model.</td>
 * </tr>
 * </table>
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propertyPath".
 * </p>
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ParameterizableAttributeMapper implements AttributeMapper, Serializable {

	private Collection mappings = Collections.EMPTY_SET;

	public ParameterizableAttributeMapper() {
	}

	public ParameterizableAttributeMapper(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	public ParameterizableAttributeMapper(Collection mappings) {
		setMappingsCollection(mappings);
	}

	/**
	 * Set the mappings for this attribute mapper.
	 * @param mappings The mappings
	 */
	public void setMappings(Mapping[] mappings) {
		this.mappings = CollectionFactory.createLinkedSetIfPossible(mappings.length);
		this.mappings.addAll(Arrays.asList(mappings));
	}

	/**
	 * Set the mappings that will be executed when mapping model data from one
	 * attributes source to another. Each list item must be a String, a Mapping
	 * object, a List, or a Map.
	 * <p>
	 * If the list item is a simple String value, the attribute will be mapped
	 * with the same name and type.
	 * <p>
	 * If the list item is a Map, each map entry must be a String key naming the
	 * attribute in the source model, and a String value naming the attribute in
	 * the target model.
	 * <p>
	 * If the list item is another List, then that list is itself evaluated
	 * recursively, and must itself contain Strings, Mapping objects, other
	 * Lists, or Maps.
	 * @param mappings The mappings
	 */
	public void setMappingsCollection(Collection mappings) {
		this.mappings = CollectionFactory.createLinkedSetIfPossible(mappings.size());
		addCollectionMappings(this.mappings, mappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data from one
	 * attributes collection to another.
	 * @link ParameterizableAttributesMapper#setMappings(List) with a List
	 * containing one item which is a Map. Each map entry must be a String key
	 * naming the attribute in the parent flow, and a String value naming the
	 * attribute in the child flow.
	 * @param mappingsMap The mappings map
	 */
	public void setMappingsMap(Map mappingsMap) {
		this.mappings = CollectionFactory.createLinkedSetIfPossible(mappings.size());
		addMapMappings(this.mappings, mappingsMap);
	}

	/**
	 * Internal worker function to convert given mappingsList to a simple
	 * mappings map.
	 */
	private void addCollectionMappings(Collection mappings, Collection mappingsList) {
		Iterator it = mappingsList.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (element instanceof Mapping) {
				mappings.add(element);
			}
			else if (element instanceof Collection) {
				addCollectionMappings(mappings, (Collection)element);
			}
			else if (element instanceof Map) {
				addMapMappings(mappings, (Map)element);
			}
			else {
				addMapping(String.valueOf(element));
			}
		}
	}

	private void addMapMappings(Collection mappings, Map mappingsMap) {
		Iterator it = mappingsMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			addMapping(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}
	}

	/**
	 * Add a mapping where the source and target attribute expressions are the
	 * same.
	 * @param expression the attribute expression to map
	 */
	public void addMapping(String expression) {
		addMapping(expression, expression);
	}

	/**
	 * Add a mapping for the source and target attribute expressions.
	 * @param sourceExpression the source expression
	 * @param targetExpression the target expression
	 */
	public void addMapping(String sourceExpression, String targetExpression) {
		addMapping(new Mapping(sourceExpression, targetExpression));
	}

	/**
	 * Add a mapping to this mapper.
	 * @param mapping the mapping to add.
	 */
	public void addMapping(Mapping mapping) {
		this.mappings.add(mapping);
	}

	/**
	 * Map data from one map to another map using specified mappings.
	 */
	public void map(Object source, Object target, Map context) {
		if (mappings != null) {
			Iterator it = this.mappings.iterator();
			while (it.hasNext()) {
				Mapping mapping = (Mapping)it.next();
				mapping.map(source, target, context);
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("mappings", mappings).toString();
	}
}