/*
 * Copyright 2006-2008 the original author or authors.
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

package org.springframework.osgi.blueprint.config.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.ConstructorArgumentEntry;
import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.beans.factory.parsing.PropertyEntry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.blueprint.config.internal.temporary.TempManagedList;
import org.springframework.osgi.blueprint.config.internal.temporary.TempManagedMap;
import org.springframework.osgi.blueprint.config.internal.temporary.TempManagedSet;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Stateful class that handles the parsing details of a &lt;component&gt;
 * elements. Borrows heavily from {@link BeanDefinitionParserDelegate}.
 * 
 * <b>Note</b>: Due to its stateful nature, this class is not thread safe.
 * <b>Note</b>: Since the namespace is important when parsing elements and
 * since mixed elements, from both rfc124 and Spring can coexist in the same
 * file, reusing the {@link BeanDefinitionParserDelegate delegate} isn't
 * entirely possible since the two state needs to be kept in synch.
 * 
 * @author Costin Leau
 * 
 */
public class ComponentParser {

	/** logger */
	private static final Log log = LogFactory.getLog(ComponentParser.class);

	public static final String COMPONENT = "component";

	public static final String NAMESPACE_URI = "http://www.osgi.org/xmlns/blueprint/v1.0.0";

	private static final String FACTORY_COMPONENT_ATTR = "factory-component";

	private final ParseState parseState;

	private final Collection<String> usedNames;

	private ParserContext parserContext;


	public ComponentParser() {
		this(null, null);
	}

	/**
	 * Constructs a new <code>ComponentParser</code> instance. Used by certain
	 * reusable static methods.
	 * 
	 * @param parserContext
	 */
	private ComponentParser(ParserContext parserContext) {
		this(null, null);
		this.parserContext = parserContext;
	}

	public ComponentParser(ParseState parseState, Collection<String> usedNames) {
		this.parseState = (parseState != null ? parseState : new ParseState());
		this.usedNames = (usedNames != null ? usedNames : new LinkedHashSet<String>());
	}

	public BeanDefinition parse(Element componentElement, ParserContext parserContext) {
		// save parser context
		this.parserContext = parserContext;

		// let Spring do its standard parsing
		BeanDefinitionHolder bdHolder = parseComponentDefinitionElement(componentElement, null);

		return bdHolder.getBeanDefinition();
	}

	public BeanDefinitionHolder parseAsHolder(Element componentElement, ParserContext parserContext) {
		// save parser context
		this.parserContext = parserContext;

		// let Spring do its standard parsing
		BeanDefinitionHolder bdHolder = parseComponentDefinitionElement(componentElement, null);

		return bdHolder;
	}

	/**
	 * Parses the supplied <code>&lt;component&gt;</code> element. May return
	 * <code>null</code> if there were errors during parse. Errors are
	 * reported to the
	 * {@link org.springframework.beans.factory.parsing.ProblemReporter}.
	 */
	private BeanDefinitionHolder parseComponentDefinitionElement(Element ele, BeanDefinition containingBean) {

		// extract bean name
		String id = ele.getAttribute(BeanDefinitionParserDelegate.ID_ATTRIBUTE);
		String nameAttr = ele.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE);

