package org.springframework.webflow.builder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.convert.support.TextToExpression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.method.MethodSignature;
import org.springframework.binding.method.TextToMethodSignature;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.action.AbstractBeanInvokingAction;
import org.springframework.webflow.action.BeanFactoryBeanInvokingAction;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.action.MementoBeanStatePersister;
import org.springframework.webflow.action.MementoOriginator;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.action.ResultSpecification;
import org.springframework.webflow.action.StatefulBeanInvokingAction;
import org.springframework.webflow.support.DefaultExpressionParserFactory;

/**
 * Base implementation of a flow artifact factory that implements a minimal set
 * of the <code>FlowArtifactFactory</code> interface, throwing unsupported
 * operation exceptions for most operations.
 * <p>
 * May be subclassed to offer additional factory/lookup support.
 * @author Keith Donald
 */
public class DefaultFlowArtifactFactory implements FlowArtifactFactory {

	/**
	 * The parser for parsing expression strings into evaluatable expression
	 * objects.
	 */
	private ExpressionParser expressionParser = new DefaultExpressionParserFactory().getExpressionParser();

	/**
	 * A conversion service that can convert between types.
	 */
	private ConversionService conversionService = createDefaultConversionService();

	/**
	 * A resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * Determines which result event factory should be used for each bean
	 * invoking action created by this factory.
	 */
	private ResultEventFactorySelector resultEventFactorySelector = new ResultEventFactorySelector();

	/**
	 * Set the expression parser responsible for parsing expression strings into
	 * evaluatable expression objects.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	/**
	 * Set the conversion service to use to convert between types; typically
	 * from string to a rich object type.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Set the resource loader to load file-based resources from string-encoded
	 * paths.
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Returns the strategy for calcuating the result event factory to configure
	 * for each bean invoking action created by this factory.
	 */
	public ResultEventFactorySelector getResultEventFactorySelector() {
		return resultEventFactorySelector;
	}

