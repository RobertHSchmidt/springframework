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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.binding.method.MethodKey;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.binding.support.Mapping;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelector;
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
 *        &lt;!DOCTYPE flow PUBLIC &quot;-//SPRING//DTD WEBFLOW 1.0//EN&quot;
 *        &quot;http://www.springframework.org/dtd/spring-webflow-1.0.dtd&quot;&gt;
 * </pre>
 * 
 * Consult the <a
 * href="http://www.springframework.org/dtd/spring-webflow-1.0.dtd">web flow DTD</a>
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
 * <td>flowArtifactFactory</td>
 * <td><i>{@link FlowArtifactFactory}</i></td>
 * <td>Set the flow artifact factory facade to use to resolve externally
 * managed flow artifacts during the build process.</td>
 * </tr>
 * </table>
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class XmlFlowBuilder extends BaseFlowBuilder implements ResourceHolder {

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

	private static final String INLINE_FLOW_ELEMENT = "inline-flow";

	private static final String IMPORT_ELEMENT = "import";

	private static final String RESOURCE_ATTRIBUTE = "resource";

	private static final String INLINE_FLOW_MAP_PROPERTY = XmlFlowBuilder.class + ".inlineFlowRegistry";

	/**
	 * The resource location of the XML flow definition.
	 */
	private Resource location;

	/**
	 * A flow artifact factory specific to this builder that first looks in a
	 * locally-managed Spring application context for flow artifacts before
	 * searching an externally managed factory.
	 */
	private LocalFlowArtifactFactory flowArtifactFactory = new LocalFlowArtifactFactory();

	/**
	 * The resource loader strategy to use with any local flow application
	 * contexts created during the building process.
	 */
	private ResourceLoader resourceLoader;

	/**
	 * Flag indicating if the the XML document parser will perform DTD
	 * validation.
	 */
	private boolean validating = true;

	/**
	 * The spring-webflow DTD resolution strategy.
	 */
	private EntityResolver entityResolver = new WebFlowDtdResolver();

	/**
	 * The in-memory document object model (DOM) of the XML Document read from
	 * the flow definition resource.
	 */
	protected Document document;

	/**
	 * Creates a new XML flow builder that builds the flow contained within the
	 * provided XML document.
	 * @param location the location of the XML document resource
	 */
	public XmlFlowBuilder(Resource location) {
		setLocation(location);
	}

	/**
	 * Creates a new XML flow builder that builds the flow contained within the
	 * provided XML document. Sets a specific "flowArtifactFactory" to provide
	 * access to dependent, but externally managed flow artifacts (actions,
	 * etc).
	 * @param location the location of the XML document resource
	 * @param flowArtifactFactory the flow artifact factory to use for accessing
	 * externally managed flow artifacts
	 */
	public XmlFlowBuilder(Resource location, FlowArtifactFactory flowArtifactFactory) {
		setLocation(location);
		setFlowArtifactFactory(flowArtifactFactory);
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

	/*
	 * @see org.springframework.webflow.config.ResourceHolder#getResource()
	 */
	public Resource getResource() {
		return getLocation();
	}

	/*
	 * Overriden to return the local registry
	 * @see org.springframework.webflow.config.BaseFlowBuilder#getFlowArtifactFactory()
	 */
	protected FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	/**
	 * Returns the resource loader.
	 */
	protected ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * Sets the resource loader.
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
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

	public Flow init(String flowId, Map flowProperties) throws FlowBuilderException {
		Assert.notNull(location,
				"The location property specifying the XML flow definition resource location is required");
		Assert.notNull(getFlowArtifactFactory(),
				"The flowArtifactFactory property for loading actions and subflows is required");
		initConversionService();
		try {
			loadDocument();
		}
		catch (IOException e) {
			throw new FlowBuilderException("Cannot load the XML flow definition resource '" + location + "'", e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException("Cannot configure the parser to parse the XML flow definition", e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException("Cannot parse the flow definition XML document at'" + location + "'", e);
		}
		setFlow(parseFlowDefinition(flowId, flowProperties, document.getDocumentElement()));
		addInlineFlowDefinitions(getFlow(), document.getDocumentElement());
		initFlowArtifactRegistry(getFlow(), document.getDocumentElement());
		return getFlow();
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

	/**
	 * Parse the XML flow definitions and construct a Flow object. This helper
	 * method will set the "flow" property.
	 */
	protected Flow parseFlowDefinition(String id, Map flowProperties, Element element) {
		return getFlowCreator().createFlow(id, buildFlowProperties(flowProperties, element));
	}

	private Map buildFlowProperties(Map assignedProperties, Element element) {
		Map properties = parseProperties(element);
		if (assignedProperties != null) {
			if (properties != null) {
				properties.putAll(assignedProperties);
			}
			else {
				properties = new HashMap(assignedProperties);
			}
		}
		return properties;
	}

	/**
	 * Initialize a local artifact registry for the flow that is currently in
	 * the process of being built. Pushes the registry onto the stack so
	 * artifacts can be loaded during state construction.
	 * 
	 * @param flow the flow that is being constructed
	 * @param element the flow's root XML element
	 */
	protected void initFlowArtifactRegistry(Flow flow, Element element) {
		NodeList nodeList = element.getElementsByTagName(IMPORT_ELEMENT);
		Resource[] resources = new Resource[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element childElement = (Element)node;
				try {
					resources[i] = getLocation().createRelative(childElement.getAttribute(RESOURCE_ATTRIBUTE));
				}
				catch (IOException e) {
					throw new FlowBuilderException("Could not access flow-relative artifact resource '"
							+ childElement.getAttribute(RESOURCE_ATTRIBUTE) + "", e);
				}
			}
		}
		GenericApplicationContext context = new GenericApplicationContext();
		if (getResourceLoader() == null) {
			context.setResourceLoader(getResourceLoader());
		}
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(resources);
		flowArtifactFactory.push(new LocalFlowArtifactRegistry(flow, context));
	}

	/**
	 * Parse the inner flow definitions in the XML file and add them to the flow
	 * object we're constructing.
	 * @param flow the outer flow
	 * @param the outer flow element
	 */
	protected void addInlineFlowDefinitions(Flow flow, Element element) {
		NodeList nodeList = element.getElementsByTagName(INLINE_FLOW_ELEMENT);
		if (nodeList.getLength() == 0) {
			return;
		}
		Map inlineFlows = new HashMap(nodeList.getLength());
		flow.setAttribute(INLINE_FLOW_MAP_PROPERTY, inlineFlows);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element inlineElement = (Element)node;
				String inlineFlowId = inlineElement.getAttribute(ID_ATTRIBUTE);
				Element flowElement = (Element)inlineElement.getElementsByTagName(FLOW_ATTRIBUTE).item(0);
				Flow inlineFlow = parseFlowDefinition(inlineFlowId, null, flowElement);
				inlineFlows.put(inlineFlow.getId(), inlineFlow);
				buildInlineFlow(inlineFlow, flowElement);
			}
		}
	}

	/**
	 * Build the nested inline flow definition.
	 * 
	 * @param inlineFlow the inline flow to build
	 * @param element the "flow" element
	 */
	protected void buildInlineFlow(Flow inlineFlow, Element element) {
		addInlineFlowDefinitions(inlineFlow, element);
		initFlowArtifactRegistry(inlineFlow, element);
		addStateDefinitions(inlineFlow, element);
		inlineFlow.addExceptionHandlers(parseExceptionHandlers(element));
		inlineFlow.resolveStateTransitionsTargetStates();
		destroyFlowArtifactRegistry(inlineFlow);
		inlineFlow.removeAttribute(INLINE_FLOW_MAP_PROPERTY);
	}

	public void buildStates() throws FlowBuilderException {
		addStateDefinitions(getFlow(), document.getDocumentElement());
	}

	public void buildExceptionHandlers() throws FlowBuilderException {
		getFlow().addExceptionHandlers(parseExceptionHandlers(document.getDocumentElement()));
	}

	/**
	 * Parse the state definitions in the XML file and add them to the flow
	 * object we're constructing.
	 */
	protected void addStateDefinitions(Flow flow, Element element) {
		String startStateId = element.getAttribute(START_STATE_ATTRIBUTE);
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element childElement = (Element)node;
				State state = null;
				if (ACTION_STATE_ELEMENT.equals(childElement.getNodeName())) {
					state = parseActionState(flow, childElement);
				}
				else if (VIEW_STATE_ELEMENT.equals(childElement.getNodeName())) {
					state = parseViewState(flow, childElement);
				}
				else if (DECISION_STATE_ELEMENT.equals(childElement.getNodeName())) {
					state = parseDecisionState(flow, childElement);
				}
				else if (SUBFLOW_STATE_ELEMENT.equals(childElement.getNodeName())) {
					state = parseSubflowState(flow, childElement);
				}
				else if (END_STATE_ELEMENT.equals(childElement.getNodeName())) {
					state = parseEndState(flow, childElement);
				}
				if (state != null) {
					parseStateActions(childElement, state);
					state.addExceptionHandlers(parseExceptionHandlers(childElement));
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
		ViewSelector creator = null;
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			creator = (ViewSelector)fromStringTo(ViewSelector.class).execute(element.getAttribute(VIEW_ATTRIBUTE));
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
	protected SubflowState parseSubflowState(Flow flow, Element element) {
		return new SubflowState(flow, element.getAttribute(ID_ATTRIBUTE), getFlowArtifactFactory().getSubflow(
				element.getAttribute(FLOW_ATTRIBUTE)), parseAttributeMapper(element), parseTransitions(element),
				parseProperties(element));
	}

	/**
	 * Parse given end state definition and add a corresponding state to given
	 * flow.
	 */
	protected EndState parseEndState(Flow flow, Element element) {
		ViewSelector creator = null;
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			creator = (ViewSelector)fromStringTo(ViewSelector.class).execute(element.getAttribute(VIEW_ATTRIBUTE));
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
		return getFlowArtifactFactory().getAction(element.getAttribute(BEAN_ATTRIBUTE));
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
				return getFlowArtifactFactory().getAttributeMapper(mapperElement.getAttribute(BEAN_ATTRIBUTE));
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
	protected StateExceptionHandler[] parseExceptionHandlers(Element element) {
		NodeList nodeList = element.getElementsByTagName(EXCEPTION_HANDLER_ELEMENT);
		if (nodeList.getLength() == 0) {
			return null;
		}
		StateExceptionHandler[] exceptionHandlers = new StateExceptionHandler[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element handlerElement = (Element)node;
				if (handlerElement.hasAttribute(BEAN_ATTRIBUTE)) {
					exceptionHandlers[i] = getFlowArtifactFactory().getExceptionHandler(
							handlerElement.getAttribute(BEAN_ATTRIBUTE));
				}
				else {
					exceptionHandlers[i] = parseDefaultExceptionHandler(handlerElement);
				}
			}
		}
		return exceptionHandlers;
	}

	protected StateExceptionHandler parseDefaultExceptionHandler(Element element) {
		TransitionExecutingStateExceptionHandler defaultHandler = new TransitionExecutingStateExceptionHandler();
		Class exceptionClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(ON_ATTRIBUTE));
		State state = getFlow().getState(element.getAttribute(TO_ATTRIBUTE));
		defaultHandler.add(new ExceptionStateMapping(exceptionClass, state));
		return defaultHandler;
	}

	public void dispose() {
		destroyFlowArtifactRegistry(getFlow());
		getFlow().removeAttribute(INLINE_FLOW_MAP_PROPERTY);
		setFlow(null);
		document = null;
	}

	/**
	 * Pops the local registry off the stack for the flow definition that has
	 * just been constructed.
	 * @param flow the built flow
	 */
	protected void destroyFlowArtifactRegistry(Flow flow) {
		flowArtifactFactory.pop();
	}

	/**
	 * A local artifact factory that searches local registries first before
	 * querying the global artifact factory.
	 * @author Keith Donald
	 */
	private class LocalFlowArtifactFactory extends FlowArtifactFactoryAdapter {

		/**
		 * The stack of registries.
		 */
		private Stack localFlowArtifactRegistries;

		/**
		 * Push a new registry onto the stack
		 * @param registry the local registry
		 */
		public void push(LocalFlowArtifactRegistry registry) {
			initIfNecessary();
			if (!localFlowArtifactRegistries.isEmpty()) {
				registry.context.setParent(top().context);
			}
			localFlowArtifactRegistries.push(registry);
		}

		private void initIfNecessary() {
			if (localFlowArtifactRegistries == null) {
				localFlowArtifactRegistries = new Stack();
			}
		}

		/**
		 * Pop a registry off the stack
		 */
		public LocalFlowArtifactRegistry pop() {
			return (LocalFlowArtifactRegistry)localFlowArtifactRegistries.pop();
		}

		/**
		 * Returns the top registry on the stack
		 */
		public LocalFlowArtifactRegistry top() {
			return (LocalFlowArtifactRegistry)localFlowArtifactRegistries.peek();
		}

		public Flow getSubflow(String id) throws FlowArtifactException {
			Flow currentFlow = top().flow;
			// quick check for recursive subflow
			if (currentFlow.getId().equals(id)) {
				return currentFlow;
			}
			// check inline flows
			if (currentFlow.containsProperty(INLINE_FLOW_MAP_PROPERTY)) {
				Map registry = (Map)currentFlow.getProperty(INLINE_FLOW_MAP_PROPERTY);
				if (registry.containsKey(id)) {
					return (Flow)registry.get(id);
				}
			}
			// check other toplevel flows
			return XmlFlowBuilder.super.getFlowArtifactFactory().getSubflow(id);
		}

		public Action getAction(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return toAction(top().context.getBean(id));
				}
			}
			return XmlFlowBuilder.super.getFlowArtifactFactory().getAction(id);
		}

		public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (FlowAttributeMapper)top().context.getBean(id);
				}
			}
			return XmlFlowBuilder.super.getFlowArtifactFactory().getAttributeMapper(id);
		}

		public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (StateExceptionHandler)top().context.getBean(id);
				}
			}
			return XmlFlowBuilder.super.getFlowArtifactFactory().getExceptionHandler(id);
		}

		public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (TransitionCriteria)top().context.getBean(id);
				}
			}
			return XmlFlowBuilder.super.getFlowArtifactFactory().getTransitionCriteria(id);
		}

		public ViewSelector getViewSelector(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (ViewSelector)top().context.getBean(id);
				}
			}
			return XmlFlowBuilder.super.getFlowArtifactFactory().getViewSelector(id);
		}
	}

	/**
	 * Simple value object that holds a reference to a local artifact registry
	 * of a flow definition that is in the process of being constructed.
	 * @author Keith Donald
	 */
	private static class LocalFlowArtifactRegistry {

		private Flow flow;

		/**
		 * The local registry holding the artifacts local to the flow.
		 */
		private ConfigurableApplicationContext context;

		/**
		 * Create new registry
		 * @param flow the flow
		 * @param registry the local registry
		 */
		public LocalFlowArtifactRegistry(Flow flow, ConfigurableApplicationContext registry) {
			this.flow = flow;
			this.context = registry;
		}
	}
}