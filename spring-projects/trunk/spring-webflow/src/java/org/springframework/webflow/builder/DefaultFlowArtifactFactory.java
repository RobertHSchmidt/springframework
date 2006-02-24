package org.springframework.webflow.builder;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.convert.support.TextToExpression;
import org.springframework.binding.convert.support.TextToExpressions;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.method.TextToMethodSignature;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
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
import org.springframework.webflow.action.LocalBeanInvokingAction;
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
	private ExpressionParser expressionParser;

	/**
	 * A conversion service that can convert between types.
	 */
	private ConversionService conversionService;

	/**
	 * A resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader;

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

	public Flow getSubflow(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, Flow.class, "Subflow lookup is not supported by this artifact factory");
	}

	public Action getAction(FlowArtifactParameters parameters) throws FlowArtifactException {
		throw new FlowArtifactException(parameters.getId(), Action.class, "Unable to lookup action with id '"
				+ parameters.getId() + "'; action lookup is not supported by this artifact factory");
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, FlowAttributeMapper.class, "Unable to lookup attribute mapper with id '"
				+ id + "'; attribute mapper lookup is not supported by this artifact factory");
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, TransitionCriteria.class, "Unable to lookup transition criteria with id '"
				+ id + "'; transition criteria lookup is not supported by this artifact factory");
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, ViewSelector.class, "Unable to lookup view selector with id '" + id
				+ "'; view selector lookup is not supported by this artifact factory");
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, StateExceptionHandler.class, "Unable to lookup exception handler with id '"
				+ id + "'; state exception handler lookup is not supported by this artifact factory");
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, TargetStateResolver.class,
				"Unable to lookup target state resolver with id '" + id
						+ "'; transition target state resolver lookup is not supported by this artifact factory");
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

	private Object newInstance(Class artifactType) {
		return BeanUtils.instantiateClass(artifactType);
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Service registry lookup not supported by this artifact factory");
	}

	public ResourceLoader getResourceLoader() throws UnsupportedOperationException {
		if (resourceLoader == null) {
			setResourceLoader(new DefaultResourceLoader());
		}
		return resourceLoader;
	}

	public ExpressionParser getExpressionParser() {
		if (expressionParser == null) {
			setExpressionParser(new DefaultExpressionParserFactory().getExpressionParser());
		}
		return expressionParser;
	}

	public ConversionService getConversionService() {
		if (conversionService == null) {
			DefaultConversionService service = new DefaultConversionService();
			service.addConverter(new TextToTransitionCriteria(this));
			service.addConverter(new TextToViewSelector(this));
			service.addConverter(new TextToTransitionTargetStateResolver(this));
			service.addConverter(new TextToExpression(getExpressionParser()));
			service.addConverter(new TextToExpressions(getExpressionParser()));
			service.addConverter(new TextToMethodSignature(service));
			setConversionService(service);
		}
		return conversionService;
	}

	/**
	 * Helper method to the transform the given object into an {@link Action}.
	 * By default, if the given object implements the <code>Action</code>
	 * interface it is returned as is, otherwise it is wrapped in an action
	 * adapter that can invoke an arbitrary method on the object. Subclasses may
	 * override.
	 * @param artifact the object to be adapted as an action
	 * @param parameters assigned action construction parameters
	 * @return the action
	 */
	protected Action toAction(Object artifact, FlowArtifactParameters parameters) {
		if (artifact instanceof Action) {
			return (Action)artifact;
		}
		else {
			return new LocalBeanInvokingAction(artifact);
		}
	}
}