		List<String> aliases = new ArrayList<String>(4);
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr,
				BeanDefinitionParserDelegate.BEAN_NAME_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}

		String beanName = id;

		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
			beanName = (String) aliases.remove(0);
			if (log.isDebugEnabled()) {
				log.debug("No XML 'id' specified - using '" + beanName + "' as bean name and " + aliases
						+ " as aliases");
			}
		}

		if (containingBean == null) {

			if (checkNameUniqueness(beanName, aliases, usedNames)) {
				error("Bean name '" + beanName + "' is already used in this file", ele);
			}
		}

		String className = null;

		// extract class attribute
		if (ele.hasAttribute(BeanDefinitionParserDelegate.CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(BeanDefinitionParserDelegate.CLASS_ATTRIBUTE).trim();
		}

		AbstractBeanDefinition beanDefinition = null;

		try {
			// create definition
			beanDefinition = BeanDefinitionReaderUtils.createBeanDefinition(null, className,
				parserContext.getReaderContext().getBeanClassLoader());

			// parse attributes
			parseAttributes(ele, beanName, beanDefinition);

			// parse description
			beanDefinition.setDescription(DomUtils.getChildElementValueByTagName(ele,
				BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT));

			parseConstructorArgElements(ele, beanDefinition);
			parsePropertyElements(ele, beanDefinition);

			beanDefinition.setResource(parserContext.getReaderContext().getResource());
			beanDefinition.setSource(extractSource(ele));

		}
		catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", ele, ex);
		}
		catch (NoClassDefFoundError err) {
			error("Class that bean class [" + className + "] depends on not found", ele, err);
		}

		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName)) {
				try {
					if (containingBean != null) {
						beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition,
							parserContext.getRegistry(), true);
					}
					else {
						beanName = parserContext.getReaderContext().generateBeanName(beanDefinition);
						// TODO: should we support 2.0 behaviour (see below):
						// 
						// Register an alias for the plain bean class name, if still possible,
						// if the generator returned the class name plus a suffix.
						// This is expected for Spring 1.2/2.0 backwards compatibility.
					}
					if (log.isDebugEnabled()) {
						log.debug("Neither XML 'id' nor 'name' specified - " + "using generated bean name [" + beanName
								+ "]");
					}
				}
				catch (Exception ex) {
					error(ex.getMessage(), ele, ex);
					return null;
				}
			}
			return new BeanDefinitionHolder(beanDefinition, beanName);
		}

		return null;
	}

	private AbstractBeanDefinition parseAttributes(Element ele, String beanName, AbstractBeanDefinition beanDefinition) {
		AbstractBeanDefinition bd = parserContext.getDelegate().parseBeanDefinitionAttributes(ele, beanName, null,
			beanDefinition);

		// handle factory component
		String componentFactory = ele.getAttribute(FACTORY_COMPONENT_ATTR);
		if (StringUtils.hasText(componentFactory)) {
			bd.setFactoryBeanName(componentFactory);
		}

		return bd;
	}

	/**
	 * Validate that the specified bean name and aliases have not been used
	 * already.
	 */
	private boolean checkNameUniqueness(String beanName, Collection<String> aliases, Collection<String> usedNames) {
		String foundName = null;

		if (StringUtils.hasText(beanName) && usedNames.contains(beanName)) {
			foundName = beanName;
		}
		if (foundName == null) {
			foundName = (String) CollectionUtils.findFirstMatch(usedNames, aliases);
		}

		usedNames.add(beanName);
		usedNames.addAll(aliases);

		return (foundName != null);
	}

	/**
	 * Parsers contructor arguments.
	 * 
	 * @param ele
	 * @param beanDefinition
	 * @param parserContext
	 */
	private void parseConstructorArgElements(Element ele, AbstractBeanDefinition beanDefinition) {

		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element
					&& DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.CONSTRUCTOR_ARG_ELEMENT)) {
				parseConstructorArgElement((Element) node, beanDefinition);
			}
		}
	}

	private void parseConstructorArgElement(Element ele, AbstractBeanDefinition beanDefinition) {

		String indexAttr = ele.getAttribute(BeanDefinitionParserDelegate.INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(BeanDefinitionParserDelegate.TYPE_ATTRIBUTE);

		boolean hasIndex = false;
		int index = -1;

		if (StringUtils.hasLength(indexAttr)) {
			hasIndex = true;
			try {
				index = Integer.parseInt(indexAttr);
			}
			catch (NumberFormatException ex) {
				error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele);
			}

			if (index < 0) {
				error("'index' cannot be lower than 0", ele);
			}
		}

		try {
			this.parseState.push(hasIndex ? new ConstructorArgumentEntry(index) : new ConstructorArgumentEntry());
			Object value = parsePropertyValue(ele, beanDefinition, null);
			ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
			if (StringUtils.hasLength(typeAttr)) {
				valueHolder.setType(typeAttr);
			}
			valueHolder.setSource(extractSource(ele));
			ConstructorArgumentValues values = beanDefinition.getConstructorArgumentValues();

			if (hasIndex) {
				values.addIndexedArgumentValue(index, valueHolder);
			}
			else {
				values.addGenericArgumentValue(valueHolder);
			}
		}
		finally {
			this.parseState.pop();
		}
	}

	/**
	 * Parses property elements.
	 * 
	 * @param ele
	 * @param beanDefinition
	 * @param parserContext
	 */
	private void parsePropertyElements(Element ele, AbstractBeanDefinition beanDefinition) {

		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.PROPERTY_ELEMENT)) {
				parsePropertyElement((Element) node, beanDefinition);
			}
		}
	}

	private void parsePropertyElement(Element ele, BeanDefinition bd) {
		String propertyName = ele.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE);
		if (!StringUtils.hasLength(propertyName)) {
			error("Tag 'property' must have a 'name' attribute", ele);
			return;
		}
		this.parseState.push(new PropertyEntry(propertyName));
		try {
			if (bd.getPropertyValues().contains(propertyName)) {
				error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
				return;
			}
			Object val = parsePropertyValue(ele, bd, propertyName);
			PropertyValue pv = new PropertyValue(propertyName, val);
			pv.setSource(parserContext.extractSource(ele));
			bd.getPropertyValues().addPropertyValue(pv);
		}
		finally {
			this.parseState.pop();
		}
	}

	private Object parsePropertyValue(Element ele, BeanDefinition bd, String propertyName) {
		String elementName = (propertyName != null) ? "<property> element for property '" + propertyName + "'"
				: "<constructor-arg> element";

		// Should only have one child element: ref, value, list, etc.
		NodeList nl = ele.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element
					&& !DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT)) {
				// Child element is what we're looking for.
				if (subElement != null) {
					error(elementName + " must not contain more than one sub-element", ele);
				}
				else {
					subElement = (Element) node;
				}
			}
		}

		boolean hasRefAttribute = ele.hasAttribute(BeanDefinitionParserDelegate.REF_ATTRIBUTE);
		boolean hasValueAttribute = ele.hasAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE);
		if ((hasRefAttribute && hasValueAttribute) || ((hasRefAttribute || hasValueAttribute) && subElement != null)) {
			error(elementName
					+ " is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele);
		}

		if (hasRefAttribute) {
			String refName = ele.getAttribute(BeanDefinitionParserDelegate.REF_ATTRIBUTE);
			if (!StringUtils.hasText(refName)) {
				error(elementName + " contains empty 'ref' attribute", ele);
			}
			RuntimeBeanReference ref = new RuntimeBeanReference(refName);
			ref.setSource(parserContext.extractSource(ele));
			return ref;
		}
		else if (hasValueAttribute) {
			TypedStringValue valueHolder = new TypedStringValue(
				ele.getAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE));
			valueHolder.setSource(parserContext.extractSource(ele));
			return valueHolder;
		}
		else if (subElement != null) {
			return parsePropertySubElement(subElement, bd, null);
		}
		else {
			// Neither child element nor "ref" or "value" attribute found.
			error(elementName + " must specify a ref or value", ele);
			return null;
		}
	}

	public static Object parsePropertySubElement(ParserContext parserContext, Element ele, BeanDefinition bd) {
		return new ComponentParser(parserContext).parsePropertySubElement(ele, bd, null);
	}

	public static Map parsePropertyMapElement(ParserContext parserContext, Element ele, BeanDefinition bd) {
		return new ComponentParser(parserContext).parseMapElement(ele, bd);
	}

	public static Set parsePropertySetElement(ParserContext parserContext, Element ele, BeanDefinition bd) {
		return new ComponentParser(parserContext).parseSetElement(ele, bd);
	}

	/**
	 * Parse a value, ref or collection sub-element of a property or
	 * constructor-arg element. This method is called from several places to
	 * handle reusable elements such as idref, ref, null, value and so on.
	 * 
	 * In fact, this method is the main reason why the
	 * BeanDefinitionParserDelegate is not used in full since the element
	 * namespace becomes important as mixed rfc124/bean content can coexist.
	 * 
	 * @param ele subelement of property element; we don't know which yet
	 * @param defaultTypeClassName the default type (class name) for any
	 * <code>&lt;value&gt;</code> tag that might be created
	 */
	private Object parsePropertySubElement(Element ele, BeanDefinition bd, String defaultTypeClassName) {
		// skip other namespace
		String namespaceUri = ele.getNamespaceURI();

		// check Spring own namespace
		if (parserContext.getDelegate().isDefaultNamespace(namespaceUri)) {
			return parserContext.getDelegate().parsePropertySubElement(ele, bd);
		}
		// let the delegate handle other ns
		else if (!NAMESPACE_URI.equals(namespaceUri)) {
			return parserContext.getDelegate().parseCustomElement(ele);
		}

		// 
		else {
			if (DomUtils.nodeNameEquals(ele, COMPONENT)) {
				BeanDefinitionHolder bdHolder = parseComponentDefinitionElement(ele, bd);
				if (bdHolder != null) {
					bdHolder = ParsingUtils.decorateBeanDefinitionIfRequired(ele, bdHolder, parserContext);
				}
				return bdHolder;
			}

			if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.REF_ELEMENT)) {
				return parseRefElement(ele);
			}
			else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.IDREF_ELEMENT)) {
				return parseIdRefElement(ele);
			}
			else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.VALUE_ELEMENT)) {
				return parseValueElement(ele, defaultTypeClassName);
			}
			else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.NULL_ELEMENT)) {
				// It's a distinguished null value. Let's wrap it in a TypedStringValue
				// object in order to preserve the source location.
				TypedStringValue nullHolder = new TypedStringValue(null);
				nullHolder.setSource(parserContext.extractSource(ele));
				return nullHolder;
			}
			else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.LIST_ELEMENT)) {
				return parseListElement(ele, bd);
			}
			else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.SET_ELEMENT)) {
				return parseSetElement(ele, bd);
			}
			else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.MAP_ELEMENT)) {
				return parseMapElement(ele, bd);
			}
			else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.PROPS_ELEMENT)) {
				return parserContext.getDelegate().parsePropsElement(ele);
			}

			// maybe it's a nested service/reference/ref-list/ref-set
			return parserContext.getDelegate().parseCustomElement(ele, bd);
		}
	}

	private Object parseRefElement(Element ele) {
		// A generic reference to any name of any component.
		String refName = ele.getAttribute(COMPONENT);
		if (!StringUtils.hasLength(refName)) {
			error("'component' is required for <ref> element", ele);
			return null;
		}

		if (!StringUtils.hasText(refName)) {
			error("<ref> element contains empty target attribute", ele);
			return null;
		}
		RuntimeBeanReference ref = new RuntimeBeanReference(refName);
		ref.setSource(parserContext.extractSource(ele));
		return ref;
	}

	private Object parseIdRefElement(Element ele) {
		// A generic reference to any name of any bean/component.
		String refName = ele.getAttribute(COMPONENT);
		if (!StringUtils.hasLength(refName)) {
			error("'component' is required for <idref> element", ele);
			return null;
		}
		if (!StringUtils.hasText(refName)) {
			error("<idref> element contains empty target attribute", ele);
			return null;
		}
		RuntimeBeanNameReference ref = new RuntimeBeanNameReference(refName);
		ref.setSource(parserContext.extractSource(ele));
		return ref;
	}

	/**
	 * Return a typed String value Object for the given value element.
	 * 
	 * @param ele element
	 * @param defaultTypeClassName type class name
	 * @return typed String value Object
	 */
	private Object parseValueElement(Element ele, String defaultTypeClassName) {
		// It's a literal value.
		String value = DomUtils.getTextValue(ele);
		String typeClassName = ele.getAttribute(BeanDefinitionParserDelegate.TYPE_ATTRIBUTE);
		if (!StringUtils.hasText(typeClassName)) {
			typeClassName = defaultTypeClassName;
		}
		try {
			return buildTypedStringValue(value, typeClassName, ele);
		}
		catch (ClassNotFoundException ex) {
			error("Type class [" + typeClassName + "] not found for <value> element", ele, ex);
			return value;
		}
	}

	/**
	 * Build a typed String value Object for the given raw value.
	 * 
	 * @see org.springframework.beans.factory.config.TypedStringValue
	 */
	private Object buildTypedStringValue(String value, String targetTypeName, Element ele)
			throws ClassNotFoundException {

		ClassLoader classLoader = parserContext.getReaderContext().getBeanClassLoader();
		TypedStringValue typedValue = null;
		if (!StringUtils.hasText(targetTypeName)) {
			typedValue = new TypedStringValue(value);
		}
		else if (classLoader != null) {
			Class<?> targetType = ClassUtils.forName(targetTypeName, classLoader);
			typedValue = new TypedStringValue(value, targetType);
		}
		else {
			typedValue = new TypedStringValue(value, targetTypeName);
		}
		typedValue.setSource(parserContext.extractSource(ele));
		return typedValue;
	}

	/**
	 * Parse a list element.
	 */
	private List<?> parseListElement(Element collectionEle, BeanDefinition bd) {
		String defaultTypeClassName = collectionEle.getAttribute(BeanDefinitionParserDelegate.VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedList list = new TempManagedList(nl.getLength(), defaultTypeClassName);
		list.setSource(parserContext.extractSource(collectionEle));
		list.setMergeEnabled(parserContext.getDelegate().parseMergeAttribute(collectionEle));
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element
					&& !DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT)) {
				list.add(parsePropertySubElement((Element) node, bd, defaultTypeClassName));
			}
		}
		return list;
	}

	/**
	 * Parse a set element.
	 */
	private Set<?> parseSetElement(Element collectionEle, BeanDefinition bd) {
		String defaultTypeClassName = collectionEle.getAttribute(BeanDefinitionParserDelegate.VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedSet set = new TempManagedSet(nl.getLength(), defaultTypeClassName);
		set.setSource(parserContext.extractSource(collectionEle));
		set.setMergeEnabled(parserContext.getDelegate().parseMergeAttribute(collectionEle));
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element
					&& !DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT)) {
				set.add(parsePropertySubElement((Element) node, bd, defaultTypeClassName));
			}
		}
		return set;
	}

	/**
	 * Parse a map element.
	 */
	private Map<?, ?> parseMapElement(Element mapEle, BeanDefinition bd) {
		String defaultKeyTypeClassName = mapEle.getAttribute(BeanDefinitionParserDelegate.KEY_TYPE_ATTRIBUTE);
		String defaultValueTypeClassName = mapEle.getAttribute(BeanDefinitionParserDelegate.VALUE_TYPE_ATTRIBUTE);

		List<Element> entryEles = DomUtils.getChildElementsByTagName(mapEle, BeanDefinitionParserDelegate.ENTRY_ELEMENT);
		ManagedMap map = new TempManagedMap(entryEles.size(), defaultKeyTypeClassName, defaultValueTypeClassName);
		map.setMergeEnabled(parserContext.getDelegate().parseMergeAttribute(mapEle));
		map.setSource(parserContext.extractSource(mapEle));

		for (Element entryEle : entryEles) {
			// Should only have one value child element: ref, value, list, etc.
			// Optionally, there might be a key child element.
			NodeList entrySubNodes = entryEle.getChildNodes();

			Element keyEle = null;
			Element valueEle = null;
			for (int j = 0; j < entrySubNodes.getLength(); j++) {
				Node node = entrySubNodes.item(j);
				if (node instanceof Element) {
					Element candidateEle = (Element) node;
					if (DomUtils.nodeNameEquals(candidateEle, BeanDefinitionParserDelegate.KEY_ELEMENT)) {
						if (keyEle != null) {
							error("<entry> element is only allowed to contain one <key> sub-element", entryEle);
						}
						else {
							keyEle = candidateEle;
						}
					}
					else {
						// Child element is what we're looking for.
						if (valueEle != null) {
							error("<entry> element must not contain more than one value sub-element", entryEle);
						}
						else {
							valueEle = candidateEle;
						}
					}
				}
			}

			// Extract key from attribute or sub-element.
			Object key = null;
			boolean hasKeyAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.KEY_ATTRIBUTE);
			boolean hasKeyRefAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.KEY_REF_ATTRIBUTE);
			if ((hasKeyAttribute && hasKeyRefAttribute) || ((hasKeyAttribute || hasKeyRefAttribute)) && keyEle != null) {
				error("<entry> element is only allowed to contain either "
						+ "a 'key' attribute OR a 'key-ref' attribute OR a <key> sub-element", entryEle);
			}
			if (hasKeyAttribute) {
				key = buildTypedStringValueForMap(entryEle.getAttribute(BeanDefinitionParserDelegate.KEY_ATTRIBUTE),
					defaultKeyTypeClassName, entryEle);
			}
			else if (hasKeyRefAttribute) {
				String refName = entryEle.getAttribute(BeanDefinitionParserDelegate.KEY_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'key-ref' attribute", entryEle);
				}
				RuntimeBeanReference ref = new RuntimeBeanReference(refName);
				ref.setSource(parserContext.extractSource(entryEle));
				key = ref;
			}
			else if (keyEle != null) {
				key = parseKeyElement(keyEle, bd, defaultKeyTypeClassName);
			}
			else {
				error("<entry> element must specify a key", entryEle);
			}

			// Extract value from attribute or sub-element.
			Object value = null;
			boolean hasValueAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE);
			boolean hasValueRefAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.VALUE_REF_ATTRIBUTE);
			if ((hasValueAttribute && hasValueRefAttribute) || ((hasValueAttribute || hasValueRefAttribute))
					&& valueEle != null) {
				error("<entry> element is only allowed to contain either "
						+ "'value' attribute OR 'value-ref' attribute OR <value> sub-element", entryEle);
			}
			if (hasValueAttribute) {
				value = buildTypedStringValueForMap(
					entryEle.getAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE), defaultValueTypeClassName,
					entryEle);
			}
			else if (hasValueRefAttribute) {
				String refName = entryEle.getAttribute(BeanDefinitionParserDelegate.VALUE_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'value-ref' attribute", entryEle);
				}
				RuntimeBeanReference ref = new RuntimeBeanReference(refName);
				ref.setSource(parserContext.extractSource(entryEle));
				value = ref;
			}
			else if (valueEle != null) {
				value = parsePropertySubElement(valueEle, bd, defaultValueTypeClassName);
			}
			else {
				error("<entry> element must specify a value", entryEle);
			}

			// Add final key and value to the Map.
			map.put(key, value);
		}

		return map;
	}

	/**
	 * Build a typed String value Object for the given raw value.
	 * 
	 * @see org.springframework.beans.factory.config.TypedStringValue
	 */
	private Object buildTypedStringValueForMap(String value, String defaultTypeClassName, Element entryEle) {
		try {
			return buildTypedStringValue(value, defaultTypeClassName, entryEle);
		}
		catch (ClassNotFoundException ex) {
			error("Type class [" + defaultTypeClassName + "] not found for Map key/value type", entryEle, ex);
			return value;
		}
	}

	/**
	 * Parse a key sub-element of a map element.
	 */
	private Object parseKeyElement(Element keyEle, BeanDefinition bd, String defaultKeyTypeClassName) {
		NodeList nl = keyEle.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				// Child element is what we're looking for.
				if (subElement != null) {
					error("<key> element must not contain more than one value sub-element", keyEle);
				}
				else {
					subElement = (Element) node;
				}
			}
		}
		return parsePropertySubElement(subElement, bd, defaultKeyTypeClassName);
	}

	// util methods (used as shortcuts)
	private Object extractSource(Element ele) {
		return parserContext.extractSource(ele);
	}

	/**
	 * Reports an error with the given message for the given source element.
	 */
	private void error(String message, Node source) {
		parserContext.getReaderContext().error(message, source, parseState.snapshot());
	}

	/**
	 * Reports an error with the given message for the given source element.
	 */
	private void error(String message, Node source, Throwable cause) {
		parserContext.getReaderContext().error(message, source, parseState.snapshot(), cause);
	}

	/**
	 * Reports an error with the given exception for the given source element.
	 */
	private void error(Node source, Throwable cause) {
		parserContext.getReaderContext().error(cause.getLocalizedMessage(), source, parseState.snapshot(), cause);
	}
}