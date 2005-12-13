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
package org.springframework.webflow.builder;

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
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.Transition.TargetStateResolver;
import org.springframework.webflow.action.FlowVariableCreatingAction;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.support.FlowScopeExpression;
import org.springframework.webflow.support.FlowVariable;
import org.springframework.webflow.support.ParameterizableFlowAttributeMapper;
import org.springframework.webflow.support.TransitionCriteriaChain;
import org.springframework.webflow.support.TransitionExecutingStateExceptionHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Flow builder that builds flows as defined in an XML document object model
 * (DOM) element. The element is supposed to be read from an XML file that uses
 * the following doctype:
 * 
 * <pre>
 *          &lt;!DOCTYPE flow PUBLIC &quot;-//SPRING//DTD WEBFLOW 1.0//EN&quot;
 *          &quot;http://www.springframework.org/dtd/spring-webflow-1.0.dtd&quot;&gt;
 * </pre>
 * 
 * <p>
 * Consult the <a
 * href="http://www.springframework.org/dtd/spring-webflow-1.0.dtd">web flow DTD</a>
 * for more information on the XML flow definition format.
 * <p>
 * This builder will setup a flow-local bean factory for the flow being
 * constructed. That flow-local bean factory will be populated with XML bean
 * definitions contained in files referenced using the "import" element. The
 * flow-local bean factory will use the bean factory defing this flow builder as
 * a parent. As such, the flow can access artifacts in either its flow-local
 * bean factory or in the parent bean factory hierarchy, e.g. the bean factory
 * of the dispatcher.
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
 * </table>
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class XmlFlowBuilder extends BaseFlowBuilder implements ResourceHolder {

	// recognized XML elements and attributes

	private static final String ID_ATTRIBUTE = "id";

	private static final String BEAN_ATTRIBUTE = "bean";

	private static final String FLOW_ELEMENT = "flow";

	private static final String START_STATE_ATTRIBUTE = "start-state";

	private static final String ACTION_STATE_ELEMENT = "action-state";

	private static final String ACTION_ELEMENT = "action";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String METHOD_ATTRIBUTE = "method";

	private static final String RESULT_NAME_ATTRIBUTE = "resultName";

	private static final String RESULT_SCOPE_ATTRIBUTE = "resultScope";

	private static final String NONE_VALUE = "none";

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

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String VAR_ELEMENT = "var";

	private static final String START_ACTIONS_ELEMENT = "start-actions";

	private static final String END_ACTIONS_ELEMENT = "end-actions";

	private static final String ENTRY_ACTIONS_ELEMENT = "entry-actions";

	private static final String EXIT_ACTIONS_ELEMENT = "exit-actions";

	private static final String EXCEPTION_HANDLER_ELEMENT = "exception-handler";

	private static final String INLINE_FLOW_ELEMENT = "inline-flow";

	private static final String IMPORT_ELEMENT = "import";

	private static final String RESOURCE_ATTRIBUTE = "resource";

	/**
	 * The resource from which the document element being parsed was read. Used
	 * as a location for relative resource lookup.
	 */
	protected Resource location;

	/**
	 * A flow artifact factory specific to this builder that first looks in a
	 * locally-managed Spring application context for flow artifacts before
	 * searching an externally managed factory.
	 */
	private LocalFlowArtifactFactory localFlowArtifactFactory = new LocalFlowArtifactFactory();

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
	private Document document;

	/**
	 * Create a new XML flow builder parsing the document at the specified
	 * location.
	 * @param artifactFactory the bean factory defining this flow builder
	 * @param documentElement the document element to parse
	 */
	public XmlFlowBuilder(Resource location) {
		super();
		setLocation(location);
	}

	/**
	 * Create a new XML flow builder parsing the document at the specified
	 * location, using the provided factory to access externally managed flow
	 * artifacts.
	 * @param artifactFactory the bean factory defining this flow builder
	 * @param documentElement the document element to parse
	 */
	public XmlFlowBuilder(Resource location, FlowArtifactFactory artifactFactory) {
		super(artifactFactory);
		setLocation(location);
	}

	/**
	 * Returns the resource from which the document element was loaded. This is
	 * used for location relative loading of other resources.
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * Sets the resource from which the document element was loaded. This is
	 * used for location relative loading of other resources.
	 */
	public void setLocation(Resource location) {
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.webflow.builder.ResourceHolder#getResource()
	 */
	public Resource getResource() {
		return location;
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
	 * @see org.springframework.webflow.builder.WebFlowDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public void init(String flowId, Map flowProperties) throws FlowBuilderException {
		Assert.notNull(getLocation(),
				"The location property specifying the XML flow definition resource location is required");
		try {
			this.document = loadDocument();
			Assert.notNull(document, "Document should never be null");
		}
		catch (IOException e) {
			throw new FlowBuilderException(this, "Could not load the XML flow definition resource at '" + getLocation()
					+ "'", e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException(this, "Could not configure the parser to parse the XML flow definition", e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException(this, "Could not parse the flow definition XML document at '"
					+ getLocation() + "'", e);
		}
		initConversionService();
		setFlow(parseFlow(flowId, flowProperties, getDocumentElement()));
		addInlineFlowDefinitions(getFlow(), getDocumentElement());
	}

	/**
	 * Load the flow definition from the configured resource and return the
	 * resulting DOM document.
	 */
	protected Document loadDocument() throws IOException, ParserConfigurationException, SAXException {
		InputStream is = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(isValidating());
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new SimpleSaxErrorHandler(logger));
			docBuilder.setEntityResolver(getEntityResolver());
			is = getLocation().getInputStream();
			return docBuilder.parse(is);
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
	 * Returns the document.
	 */
	protected Document getDocument() {
		return document;
	}

	/**
	 * Returns the root document element.
	 */
	protected Element getDocumentElement() {
		return document.getDocumentElement();
	}

	/**
	 * Returns the flow artifact factory local to this builder.
	 */
	protected FlowArtifactFactory getLocalFlowArtifactFactory() {
		return localFlowArtifactFactory;
	}

	/**
	 * Parse the XML flow definitions and construct a Flow object. Will not
	 * parse all state definitions for the flow!
	 */
	protected Flow parseFlow(String id, Map properties, Element flowElement) {
		Assert.state(FLOW_ELEMENT.equals(flowElement.getTagName()), "This is not the '" + FLOW_ELEMENT + "' element");
		initLocalFlowArtifactFactoryRegistry(flowElement);
		Flow flow = (Flow)getLocalFlowArtifactFactory().createFlow(id, buildProperties(properties, flowElement));
		parseAndAddFlowVariables(flowElement, flow);
		parseAndAddFlowActions(flowElement, flow);
		return flow;
	}

	/**
	 * Initialize a local flow artifact registry to access the flow local bean
	 * factory.
	 */
	protected void initLocalFlowArtifactFactoryRegistry(Element flowElement) {
		List importElements = DomUtils.getChildElementsByTagName(flowElement, IMPORT_ELEMENT);
		Resource[] resources = new Resource[importElements.size()];
		for (int i = 0; i < importElements.size(); i++) {
			Element importElement = (Element)importElements.get(i);
			try {
				resources[i] = getLocation().createRelative(importElement.getAttribute(RESOURCE_ATTRIBUTE));
			}
			catch (IOException e) {
				throw new FlowBuilderException(this, "Could not access flow-relative artifact resource '"
						+ importElement.getAttribute(RESOURCE_ATTRIBUTE) + "'", e);
			}
		}
		GenericApplicationContext context = new GenericApplicationContext();
		setResourceLoaderIfSupported(context);
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(resources);
		localFlowArtifactFactory.push(new LocalFlowArtifactRegistry(context));
	}

	/**
	 * Sets a context relative resource loader if supported by the configured
	 * FlowArtifactFactory.
	 * @param context the resource loading context
	 */
	protected void setResourceLoaderIfSupported(GenericApplicationContext context) {
		try {
			if (getFlowArtifactFactory().getResourceLoader() == null) {
				// for context relative resource loading
				context.setResourceLoader(getFlowArtifactFactory().getResourceLoader());
			}
		}
		catch (UnsupportedOperationException e) {

		}
	}

	/**
	 * Build the flow property map.
	 * @param properties the initial set of assigned properties
	 * @param flowElement the flow element that may define additional properties
	 * @return the flow property map
	 */
	protected Map buildProperties(Map properties, Element flowElement) {
		Map flowProperties = parseProperties(flowElement);
		if (properties != null) {
			if (flowProperties != null) {
				flowProperties.putAll(properties);
			}
			else {
				flowProperties = new HashMap(properties);
			}
		}
		return flowProperties;
	}

	/**
	 * Parse a list of flow variables to create when the flow starts.
	 * @param flowElement the flow element
	 * @param flow the flow
	 */
	protected void parseAndAddFlowVariables(Element flowElement, Flow flow) {
		List varElements = DomUtils.getChildElementsByTagName(flowElement, VAR_ELEMENT);
		if (varElements.isEmpty()) {
			return;
		}
		FlowVariableCreatingAction variableCreator = new FlowVariableCreatingAction();
		for (int i = 0; i < varElements.size(); i++) {
			variableCreator.addVariable(parseVariable((Element)varElements.get(i)));
		}
		flow.getStartActionList().add(variableCreator);
	}

	/**
	 * Parse the flow variable.
	 * @param element the var element
	 * @return the flow variable
	 */
	protected FlowVariable parseVariable(Element element) {
		Class type = (Class)fromStringTo(Class.class).execute(element.getAttribute(TYPE_ATTRIBUTE));
		return new FlowVariable(element.getAttribute(NAME_ATTRIBUTE), type);
	}

	/**
	 * Parse all state entry and exit actions defined in given element and add
	 * them to given state.
	 */
	protected void parseAndAddFlowActions(Element element, Flow flow) {
		List startElements = DomUtils.getChildElementsByTagName(element, START_ACTIONS_ELEMENT);
		if (!startElements.isEmpty()) {
			Element startElement = (Element)startElements.get(0);
			flow.getStartActionList().addAll(parseAnnotatedActions(startElement));
		}
		List endElements = DomUtils.getChildElementsByTagName(element, END_ACTIONS_ELEMENT);
		if (!endElements.isEmpty()) {
			Element endElement = (Element)endElements.get(0);
			flow.getEndActionList().addAll(parseAnnotatedActions(endElement));
		}
	}

	/**
	 * Parse the inline flow definitions in the XML file and add corresponding
	 * flow factory beans to the flow local bean factory.
	 */
	protected void addInlineFlowDefinitions(Flow flow, Element parentFlowElement) {
		List inlineFlowElements = DomUtils.getChildElementsByTagName(parentFlowElement, INLINE_FLOW_ELEMENT);
		if (inlineFlowElements.isEmpty()) {
			return;
		}
		for (int i = 0; i < inlineFlowElements.size(); i++) {
			Element inlineFlowElement = (Element)inlineFlowElements.get(i);
			String inlineFlowId = inlineFlowElement.getAttribute(ID_ATTRIBUTE);
			Element flowElement = (Element)inlineFlowElement.getElementsByTagName(FLOW_ATTRIBUTE).item(0);
			Flow inlineFlow = parseFlow(inlineFlowId, null, flowElement);
			buildInlineFlow(inlineFlow, flowElement);
			flow.addInlineFlow(inlineFlow);
		}
	}

	/**
	 * Build the nested inline flow definition.
	 * 
	 * @param inlineFlow the inline flow to build
	 * @param flowElement the inline flow's "flow" element
	 */
	protected void buildInlineFlow(Flow inlineFlow, Element flowElement) {
		addInlineFlowDefinitions(inlineFlow, flowElement);
		addStateDefinitions(inlineFlow, flowElement);
		inlineFlow.getExceptionHandlerSet().addAll(parseExceptionHandlers(flowElement));
		inlineFlow.resolveStateTransitionsTargetStates();
		destroyFlowArtifactRegistry(inlineFlow);
	}

	public void buildStates() throws FlowBuilderException {
		addStateDefinitions(getFlow(), getDocumentElement());
	}

	/**
	 * Parse the state definitions in given element and add them to the flow
	 * object we're constructing.
	 */
	protected void addStateDefinitions(Flow flow, Element flowElement) {
		String startStateId = flowElement.getAttribute(START_STATE_ATTRIBUTE);
		NodeList nodeList = flowElement.getChildNodes();
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
					parseAndAddStateActions(childElement, state);
					state.getExceptionHandlerSet().addAll(parseExceptionHandlers(childElement));
				}
			}
		}
		flow.setStartState(startStateId);
	}

	/**
	 * Parse all state entry and exit actions defined in given element and add
	 * them to given state.
	 */
	protected void parseAndAddStateActions(Element element, State state) {
		// parse any state entry actions
		List entryElements = DomUtils.getChildElementsByTagName(element, ENTRY_ACTIONS_ELEMENT);
		if (!entryElements.isEmpty()) {
			Element entryElement = (Element)entryElements.get(0);
			state.getEntryActionList().addAll(parseAnnotatedActions(entryElement));
		}
		if (state instanceof TransitionableState) {
			// parse any state exit actions
			List exitElements = DomUtils.getChildElementsByTagName(element, EXIT_ACTIONS_ELEMENT);
			if (!exitElements.isEmpty()) {
				Element exitElement = (Element)exitElements.get(0);
				((TransitionableState)state).getExitActionList().addAll(parseAnnotatedActions(exitElement));
			}
		}
	}

	/**
	 * Parse given action state definition and add a corresponding state to the
	 * flow.
	 */
	protected ActionState parseActionState(Flow flow, Element element) {
		ActionState state = (ActionState)getLocalFlowArtifactFactory().createState(flow,
				element.getAttribute(ID_ATTRIBUTE), ActionState.class, parseProperties(element));
		state.getActionList().addAll(parseAnnotatedActions(element));
		state.addTransitions(parseTransitions(state, element));
		return state;
	}

	/**
	 * Parse given view state definition and add a corresponding state to the
	 * flow.
	 */
	protected ViewState parseViewState(Flow flow, Element element) {
		ViewState state = (ViewState)getLocalFlowArtifactFactory().createState(flow,
				element.getAttribute(ID_ATTRIBUTE), ViewState.class, parseProperties(element));
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			state.setViewSelector((ViewSelector)fromStringTo(ViewSelector.class).execute(
					element.getAttribute(VIEW_ATTRIBUTE)));
		}
		state.addTransitions(parseTransitions(state, element));
		return state;
	}

	/**
	 * Parse given decision state definition and add a corresponding state to
	 * the flow.
	 */
	protected DecisionState parseDecisionState(Flow flow, Element element) {
		DecisionState state = (DecisionState)getLocalFlowArtifactFactory().createState(flow,
				element.getAttribute(ID_ATTRIBUTE), DecisionState.class, parseProperties(element));
		state.addTransitions(parseIfs(element));
		return state;
	}

	/**
	 * Parse given subflow state definition and add a corresponding state to the
	 * flow.
	 */
	protected SubflowState parseSubflowState(Flow flow, Element element) {
		SubflowState state = (SubflowState)getLocalFlowArtifactFactory().createState(flow,
				element.getAttribute(ID_ATTRIBUTE), SubflowState.class, parseProperties(element));
		state.setSubflow(getLocalFlowArtifactFactory().getSubflow(element.getAttribute(FLOW_ATTRIBUTE)));
		state.setAttributeMapper(parseAttributeMapper(element));
		state.addTransitions(parseTransitions(state, element));
		return state;
	}

	/**
	 * Parse given end state definition and add a corresponding state to the
	 * flow.
	 */
	protected EndState parseEndState(Flow flow, Element element) {
		EndState state = (EndState)getLocalFlowArtifactFactory().createState(flow, element.getAttribute(ID_ATTRIBUTE),
				EndState.class, parseProperties(element));
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			state.setViewSelector((ViewSelector)fromStringTo(ViewSelector.class).execute(
					element.getAttribute(VIEW_ATTRIBUTE)));
		}
		return state;
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
		return (AnnotatedAction[])actions.toArray(new AnnotatedAction[0]);
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
			MethodKey method = (MethodKey)fromStringTo(MethodKey.class).execute(element.getAttribute(METHOD_ATTRIBUTE));
			action.setMethod(method);
		}
		if (element.hasAttribute(RESULT_NAME_ATTRIBUTE)) {
			action.setResultName(element.getAttribute(RESULT_NAME_ATTRIBUTE));
		}
		if (element.hasAttribute(RESULT_SCOPE_ATTRIBUTE)
				&& !element.getAttribute(RESULT_SCOPE_ATTRIBUTE).equals(NONE_VALUE)) {
			ScopeType scopeType = (ScopeType)fromStringTo(ScopeType.class).execute(
					element.getAttribute(RESULT_SCOPE_ATTRIBUTE));
			action.setResultScope(scopeType);
		}
		parseAndSetProperties(element, action);
		return action;
	}

	/**
	 * Parse an action definition and return the corresponding object.
	 */
	protected Action parseAction(Element element) throws FlowBuilderException {
		return getLocalFlowArtifactFactory().getAction(element.getAttribute(BEAN_ATTRIBUTE));
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
	protected Transition[] parseTransitions(TransitionableState sourceState, Element element) {
		List transitions = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, TRANSITION_ELEMENT);
		for (int i = 0; i < transitionElements.size(); i++) {
			transitions.add(parseTransition(sourceState, (Element)transitionElements.get(i)));
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Parse a transition definition and return a corresponding Transition
	 * object.
	 */
	protected Transition parseTransition(TransitionableState sourceState, Element element) {
		Transition transition = (Transition)getLocalFlowArtifactFactory().createTransition(sourceState,
				parseProperties(element));
		transition.setMatchingCriteria((TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(
				element.getAttribute(ON_ATTRIBUTE)));
		transition.setExecutionCriteria(TransitionCriteriaChain.criteriaChainFor(parseAnnotatedActions(element)));
		transition.setTargetStateResolver((Transition.TargetStateResolver)fromStringTo(
				Transition.TargetStateResolver.class).execute(element.getAttribute(TO_ATTRIBUTE)));
		return transition;
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
	 * Obtain an attribute mapper reference from given subflow definition
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
			if (mapperElement.hasAttribute(BEAN_ATTRIBUTE)) {
				return getLocalFlowArtifactFactory().getAttributeMapper(mapperElement.getAttribute(BEAN_ATTRIBUTE));
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
	 * list.
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

	public void buildExceptionHandlers() throws FlowBuilderException {
		getFlow().getExceptionHandlerSet().addAll(parseExceptionHandlers(getDocumentElement()));
	}

	/**
	 * Parse the list of exception handlers present in the XML document and add
	 * them to the flow definition being built.
	 */
	protected StateExceptionHandler[] parseExceptionHandlers(Element element) {
		List handlerElements = DomUtils.getChildElementsByTagName(element, EXCEPTION_HANDLER_ELEMENT);
		if (handlerElements.isEmpty()) {
			return null;
		}
		StateExceptionHandler[] exceptionHandlers = new StateExceptionHandler[handlerElements.size()];
		for (int i = 0; i < handlerElements.size(); i++) {
			Element handlerElement = (Element)handlerElements.get(i);
			if (handlerElement.hasAttribute(BEAN_ATTRIBUTE)) {
				exceptionHandlers[i] = getLocalFlowArtifactFactory().getExceptionHandler(
						handlerElement.getAttribute(BEAN_ATTRIBUTE));
			}
			else {
				exceptionHandlers[i] = parseDefaultExceptionHandler(handlerElement);
			}
		}
		return exceptionHandlers;
	}

	/**
	 * Parse a default exception handler definition from given element.
	 */
	protected StateExceptionHandler parseDefaultExceptionHandler(Element element) {
		TransitionExecutingStateExceptionHandler defaultHandler = new TransitionExecutingStateExceptionHandler();
		Class exceptionClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(ON_ATTRIBUTE));
		State state = getFlow().getState(element.getAttribute(TO_ATTRIBUTE));
		defaultHandler.add(exceptionClass, state);
		return defaultHandler;
	}

	public void dispose() {
		destroyFlowArtifactRegistry(getFlow());
		document = null;
	}

	/**
	 * Pops the local registry off the stack for the flow definition that has
	 * just been constructed.
	 * @param flow the built flow
	 */
	protected void destroyFlowArtifactRegistry(Flow flow) {
		localFlowArtifactFactory.pop();
	}

	/**
	 * A local artifact factory that searches local registries first before
	 * querying the global, externally managed artifact factory.
	 * @author Keith Donald
	 */
	private class LocalFlowArtifactFactory extends FlowArtifactFactoryAdapter {

		/**
		 * The stack of registries.
		 */
		private Stack localFlowArtifactRegistries = new Stack();

		/**
		 * Push a new registry onto the stack
		 * @param registry the local registry
		 */
		public void push(LocalFlowArtifactRegistry registry) {
			if (localFlowArtifactRegistries.isEmpty()) {
				attachRootServiceRegistryIfSupported(registry.context);
			}
			else {
				registry.context.setParent(top().context);
			}
			localFlowArtifactRegistries.push(registry);
		}

		/**
		 * Attach a master service registry as a parent registry of the local
		 * context, if supported by the configured flow artifact factory.
		 * @param context the local context to attach a global service registry
		 * to
		 */
		protected void attachRootServiceRegistryIfSupported(ConfigurableApplicationContext context) {
			try {
				context.getBeanFactory().setParentBeanFactory(getFlowArtifactFactory().getServiceRegistry());
			}
			catch (UnsupportedOperationException e) {

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

		public Flow getCurrentFlow() {
			return top().flow;
		}

		public Flow getSubflow(String id) throws FlowArtifactException {
			Flow currentFlow = getCurrentFlow();
			// quick check for recursive subflow
			if (currentFlow.getId().equals(id)) {
				return currentFlow;
			}
			// check local inline flows
			if (currentFlow.containsInlineFlow(id)) {
				return currentFlow.getInlineFlow(id);
			}
			// check externally managed toplevel flows
			return getFlowArtifactFactory().getSubflow(id);
		}

		public Action getAction(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return toAction(top().context.getBean(id));
				}
			}
			return getFlowArtifactFactory().getAction(id);
		}

		/**
		 * Helper method to the given service object into an action. If the
		 * given service object implements the <code>Action</code> interface,
		 * it is returned as is, otherwise it is wrapped in an action that can
		 * invoke a method on the service bean.
		 * @param artifact the service bean
		 * @return the action
		 */
		protected Action toAction(Object artifact) {
			if (artifact instanceof Action) {
				return (Action)artifact;
			}
			else {
				return new LocalBeanInvokingAction(artifact);
			}
		}

		public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (FlowAttributeMapper)top().context.getBean(id);
				}
			}
			return getFlowArtifactFactory().getAttributeMapper(id);
		}

		public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (StateExceptionHandler)top().context.getBean(id);
				}
			}
			return getFlowArtifactFactory().getExceptionHandler(id);
		}

		public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (TransitionCriteria)top().context.getBean(id);
				}
			}
			return getFlowArtifactFactory().getTransitionCriteria(id);
		}

		public ViewSelector getViewSelector(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (ViewSelector)top().context.getBean(id);
				}
			}
			return getFlowArtifactFactory().getViewSelector(id);
		}

		public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
			if (!localFlowArtifactRegistries.isEmpty()) {
				if (top().context.containsBean(id)) {
					return (TargetStateResolver)top().context.getBean(id);
				}
			}
			return getFlowArtifactFactory().getTargetStateResolver(id);
		}

		public Flow createFlow(String id, Map properties) throws FlowArtifactException {
			top().flow = getFlowArtifactFactory().createFlow(id, properties);
			return top().flow;
		}

		public State createState(Flow flow, String id, Class stateType, Map properties) throws FlowArtifactException {
			return getFlowArtifactFactory().createState(flow, id, stateType, properties);
		}

		public Transition createTransition(TransitionableState sourceState, Map properties)
				throws FlowArtifactException {
			return getFlowArtifactFactory().createTransition(sourceState, properties);
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
		public LocalFlowArtifactRegistry(ConfigurableApplicationContext registry) {
			this.context = registry;
		}
	}
}