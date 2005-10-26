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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.binding.method.MethodKey;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.binding.support.Mapping;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.action.CompositeAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Flow builder that builds a flow definition from a XML file. XML files read by
 * this class should use the following doctype:
 * 
 * <pre>
 *            &lt;!DOCTYPE webflow PUBLIC &quot;-//SPRING//DTD WEBFLOW//EN&quot;
 *            &quot;http://www.springframework.org/dtd/spring-webflow.dtd&quot;&gt;
 * </pre>
 * 
 * Consult the <a
 * href="http://www.springframework.org/dtd/spring-webflow.dtd">web flow DTD</a>
 * for more information on the XML flow definition format.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name </b></td>
 * <td><b>default </b></td>
 * <td><b>description </b></td>
 * </tr>
 * <tr>
 * <td>location</td>
 * <td><i>null</i></td>
 * <td>Specifies the resource location from which the XML-based flow definition
 * is loaded. This "input stream source" is a required property.</td>
 * </tr>
 * <tr>
 * <td>validating</td>
 * <td><i>true</i></td>
 * <td>Set if the XML parser should validate the document and thus enforce a
 * DTD.</td>
 * </tr>
 * <tr>
 * <td>entityResolver</td>
 * <td><i>{@link WebFlowDtdResolver}</i></td>
 * <td>Set a SAX entity resolver to be used for parsing.</td>
 * </tr>
 * <tr>
 * <td>flowArtifactLocator</td>
 * <td><i>{@link FlowArtifactLocator}</i></td>
 * <td>Set the flow artifact location strategy to use to resolve externally
 * managed flow artifacts during the build process.</td>
 * </tr>
 * </table>
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class XmlFlowBuilder extends BaseFlowBuilder {

	// recognized XML elements and attributes

	private static final String ID_ATTRIBUTE = "id";

	private static final String START_STATE_ATTRIBUTE = "start-state";

	private static final String ACTION_STATE_ELEMENT = "action-state";

	private static final String ACTION_ELEMENT = "action";

	private static final String BEAN_ATTRIBUTE = "bean";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String METHOD_ATTRIBUTE = "method";

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String VIEW_STATE_ELEMENT = "view-state";

	private static final String VIEW_ATTRIBUTE = "view";

	private static final String DECISION_STATE_ELEMENT = "decision-state";

	private static final String IF_ELEMENT = "if";

	private static final String TEST_ATTRIBUTE = "test";

	private static final String THEN_ATTRIBUTE = "then";

	private static final String ELSE_ATTRIBUTE = "else";

	private static final String SUBFLOW_STATE_ELEMENT = "subflow-state";

	private static final String FLOW_ATTRIBUTE = "flow";

	private static final String ATTRIBUTE_MAPPER_ELEMENT = "attribute-mapper";

	private static final String INPUT_ELEMENT = "input";

	private static final String OUTPUT_ELEMENT = "output";

	private static final String AS_ATTRIBUTE = "as";

	private static final String END_STATE_ELEMENT = "end-state";

	private static final String TRANSITION_ELEMENT = "transition";

	private static final String ON_ATTRIBUTE = "on";

	private static final String TO_ATTRIBUTE = "to";

	private static final String FROM_ATTRIBUTE = "from";

	private static final String PROPERTY_ELEMENT = "property";

	private static final String VALUE_ELEMENT = "value";

	private static final String ENTRY_ACTION_ELEMENT = "entry-action";

	private static final String EXIT_ACTION_ELEMENT = "exit-action";

	private static final String EXCEPTION_HANDLER_ELEMENT = "exception-handler";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String STATE_ATTRIBUTE = "state";

	/**
	 * The resource location of the XML flow definition
	 */
	private Resource location;

	/**
	 * Flag indicating if the the XML document parser will perform DTD
	 * validation
	 */
	private boolean validating = true;

	/**
	 * The webflow DTD resolution strategy
	 */
	private EntityResolver entityResolver = new WebFlowDtdResolver();

	/**
	 * A local Spring managed bean registry for Flow definition scoped beans
	 * (for example, for managing actions, subflows, or other artifact local to
	 * the flow built by this builder).
	 */
	private DefaultListableBeanFactory localArtifactRegistry = new DefaultListableBeanFactory();

	/**
	 * The in-memory DOM of the XML Document loaded from the flow definition
	 * resource
	 */
	protected Document document;

	/**
	 * Creates a new XML flow builder.
	 * @param location resource to read the XML flow definition from
	 */
	public XmlFlowBuilder(Resource location) {
		setLocation(location);
	}

	/**
	 * Creates a new XML flow builder.
	 * @param location resource to read the XML flow definition from
	 * @param artifactLocator the flow artifact location strategy to use
	 */
	public XmlFlowBuilder(Resource location, FlowArtifactLocator artifactLocator) {
		setLocation(location);
		setFlowArtifactLocator(artifactLocator);
	}

	/**
	 * Returns the XML resource from which the flow definition is read.
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * Set the resource from which the XML flow definition will be read.
	 */
	public void setLocation(Resource location) {
		this.location = location;
	}

	/**
	 * Returns whether or not the XML parser will validate the document.
	 */
	public boolean isValidating() {
		return validating;
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a
	 * DTD. Defaults to true.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Returns the SAX entity resolver used by the XML parser.
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default,
	 * WebFlowDtdResolver will be used. Can be overridden for custom entity
	 * resolution, for example relative to some specific base path.
	 * 
	 * @see org.springframework.webflow.config.WebFlowDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/*
	 * Overriden to hook in this XML builder's local artifact registry as a
	 * child of the parent registry.
	 * @see org.springframework.webflow.config.BaseFlowBuilder#setFlowArtifactLocator(org.springframework.webflow.config.FlowArtifactLocator)
	 */
	public void setFlowArtifactLocator(FlowArtifactLocator artifactLocator) {
		FlowArtifactLocator localLocator = new BeanFactoryFlowArtifactLocator(localArtifactRegistry);
		if (artifactLocator != null) {
			super.setFlowArtifactLocator(new CompositeFlowArtifactLocator(new FlowArtifactLocator[] { localLocator,
					artifactLocator }));
		}
		else {
			super.setFlowArtifactLocator(localLocator);
		}
	}

	public Flow init() throws FlowBuilderException {
		Assert.notNull(location,
				"The location property specifying the XML flow definition resource location is required");
		Assert.notNull(getFlowArtifactLocator(),
				"The flowArtifactLocator property for loading actions and subflows is required");
		initConversionService();
		try {
			loadDocument();
		}
		catch (IOException e) {
			throw new FlowBuilderException("Cannot load the XML flow definition resource '" + location + "'", e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException("Cannot configure parser to parse the XML flow definition", e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException("Cannot parse the flow definition XML document '" + location + "'", e);
		}
		parseFlowDefinition();
		loadFlowBeanDefinitions();
		return getFlow();
	}

	public void buildStates() throws FlowBuilderException {
		parseStateDefinitions();
		parseExceptionHandlers();
	}

	public void dispose() {
		setFlow(null);
		document = null;
	}

	/**
	 * Load the flow definition from the configured resource. This helper method
	 * initializes the {@link #document} member variable.
	 */
	protected void loadDocument() throws IOException, ParserConfigurationException, SAXException {
		InputStream is = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(isValidating());
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new SimpleSaxErrorHandler(logger));
			docBuilder.setEntityResolver(getEntityResolver());
			is = location.getInputStream();
			document = docBuilder.parse(is);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ex) {
					logger.warn("Could not close InputStream", ex);
				}
			}
		}
	}

	protected void loadFlowBeanDefinitions() {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.localArtifactRegistry);
		reader.registerBeanDefinitions(this.document, getLocation());
	}

	// XML parsing logic

	/**
	 * Parse the XML flow definitions and construct a Flow object. This helper
	 * method will set the "flow" property.
	 */
	protected void parseFlowDefinition() {
		Element root = document.getDocumentElement();
		setFlow(getFlowCreator().createFlow(root.getAttribute(ID_ATTRIBUTE), parseProperties(root)));
	}

	/**
	 * Parse the state definitions in the XML file and add them to the flow
	 * object we're constructing.
	 */
	protected void parseStateDefinitions() {
		Element root = document.getDocumentElement();
		String startStateId = root.getAttribute(START_STATE_ATTRIBUTE);
		// get the flow under construction
		Flow flow = getFlow();
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element element = (Element)node;
				State state = null;
				if (ACTION_STATE_ELEMENT.equals(element.getNodeName())) {
					state = parseActionState(flow, element);
				}
				else if (VIEW_STATE_ELEMENT.equals(element.getNodeName())) {
					state = parseViewState(flow, element);
				}
				else if (DECISION_STATE_ELEMENT.equals(element.getNodeName())) {
					state = parseDecisionState(flow, element);
				}
				else if (SUBFLOW_STATE_ELEMENT.equals(element.getNodeName())) {
					state = parseSubFlowState(flow, element);
				}
				else if (END_STATE_ELEMENT.equals(element.getNodeName())) {
					state = parseEndState(flow, element);
				}
				if (state != null) {
					parseStateActions(element, state);
				}
			}
		}
		flow.setStartState(startStateId);
	}

	protected void parseStateActions(Element element, State state) {
		// parse any state entry actions
		List entryElements = DomUtils.getChildElementsByTagName(element, ENTRY_ACTION_ELEMENT);
		if (!entryElements.isEmpty()) {
			Element entryElement = (Element)entryElements.get(0);
			state.setEntryAction(new CompositeAction(parseAnnotatedActions(entryElement)));
		}
		if (state instanceof TransitionableState) {
			// parse any state exit actions
			List exitElements = DomUtils.getChildElementsByTagName(element, EXIT_ACTION_ELEMENT);
			if (!exitElements.isEmpty()) {
				Element exitElement = (Element)exitElements.get(0);
				((TransitionableState)state).setExitAction(new CompositeAction(parseAnnotatedActions(exitElement)));
			}
		}
	}

	/**
	 * Parse given action state definition and add a corresponding state to
	 * given flow.
	 */
	protected ActionState parseActionState(Flow flow, Element element) {
		return new ActionState(flow, element.getAttribute(ID_ATTRIBUTE), parseAnnotatedActions(element),
				parseTransitions(element), parseProperties(element));
	}

	/**
	 * Parse given view state definition and add a corresponding state to given
	 * flow.
	 */
	protected ViewState parseViewState(Flow flow, Element element) {
		ViewDescriptorCreator creator = null;
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			creator = (ViewDescriptorCreator)fromStringTo(ViewDescriptorCreator.class).execute(
					element.getAttribute(VIEW_ATTRIBUTE));
		}
		return new ViewState(flow, element.getAttribute(ID_ATTRIBUTE), creator, parseTransitions(element),
				parseProperties(element));
	}

	/**
	 * Parse given decision state definition and add a corresponding state to
	 * given flow.
	 */
	protected DecisionState parseDecisionState(Flow flow, Element element) {
		return new DecisionState(flow, element.getAttribute(ID_ATTRIBUTE), parseIfs(element), parseProperties(element));
	}

	/**
	 * Parse given sub flow state definition and add a corresponding state to
	 * given flow.
	 */
	protected SubflowState parseSubFlowState(Flow flow, Element element) {
		return new SubflowState(flow, element.getAttribute(ID_ATTRIBUTE), getFlowArtifactLocator().getSubflow(
				element.getAttribute(FLOW_ATTRIBUTE)), parseAttributeMapper(element), parseTransitions(element),
				parseProperties(element));
	}

	/**
	 * Parse given end state definition and add a corresponding state to given
	 * flow.
	 */
	protected EndState parseEndState(Flow flow, Element element) {
		ViewDescriptorCreator creator = null;
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			creator = (ViewDescriptorCreator)fromStringTo(ViewDescriptorCreator.class).execute(
					element.getAttribute(VIEW_ATTRIBUTE));
		}
		return new EndState(flow, element.getAttribute(ID_ATTRIBUTE), creator, parseProperties(element));
	}

	/**
	 * Parse all annotated action definitions contained in given element.
	 */
	protected AnnotatedAction[] parseAnnotatedActions(Element element) {
		List actions = new LinkedList();
		List actionElements = DomUtils.getChildElementsByTagName(element, ACTION_ELEMENT);
		Iterator it = actionElements.iterator();
		while (it.hasNext()) {
			actions.add(parseAnnotatedAction((Element)it.next()));
		}
		return (AnnotatedAction[])actions.toArray(new AnnotatedAction[actions.size()]);
	}

	/**
	 * Parse an annotated action definition and return the corresponding object.
	 */
	protected AnnotatedAction parseAnnotatedAction(Element element) {
		AnnotatedAction action = new AnnotatedAction((Action)parseAction(element));
		if (element.hasAttribute(NAME_ATTRIBUTE)) {
			action.setName(element.getAttribute(NAME_ATTRIBUTE));
		}
		if (element.hasAttribute(METHOD_ATTRIBUTE)) {
			// direct support for bean invoking actions
			MethodKey methodKey = (MethodKey)fromStringTo(MethodKey.class).execute(
					element.getAttribute(METHOD_ATTRIBUTE));
			action.setProperty(AnnotatedAction.METHOD_PROPERTY, methodKey);
		}
		parseAndSetProperties(element, action);
		return action;
	}

	/**
	 * Parse an action definition and return the corresponding object.
	 */
	protected Action parseAction(Element element) throws FlowBuilderException {
		return getFlowArtifactLocator().getAction(element.getAttribute(BEAN_ATTRIBUTE));
	}

	/**
	 * Parse all properties defined as nested elements of given element. Returns
	 * the properties as a map: the name of the property is the key, the
	 * associated value the value.
	 */
	protected Map parseProperties(Element element) {
		MapAttributeSource properties = new MapAttributeSource();
		parseAndSetProperties(element, properties);
		return properties.getAttributeMap();
	}

	/**
	 * Parse all properties defined as nested elements of given element and add
	 * them to given set of properties.
	 */
	protected void parseAndSetProperties(Element element, MutableAttributeSource properties) {
		List propertyElements = DomUtils.getChildElementsByTagName(element, PROPERTY_ELEMENT);
		for (int i = 0; i < propertyElements.size(); i++) {
			parseAndSetProperty((Element)propertyElements.get(i), properties);
		}
	}

	/**
	 * Parse a property definition from given element and add the property to
	 * given set.
	 */
	protected void parseAndSetProperty(Element element, MutableAttributeSource properties) {
		String name = element.getAttribute(NAME_ATTRIBUTE);
		String value = null;
		if (element.hasAttribute(VALUE_ATTRIBUTE)) {
			value = element.getAttribute(VALUE_ATTRIBUTE);
		}
		else {
			List valueElements = DomUtils.getChildElementsByTagName(element, VALUE_ELEMENT);
			Assert.state(valueElements.size() == 1, "A property value should be specified for property '" + name + "'");
			value = DomUtils.getTextValue((Element)valueElements.get(0));
		}
		properties.setAttribute(name, convertPropertyValue(element, value));
	}

	/**
	 * Do type conversion for given property value.
	 */
	protected Object convertPropertyValue(Element element, String stringValue) {
		if (element.hasAttribute(TYPE_ATTRIBUTE)) {
			ConversionExecutor executor = fromStringToAliased(element.getAttribute(TYPE_ATTRIBUTE));
			if (executor != null) {
				// convert string value to instance of aliased type
				return executor.execute(stringValue);
			}
			else {
				Class targetClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(TYPE_ATTRIBUTE));
				// convert string value to instance of target class
				return fromStringTo(targetClass).execute(stringValue);
			}
		}
		else {
			return stringValue;
		}
	}

	/**
	 * Find all transition definitions in given state definition and return a
	 * list of corresponding Transition objects.
	 */
	protected Transition[] parseTransitions(Element element) {
		List transitions = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, TRANSITION_ELEMENT);
		for (int i = 0; i < transitionElements.size(); i++) {
			transitions.add(parseTransition((Element)transitionElements.get(i)));
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Parse a transition definition and return a corresponding Transition
	 * object.
	 */
	protected Transition parseTransition(Element element) {
		TransitionCriteria matchingCriteria = (TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(
				element.getAttribute(ON_ATTRIBUTE));
		TransitionCriteria executionCriteria = TransitionCriteriaChain.criteriaChainFor(parseAnnotatedActions(element));
		return new Transition(matchingCriteria, executionCriteria, element.getAttribute(TO_ATTRIBUTE),
				parseProperties(element));
	}

	/**
	 * Find all "if" definitions in given state definition and return a list of
	 * corresponding Transition objects.
	 */
	protected Transition[] parseIfs(Element element) {
		List transitions = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, IF_ELEMENT);
		Iterator it = transitionElements.iterator();
		while (it.hasNext()) {
			transitions.addAll(Arrays.asList(parseIf((Element)it.next())));
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Parse an "if" transition definition and return a corresponding Transition
	 * object.
	 */
	protected Transition[] parseIf(Element element) {
		TransitionCriteria criteria = (TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(
				element.getAttribute(TEST_ATTRIBUTE));
		String trueStateId = element.getAttribute(THEN_ATTRIBUTE);
		Transition thenTransition = new Transition(criteria, trueStateId);
		String falseStateId = element.getAttribute(ELSE_ATTRIBUTE);
		if (StringUtils.hasText(falseStateId)) {
			Transition elseTransition = new Transition(falseStateId);
			return new Transition[] { thenTransition, elseTransition };
		}
		else {
			return new Transition[] { thenTransition };
		}
	}

	/**
	 * Obtain an attribute mapper reference from given sub flow definition
	 * element and return the identified mapper, or null if no mapper is
	 * referenced.
	 */
	protected FlowAttributeMapper parseAttributeMapper(Element element) {
		List mapperElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE_MAPPER_ELEMENT);
		if (mapperElements.isEmpty()) {
			return null;
		}
		else {
			Element mapperElement = (Element)mapperElements.get(0);
			if (StringUtils.hasText(mapperElement.getAttribute(BEAN_ATTRIBUTE))) {
				return getFlowArtifactLocator().getAttributeMapper(mapperElement.getAttribute(BEAN_ATTRIBUTE));
			}
			else {
				// inline definition of a mapping
				ParameterizableFlowAttributeMapper attributeMapper = new ParameterizableFlowAttributeMapper();
				List inputElements = DomUtils.getChildElementsByTagName(mapperElement, INPUT_ELEMENT);
				List inputMappings = new ArrayList(inputElements.size());
				for (Iterator it = inputElements.iterator(); it.hasNext();) {
					parseAndAddMapping((Element)it.next(), inputMappings);
				}
				attributeMapper.setInputMappings(inputMappings);
				List outputElements = DomUtils.getChildElementsByTagName(mapperElement, OUTPUT_ELEMENT);
				List outputMappings = new ArrayList(outputElements.size());
				for (Iterator it = outputElements.iterator(); it.hasNext();) {
					parseAndAddMapping((Element)it.next(), outputMappings);
				}
				attributeMapper.setOutputMappings(outputMappings);
				return attributeMapper;
			}
		}
	}

	/**
	 * Parse a single inline attribute mapping definition and add it to given
	 * map.
	 */
	protected void parseAndAddMapping(Element element, List mappings) {
		String name = element.getAttribute(NAME_ATTRIBUTE);
		String value = element.getAttribute(VALUE_ATTRIBUTE);
		String as = element.getAttribute(AS_ATTRIBUTE);
		String from = element.getAttribute(FROM_ATTRIBUTE);
		String to = element.getAttribute(TO_ATTRIBUTE);
		ConversionExecutor valueConverter = null;
		if (StringUtils.hasText(from)) {
			if (StringUtils.hasText(to)) {
				valueConverter = getConversionService().getConversionExecutor(
						getConversionService().getClassByAlias(from), getConversionService().getClassByAlias(to));
			}
		}
		if (StringUtils.hasText(name)) {
			// "name" allows you to specify the name of an attribute to map
			if (StringUtils.hasText(as)) {
				mappings.add(new Mapping(new FlowScopeExpression(name), ExpressionFactory.parsePropertyExpression(as),
						valueConverter));
			}
			else {
				mappings.add(new Mapping(new FlowScopeExpression(name),
						ExpressionFactory.parsePropertyExpression(name), valueConverter));
			}
		}
		else if (StringUtils.hasText(value)) {
			// "value" allows you to specify the value that should get mapped
			// using an expression
			Assert.hasText(as, "The 'as' attribute is required with the 'value' attribute");
			mappings.add(new Mapping(ExpressionFactory.parseExpression(value), ExpressionFactory
					.parsePropertyExpression(as), valueConverter));
		}
		else {
			throw new FlowBuilderException(this, "Name or value is required in a mapping definition: " + element);
		}
	}

	/**
	 * Parse the list of exception handlers present in the xml document and add
	 * them to the flow definition being built.
	 */
	protected void parseExceptionHandlers() {
		Element root = document.getDocumentElement();
		NodeList nodeList = root.getElementsByTagName(EXCEPTION_HANDLER_ELEMENT);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			StateMapperFlowExceptionHandler defaultHandler = null;
			if (node instanceof Element) {
				Element element = (Element)node;
				if (element.hasAttribute(BEAN_ATTRIBUTE)) {
					getFlowArtifactLocator().getExceptionHandler(element.getAttribute(BEAN_ATTRIBUTE));
				}
				else {
					if (defaultHandler == null) {
						defaultHandler = new StateMapperFlowExceptionHandler();
					}
					Class exceptionClass = (Class)fromStringTo(Class.class).execute(
							element.getAttribute(CLASS_ATTRIBUTE));
					State state = getFlow().getState(element.getAttribute(STATE_ATTRIBUTE));
					defaultHandler.add(new ExceptionStateMapping(exceptionClass, state));
				}
			}
			if (defaultHandler != null) {
				getFlow().addExceptionHandler(defaultHandler);
			}
		}
	}

}