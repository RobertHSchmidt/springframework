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
package org.springframework.webflow.builder;

import java.io.IOException;
import java.io.InputStream;
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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.method.MethodSignature;
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
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.FlowVariable;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.action.ResultSpecification;
import org.springframework.webflow.support.BeanFactoryFlowVariable;
import org.springframework.webflow.support.CollectionAddingPropertyExpression;
import org.springframework.webflow.support.ImmutableFlowAttributeMapper;
import org.springframework.webflow.support.SimpleFlowVariable;
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
 *     &lt;!DOCTYPE flow PUBLIC &quot;-//SPRING//DTD WEBFLOW 1.0//EN&quot;
 *     &quot;http://www.springframework.org/dtd/spring-webflow-1.0.dtd&quot;&gt;
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

	private static final String DEFAULT_VALUE = "default";

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

	private static final String OUTPUT_MAPPER_ELEMENT = "output-mapper";

	private static final String INPUT_MAPPER_ELEMENT = "input-mapper";

	private static final String MAPPING_ELEMENT = "mapping";

	private static final String SOURCE_ATTRIBUTE = "source";

	private static final String TARGET_ATTRIBUTE = "target";

	private static final String FROM_ATTRIBUTE = "from";

	private static final String TO_ATTRIBUTE = "to";

	private static final String TARGET_COLLECTION_ATTRIBUTE = "target-collection";

	private static final String END_STATE_ELEMENT = "end-state";

	private static final String OUTPUT_ATTRIBUTE_ELEMENT = "output-attribute";

	private static final String TRANSITION_ELEMENT = "transition";

	private static final String GLOBAL_TRANSITIONS_ELEMENT = "global-transitions";

	private static final String ON_ATTRIBUTE = "on";

	private static final String ON_EXCEPTION_ATTRIBUTE = "on-exception";

	private static final String ATTRIBUTE_ELEMENT = "attribute";

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String VALUE_ELEMENT = "value";

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String VAR_ELEMENT = "var";

	private static final String SCOPE_ATTRIBUTE = "scope";

	private static final String CLASS_ATTRIBUTE = "class";

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
	 * @param location the location of the xml-based flow definition resource
	 */
	public XmlFlowBuilder(Resource location) {
		super();
		setLocation(location);
	}

	/**
	 * Create a new XML flow builder parsing the document at the specified
	 * location, using the provided factory to access externally managed flow
	 * artifacts.
	 * @param location the location of the xml-based flow definition resource
	 * @param flowArtifactFactory the bean factory defining this flow builder
	 */
	public XmlFlowBuilder(Resource location, FlowArtifactFactory flowArtifactFactory) {
		super(flowArtifactFactory);
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

	public void init(FlowArtifactParameters flowParameters) throws FlowBuilderException {
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
		setFlow(parseFlow(flowParameters, getDocumentElement()));
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
	protected Flow parseFlow(FlowArtifactParameters flowParameters, Element flowElement) {
		Assert.state(FLOW_ELEMENT.equals(flowElement.getTagName()), "This is not the '" + FLOW_ELEMENT + "' element");
		initLocalFlowArtifactFactoryRegistry(flowElement);
		FlowArtifactParameters parameters = flowParameters.putAll(parseAttributes(flowElement));
		Flow flow = getLocalFlowArtifactFactory().createFlow(parameters);
		parseAndAddFlowVariables(flowElement, flow);
		flow.setInputMapper(parseInputMapper(flowElement));
		parseAndAddFlowActions(flowElement, flow);
		parseAndAddFlowTransitions(flowElement, flow);
		flow.setOutputMapper(parseOutputMapper(flowElement));
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
		context.setResourceLoader(getFlowArtifactFactory().getResourceLoader());
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(resources);
		localFlowArtifactFactory.push(new LocalFlowArtifactRegistry(context));
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
		for (int i = 0; i < varElements.size(); i++) {
			flow.addVariable(parseVariable((Element)varElements.get(i)));
		}
	}

	/**
	 * Parse the flow variable.
	 * @param element the var element
	 * @return the flow variable
	 */
	protected FlowVariable parseVariable(Element element) {
		ScopeType scope = null;
		if (element.hasAttribute(SCOPE_ATTRIBUTE) && !element.getAttribute(SCOPE_ATTRIBUTE).equals(DEFAULT_VALUE)) {
			scope = (ScopeType)fromStringTo(ScopeType.class).execute(element.getAttribute(SCOPE_ATTRIBUTE));
		}
		if (StringUtils.hasText(element.getAttribute(BEAN_ATTRIBUTE))) {
			BeanFactory beanFactory = getLocalFlowArtifactFactory().getServiceRegistry();
			return new BeanFactoryFlowVariable(element.getAttribute(NAME_ATTRIBUTE), scope, element
					.getAttribute(BEAN_ATTRIBUTE), beanFactory);
		}
		else {
			if (StringUtils.hasText(element.getAttribute(CLASS_ATTRIBUTE))) {
				Class variableClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(CLASS_ATTRIBUTE));
				return new SimpleFlowVariable(element.getAttribute(NAME_ATTRIBUTE), variableClass, scope);
			}
			else {
				BeanFactory beanFactory = getLocalFlowArtifactFactory().getServiceRegistry();
				return new BeanFactoryFlowVariable(element.getAttribute(NAME_ATTRIBUTE), scope, null, beanFactory);
			}
		}
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
	 * Parse all state entry and exit actions defined in given element and add
	 * them to given state.
	 */
	protected void parseAndAddFlowTransitions(Element element, Flow flow) {
		List transitionElements = DomUtils.getChildElementsByTagName(element, GLOBAL_TRANSITIONS_ELEMENT);
		if (!transitionElements.isEmpty()) {
			Element globalTransitionsElement = (Element)transitionElements.get(0);
			TransitionExecutors transitionExecutors = parseTransitionExecutors(globalTransitionsElement);
			flow.getGlobalTransitionSet().addAll(transitionExecutors.getTransitions());
			flow.getExceptionHandlerSet().addAll(transitionExecutors.getExceptionHandlers());
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
			Flow inlineFlow = parseFlow(new FlowArtifactParameters(inlineFlowId), flowElement);
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
		ActionState state = (ActionState)getLocalFlowArtifactFactory().createState(flow, ActionState.class,
				parseParameters(element));
		state.getActionList().addAll(parseAnnotatedActions(element));
		parseAndAddTransitionExecutors(state, element);
		return state;
	}

	private void parseAndAddTransitionExecutors(TransitionableState state, Element element) {
		TransitionExecutors transitionExecutors = parseTransitionExecutors(element);
		state.getTransitionSet().addAll(transitionExecutors.getTransitions());
		state.getExceptionHandlerSet().addAll(transitionExecutors.getExceptionHandlers());
	}

	/**
	 * Parse given view state definition and add a corresponding state to the
	 * flow.
	 */
	protected ViewState parseViewState(Flow flow, Element element) {
		ViewState state = (ViewState)getLocalFlowArtifactFactory().createState(flow, ViewState.class,
				parseParameters(element));
		state.setViewSelector(viewSelector(TextToViewSelector.VIEW_STATE_TYPE, element.getAttribute(VIEW_ATTRIBUTE)));
		parseAndAddTransitionExecutors(state, element);
		return state;
	}

	protected FlowArtifactParameters parseParameters(Element element) {
		return new FlowArtifactParameters(element.getAttribute(ID_ATTRIBUTE), parseAttributes(element));
	}

	/**
	 * Parse given decision state definition and add a corresponding state to
	 * the flow.
	 */
	protected DecisionState parseDecisionState(Flow flow, Element element) {
		DecisionState state = (DecisionState)getLocalFlowArtifactFactory().createState(flow, DecisionState.class,
				parseParameters(element));
		state.getTransitionSet().addAll(parseIfs(element));
		return state;
	}

	/**
	 * Parse given subflow state definition and add a corresponding state to the
	 * flow.
	 */
	protected SubflowState parseSubflowState(Flow flow, Element element) {
		SubflowState state = (SubflowState)getLocalFlowArtifactFactory().createState(flow, SubflowState.class,
				parseParameters(element));
		state.setSubflow(getLocalFlowArtifactFactory().getSubflow(element.getAttribute(FLOW_ATTRIBUTE)));
		state.setAttributeMapper(parseFlowAttributeMapper(element));
		parseAndAddTransitionExecutors(state, element);
		return state;
	}

	/**
	 * Parse given end state definition and add a corresponding state to the
	 * flow.
	 */
	protected EndState parseEndState(Flow flow, Element element) {
		EndState state = (EndState)getLocalFlowArtifactFactory().createState(flow, EndState.class,
				parseParameters(element));
		state.setViewSelector(viewSelector(TextToViewSelector.END_STATE_TYPE, element.getAttribute(VIEW_ATTRIBUTE)));
		List outputAttributeElements = DomUtils.getChildElementsByTagName(element, OUTPUT_ATTRIBUTE_ELEMENT);
		Iterator it = outputAttributeElements.iterator();
		while (it.hasNext()) {
			Element outputElement = (Element)it.next();
			state.addOutputAttributeName(outputElement.getAttribute(NAME_ATTRIBUTE));
		}
		return state;
	}

	/**
	 * Turn given view name into a corresponding view selector.
	 * @param viewName the view name (might be encoded)
	 * @return the corresponding view selector
	 */
	private ViewSelector viewSelector(String stateType, String viewName) {
		Map context = new HashMap(1, 1);
		context.put(TextToViewSelector.STATE_TYPE_CONTEXT_PARAMETER, stateType);
		return (ViewSelector)fromStringTo(ViewSelector.class).execute(viewName, context);
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
		AnnotatedAction action = new AnnotatedAction();
		if (element.hasAttribute(NAME_ATTRIBUTE)) {
			action.setName(element.getAttribute(NAME_ATTRIBUTE));
		}
		if (element.hasAttribute(METHOD_ATTRIBUTE)
				&& getLocalFlowArtifactFactory().isMultiAction(element.getAttribute(BEAN_ATTRIBUTE))) {
			action.getAttributeMap().put(MultiAction.METHOD_ATTRIBUTE, element.getAttribute(METHOD_ATTRIBUTE));
		}
		action.getAttributeMap().putAll(parseAttributes(element));
		action.setTargetAction(parseAction(element));
		return action;
	}

	/**
	 * Parse an action definition and return the corresponding object.
	 */
	protected Action parseAction(Element element) {
		String actionId = element.getAttribute(BEAN_ATTRIBUTE);
		MethodSignature method = null;
		if (element.hasAttribute(METHOD_ATTRIBUTE)) {
			method = (MethodSignature)fromStringTo(MethodSignature.class).execute(
					element.getAttribute(METHOD_ATTRIBUTE));
		}
		String resultName = null;
		if (element.hasAttribute(RESULT_NAME_ATTRIBUTE)) {
			resultName = element.getAttribute(RESULT_NAME_ATTRIBUTE);
		}
		ScopeType resultScope = null;
		if (element.hasAttribute(RESULT_SCOPE_ATTRIBUTE)
				&& !element.getAttribute(RESULT_SCOPE_ATTRIBUTE).equals(DEFAULT_VALUE)) {
			resultScope = (ScopeType)fromStringTo(ScopeType.class)
					.execute(element.getAttribute(RESULT_SCOPE_ATTRIBUTE));
		}
		ResultSpecification resultSpecification = null;
		if (resultName != null) {
			resultSpecification = new ResultSpecification(resultName, (resultScope != null ? resultScope
					: ScopeType.REQUEST));
		}
		BeanInvokingActionParameters actionParameters = new BeanInvokingActionParameters(actionId, method,
				resultSpecification, null, null);
		return getLocalFlowArtifactFactory().getAction(actionParameters);
	}

	/**
	 * Parse all properties defined as nested elements of given element. Returns
	 * the properties as a map: the name of the property is the key, the
	 * associated value the value.
	 */
	protected AttributeCollection parseAttributes(Element element) {
		AttributeMap attributes = new AttributeMap();
		List propertyElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE_ELEMENT);
		for (int i = 0; i < propertyElements.size(); i++) {
			parseAndSetAttribute((Element)propertyElements.get(i), attributes);
		}
		return attributes;
	}

	/**
	 * Parse a property definition from given element and add the property to
	 * given set.
	 */
	protected void parseAndSetAttribute(Element element, AttributeMap attributes) {
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
		attributes.put(name, convertPropertyValue(element, value));
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
	protected TransitionExecutors parseTransitionExecutors(Element element) {
		List transitions = new LinkedList();
		List exceptionHandlers = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, TRANSITION_ELEMENT);
		for (int i = 0; i < transitionElements.size(); i++) {
			Element transitionElement = (Element)transitionElements.get(i);
			if (StringUtils.hasText(transitionElement.getAttribute(ON_EXCEPTION_ATTRIBUTE))) {
				exceptionHandlers.add(parseTransitionExecutingExceptionHandler(transitionElement));
			}
			else {
				transitions.add(parseTransition(transitionElement));
			}
		}
		return new TransitionExecutors((Transition[])transitions.toArray(new Transition[transitions.size()]),
				(StateExceptionHandler[])exceptionHandlers.toArray(new StateExceptionHandler[exceptionHandlers.size()]));

	}

	protected static class TransitionExecutors {
		private Transition[] transitions;

		private StateExceptionHandler[] exceptionHandlers;

		public TransitionExecutors(Transition[] transitions, StateExceptionHandler[] exceptionHandlers) {
			this.transitions = transitions;
			this.exceptionHandlers = exceptionHandlers;
		}

		public Transition[] getTransitions() {
			return transitions;
		}

		public StateExceptionHandler[] getExceptionHandlers() {
			return exceptionHandlers;
		}
	}

	/**
	 * Parse a transition definition and return a corresponding Transition
	 * object.
	 */
	protected Transition parseTransition(Element element) {
		Transition transition = getLocalFlowArtifactFactory().createTransition(parseAttributes(element).unmodifiable());
		transition.setMatchingCriteria((TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(
				element.getAttribute(ON_ATTRIBUTE)));
		transition.setExecutionCriteria(TransitionCriteriaChain.criteriaChainFor(parseAnnotatedActions(element)));
		transition.setTargetStateResolver((TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(
				element.getAttribute(TO_ATTRIBUTE)));
		return transition;
	}

	/**
	 * Parse a transition executing exception handler definition.
	 */
	protected StateExceptionHandler parseTransitionExecutingExceptionHandler(Element element) {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		Class exceptionClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(ON_EXCEPTION_ATTRIBUTE));
		handler.add(exceptionClass, (TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(
				element.getAttribute(TO_ATTRIBUTE)));
		return handler;
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
		Transition thenTransition = getLocalFlowArtifactFactory().createTransition(CollectionUtils.EMPTY_ATTRIBUTE_MAP);
		thenTransition.setMatchingCriteria(criteria);
		ConversionExecutor converter = fromStringTo(TargetStateResolver.class);
		thenTransition.setTargetStateResolver((TargetStateResolver)converter.execute(element
				.getAttribute(THEN_ATTRIBUTE)));
		if (StringUtils.hasText(element.getAttribute(ELSE_ATTRIBUTE))) {
			Transition elseTransition = getLocalFlowArtifactFactory().createTransition(
					CollectionUtils.EMPTY_ATTRIBUTE_MAP);
			elseTransition.setTargetStateResolver((TargetStateResolver)converter.execute(element
					.getAttribute(ELSE_ATTRIBUTE)));
			return new Transition[] { thenTransition, elseTransition };
		}
		else {
			return new Transition[] { thenTransition };
		}
	}

	protected FlowAttributeMapper parseFlowAttributeMapper(Element element) {
		List mapperElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE_MAPPER_ELEMENT);
		if (mapperElements.isEmpty()) {
			return null;
		}
		Element mapperElement = (Element)mapperElements.get(0);
		if (StringUtils.hasText(mapperElement.getAttribute(BEAN_ATTRIBUTE))) {
			return getFlowArtifactFactory().getAttributeMapper(mapperElement.getAttribute(BEAN_ATTRIBUTE));
		}
		else {
			return new ImmutableFlowAttributeMapper(parseInputMapper(mapperElement), parseOutputMapper(mapperElement));
		}
	}

	protected AttributeMapper parseInputMapper(Element element) {
		List mapperElements = DomUtils.getChildElementsByTagName(element, INPUT_MAPPER_ELEMENT);
		return mapperElements.isEmpty() ? null : parseAttributeMapper((Element)mapperElements.get(0));
	}

	protected AttributeMapper parseOutputMapper(Element element) {
		List mapperElements = DomUtils.getChildElementsByTagName(element, OUTPUT_MAPPER_ELEMENT);
		return mapperElements.isEmpty() ? null : parseAttributeMapper((Element)mapperElements.get(0));
	}

	protected AttributeMapper parseAttributeMapper(Element element) {
		List mappingElements = DomUtils.getChildElementsByTagName(element, MAPPING_ELEMENT);
		DefaultAttributeMapper mapper = new DefaultAttributeMapper();
		Iterator it = mappingElements.iterator();
		while (it.hasNext()) {
			Element mappingElement = (Element)it.next();
			Expression source = getExpressionParser().parseExpression(mappingElement.getAttribute(SOURCE_ATTRIBUTE));
			PropertyExpression target = null;
			if (StringUtils.hasText(mappingElement.getAttribute(TARGET_ATTRIBUTE))) {
				target = getExpressionParser().parsePropertyExpression(mappingElement.getAttribute(TARGET_ATTRIBUTE));
			}
			else if (StringUtils.hasText(mappingElement.getAttribute(TARGET_COLLECTION_ATTRIBUTE))) {
				target = new CollectionAddingPropertyExpression(getExpressionParser().parsePropertyExpression(
						mappingElement.getAttribute(TARGET_COLLECTION_ATTRIBUTE)));
			}
			mapper.addMapping(new Mapping(source, target, parseTypeConverter(mappingElement)));
		}
		return mapper;
	}

	protected ConversionExecutor parseTypeConverter(Element element) {
		String from = element.getAttribute(FROM_ATTRIBUTE);
		String to = element.getAttribute(TO_ATTRIBUTE);
		if (StringUtils.hasText(from)) {
			if (StringUtils.hasText(to)) {
				ConversionService service = getFlowArtifactFactory().getConversionService();
				return service.getConversionExecutor(service.getClassByAlias(from), service.getClassByAlias(to));
			}
			else {
				throw new IllegalArgumentException("Use of the 'from' attribute requires use of the 'to' attribute");
			}
		}
		else {
			Assert.isTrue(!StringUtils.hasText(to), "Use of the 'to' attribute requires use of the 'from' attribute");
		}
		return null;

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
			exceptionHandlers[i] = getLocalFlowArtifactFactory().getExceptionHandler(
					handlerElement.getAttribute(BEAN_ATTRIBUTE));
		}
		return exceptionHandlers;
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
	 * Returns the configured expression parser.
	 * @return the expression parser
	 */
	private ExpressionParser getExpressionParser() {
		return getFlowArtifactFactory().getExpressionParser();
	}

	/**
	 * A local artifact factory that searches local registries first before
	 * querying the global, externally managed artifact factory.
	 * @author Keith Donald
	 */
	private class LocalFlowArtifactFactory extends DefaultFlowArtifactFactory {

		/**
		 * The stack of registries.
		 */
		private Stack localRegistries = new Stack();

		/**
		 * Push a new registry onto the stack
		 * @param registry the local registry
		 */
		public void push(LocalFlowArtifactRegistry registry) {
			if (localRegistries.isEmpty()) {
				setFirstParent(registry.context);
			}
			else {
				registry.context.setParent(top().context);
			}
			registry.context.refresh();
			localRegistries.push(registry);
		}

		/**
		 * Attach a master service registry as a parent registry of the local
		 * context, if supported by the configured flow artifact factory.
		 * @param context the local context to attach a global service registry
		 * to
		 */
		private void setFirstParent(ConfigurableApplicationContext context) {
			try {
				context.getBeanFactory().setParentBeanFactory(getFlowArtifactFactory().getServiceRegistry());
			}
			catch (UnsupportedOperationException e) {

			}
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
		
		public Flow createFlow(FlowArtifactParameters parameters) throws FlowArtifactException {
			top().flow = getFlowArtifactFactory().createFlow(parameters);
			return top().flow;
		}
		
		/**
		 * Pop a registry off the stack
		 */
		public LocalFlowArtifactRegistry pop() {
			return (LocalFlowArtifactRegistry)localRegistries.pop();
		}

		/**
		 * Returns the top registry on the stack
		 */
		public LocalFlowArtifactRegistry top() {
			return (LocalFlowArtifactRegistry)localRegistries.peek();
		}

		public Flow getCurrentFlow() {
			return top().flow;
		}

		public BeanFactory getServiceRegistry() {
			return top().context;
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
		 * @param context the local registry
		 */
		public LocalFlowArtifactRegistry(ConfigurableApplicationContext context) {
			this.context = context;
		}
	}
}