	/**
	 * Sets the strategy to calculate the result event factory to configure for
	 * each bean invoking action created by this factory.
	 */
	public void setResultEventFactorySelector(ResultEventFactorySelector resultEventFactorySelector) {
		this.resultEventFactorySelector = resultEventFactorySelector;
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, Flow.class, "Subflow lookup is not supported by this artifact factory");
	}

	public Action getAction(String id) throws FlowArtifactException {
		return (Action)getService(id, Action.class);
	}

	public boolean isAction(String actionId) throws FlowArtifactException {
		return Action.class.isAssignableFrom(getServiceType(actionId, Action.class));
	}

	public boolean isMultiAction(String actionId) throws FlowArtifactException {
		if (containsService(actionId)) {
			return MultiAction.class.isAssignableFrom(getServiceRegistry().getType(actionId));
		}
		else {
			return false;
		}
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		return (FlowAttributeMapper)getService(id, FlowAttributeMapper.class);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		return (TransitionCriteria)getService(id, TransitionCriteria.class);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		return (ViewSelector)getService(id, ViewSelector.class);
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
		return (TargetStateResolver)getService(id, TargetStateResolver.class);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		return (StateExceptionHandler)getService(id, StateExceptionHandler.class);
	}

	public Flow createFlow(String id, AttributeCollection attributes) throws FlowArtifactException {
		Flow flow = new Flow(id);
		flow.getAttributeMap().putAll(attributes);
		return flow;
	}

	public State createViewState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException {
		ViewState viewState = new ViewState(flow, id);
		if (viewSelector != null) {
			viewState.setViewSelector(viewSelector);
		}
		configureCommonProperties(viewState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return viewState;
	}

	public State createActionState(String id, Flow flow, Action[] entryActions, Action[] actions,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException {
		ActionState actionState = new ActionState(flow, id);
		actionState.getActionList().addAll(actions);
		configureCommonProperties(actionState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return actionState;
	}

	public State createDecisionState(String id, Flow flow, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes)
			throws FlowArtifactException {
		DecisionState decisionState = new DecisionState(flow, id);
		configureCommonProperties(decisionState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return decisionState;
	}

	public State createSubflowState(String id, Flow flow, Action[] entryActions, Flow subflow,
			FlowAttributeMapper attributeMapper, Transition[] transitions, StateExceptionHandler[] exceptionHandlers,
			Action[] exitActions, AttributeCollection attributes) throws FlowArtifactException {
		SubflowState subflowState = new SubflowState(flow, id, subflow);
		if (attributeMapper != null) {
			subflowState.setAttributeMapper(attributeMapper);
		}
		configureCommonProperties(subflowState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return subflowState;
	}

	public State createEndState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			AttributeMapper outputMapper, StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes)
			throws FlowArtifactException {
		EndState endState = new EndState(flow, id);
		if (viewSelector != null) {
			endState.setViewSelector(viewSelector);
		}
		configureCommonProperties(endState, entryActions, exceptionHandlers, attributes);
		return endState;
	}

	public Action createBeanInvokingAction(String beanId, MethodSignature methodSignature,
			ResultSpecification resultSpecification, AttributeCollection attributes) {
		if (isPrototype(beanId)) {
			return createStatefulAction(beanId, methodSignature, resultSpecification, attributes);
		}
		else {
			Object bean = getService(beanId, Object.class);
			LocalBeanInvokingAction action = new LocalBeanInvokingAction(methodSignature, bean);
			configureCommonProperties(action, methodSignature, resultSpecification, bean.getClass());
			return action;
		}
	}

	public Transition createTransition(TransitionCriteria matchingCriteria, TransitionCriteria executionCriteria,
			TargetStateResolver targetStateResolver, AttributeCollection attributes) throws FlowArtifactException {
		Transition transition = new Transition(targetStateResolver);
		if (matchingCriteria != null) {
			transition.setMatchingCriteria(matchingCriteria);
		}
		if (executionCriteria != null) {
			transition.setExecutionCriteria(executionCriteria);
		}
		transition.getAttributeMap().putAll(attributes);
		return transition;
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Service registry lookup is not supported by this artifact factory");
	}

	public ResourceLoader getResourceLoader() throws UnsupportedOperationException {
		return resourceLoader;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	protected Action createStatefulAction(String beanId, MethodSignature methodSignature,
			ResultSpecification resultSpecification, AttributeCollection attributes) {
		Class beanClass = getServiceType(beanId, Action.class);
		if (MementoOriginator.class.isAssignableFrom(beanClass)) {
			BeanFactoryBeanInvokingAction action = new BeanFactoryBeanInvokingAction(methodSignature, beanId,
					getServiceRegistry());
			action.setBeanStatePersister(new MementoBeanStatePersister());
			configureCommonProperties(action, methodSignature, resultSpecification, beanClass);
			return action;
		}
		else {
			StatefulBeanInvokingAction action = new StatefulBeanInvokingAction(methodSignature, beanId,
					getServiceRegistry());
			configureCommonProperties(action, methodSignature, resultSpecification, beanClass);
			return action;
		}
	}

	private void configureCommonProperties(AbstractBeanInvokingAction action, MethodSignature methodSignature,
			ResultSpecification resultSpecification, Class beanClass) {
		action.setResultSpecification(resultSpecification);
		action.setResultEventFactory(resultEventFactorySelector.forMethod(methodSignature, beanClass));
		action.setConversionService(conversionService);
	}

	private ConversionService createDefaultConversionService() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter(new TextToTransitionCriteria(this));
		service.addConverter(new TextToViewSelector(this));
		service.addConverter(new TextToTransitionTargetStateResolver(this));
		service.addConverter(new TextToExpression(getExpressionParser()));
		service.addConverter(new TextToMethodSignature(service));
		return service;
	}

	private void configureCommonProperties(TransitionableState state, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes) {
		configureCommonProperties(state, entryActions, exceptionHandlers, attributes);
		state.getTransitionSet().addAll(transitions);
		state.getExitActionList().addAll(exitActions);
	}

	private void configureCommonProperties(State state, Action[] entryActions,
			StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes) {
		state.getEntryActionList().addAll(entryActions);
		state.getExceptionHandlerSet().addAll(exceptionHandlers);
		state.getAttributeMap().putAll(attributes);
	}

	protected boolean isPrototype(String beanId) {
		if (containsService(beanId)) {
			return !getServiceRegistry().isSingleton(beanId);
		}
		else {
			return false;
		}
	}

	protected boolean containsService(String id) {
		return getServiceRegistry().containsBean(id);
	}

	protected Object getService(String id, Class artifactType) {
		try {
			return getServiceRegistry().getBean(id, artifactType);
		}
		catch (BeansException e) {
			throw new FlowArtifactException(id, artifactType, e);
		}
	}

	protected Class getServiceType(String id, Class artifactType) {
		try {
			return getServiceRegistry().getType(id);
		}
		catch (BeansException e) {
			throw new FlowArtifactException(id, artifactType, e);
		}
	}
}