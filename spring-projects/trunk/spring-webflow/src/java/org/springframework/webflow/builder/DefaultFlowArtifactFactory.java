package org.springframework.webflow.builder;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.convert.support.TextToExpression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.method.TextToMethodSignature;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.MultiAction;
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

	public Action getAction(FlowArtifactParameters actionParameters) throws FlowArtifactException {
		Class actionType = getServiceType(actionParameters.getId(), Action.class);
		if (Action.class.isAssignableFrom(actionType)) {
			return (Action)getServiceRegistry().getBean(actionParameters.getId());
		}
		else {
			// adapt the bean to an Action using the construction attributes as
			// declarative instructions
			Assert.isInstanceOf(BeanInvokingActionParameters.class, actionParameters, "Wrong parameter object type: ");
			return beanToAction((BeanInvokingActionParameters)actionParameters);
		}
	}

	public boolean isMultiAction(String actionId) throws FlowArtifactException {
		if (containsService(actionId)) {
			return MultiAction.class.isAssignableFrom(getServiceRegistry().getType(actionId));
		}
		else {
			return false;
		}
	}

	public boolean isStatefulAction(String actionId) {
		if (containsService(actionId)) {
			return !getServiceRegistry().isSingleton(actionId);
		}
		else {
			return false;
		}
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		return (FlowAttributeMapper)getService(id, FlowAttributeMapper.class, true);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		return (TransitionCriteria)getService(id, TransitionCriteria.class, true);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		return (ViewSelector)getService(id, ViewSelector.class, true);
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
		return (TargetStateResolver)getService(id, TargetStateResolver.class, true);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		return (StateExceptionHandler)getService(id, StateExceptionHandler.class, true);
	}

	protected boolean containsService(String id) {
		return getServiceRegistry().containsBean(id);
	}
	
	protected Object getService(String id, Class artifactType, boolean enforceTypeCheck) {
		try {
			if (enforceTypeCheck) {
				return getServiceRegistry().getBean(id, artifactType);
			}
			else {
				return getServiceRegistry().getBean(id);
			}
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

	public Flow createFlow(FlowArtifactParameters parameters) throws FlowArtifactException {
		Flow flow = (Flow)newInstance(Flow.class);
		flow.setId(parameters.getId());
		flow.getAttributeMap().putAll(parameters.getAttributes());
		return flow;
	}

	public State createState(Flow flow, Class stateType, FlowArtifactParameters parameters)
			throws FlowArtifactException {
		State state = (State)newInstance(stateType);
		state.setId(parameters.getId());
		state.setFlow(flow);
		state.getAttributeMap().putAll(parameters.getAttributes());
		return state;
	}

	public Transition createTransition(UnmodifiableAttributeMap attributes) throws FlowArtifactException {
		Transition transition = (Transition)newInstance(Transition.class);
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

	private ConversionService createDefaultConversionService() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter(new TextToTransitionCriteria(this));
		service.addConverter(new TextToViewSelector(this));
		service.addConverter(new TextToTransitionTargetStateResolver(this));
		service.addConverter(new TextToExpression(getExpressionParser()));
		service.addConverter(new TextToMethodSignature(service));
		return service;
	}

	private Action beanToAction(BeanInvokingActionParameters parameters) {
		if (isStatefulAction(parameters.getId())) {
			return parameters.createStatefulAction(getServiceRegistry(), getResultEventFactorySelector(),
					getConversionService());
		}
		else {
			return parameters.createAction(getServiceRegistry().getBean(parameters.getId()),
					getResultEventFactorySelector(), getConversionService());
		}
	}

	private Object newInstance(Class artifactType) {
		return BeanUtils.instantiateClass(artifactType);
	}
}