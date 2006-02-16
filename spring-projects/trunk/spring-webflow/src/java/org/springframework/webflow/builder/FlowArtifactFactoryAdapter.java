package org.springframework.webflow.builder;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
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
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.LocalBeanInvokingAction;

/**
 * Base implementation of a flow artifact factory that implements a minimal set
 * of the <code>FlowArtifactFactory</code> interface, throwing unsupported
 * operation exceptions for most operations.
 * <p>
 * May be subclassed to offer additional factory/lookup support.
 * @author Keith Donald
 */
public class FlowArtifactFactoryAdapter implements FlowArtifactFactory {

	/**
	 * A conversion service that can convert between types.
	 */
	private ConversionService conversionService;

	/**
	 * A resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader;

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
		flow.addProperties(parameters.getProperties());
		return flow;
	}

	public State createState(Flow flow, Class stateType, FlowArtifactParameters parameters)
			throws FlowArtifactException {
		State state = (State)newInstance(stateType);
		state.setId(parameters.getId());
		state.setFlow(flow);
		state.addProperties(parameters.getProperties());
		return state;
	}

	public Transition createTransition(Map properties) throws FlowArtifactException {
		Transition transition = (Transition)newInstance(Transition.class);
		transition.addProperties(properties);
		return transition;
	}

	protected Object newInstance(Class artifactType) {
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

	public ConversionService getConversionService() {
		if (conversionService == null) {
			DefaultConversionService service = new DefaultConversionService();
			service.addConverter(new TextToTransitionCriteria(this));
			service.addConverter(new TextToViewSelector(this, service));
			service.addConverter(new TextToTransitionTargetStateResolver(this));
			setConversionService(service);
		}
		return conversionService;
	}

	/**
	 * Helper method to the given service object into an action. If the given
	 * service object implements the <code>Action</code> interface, it is
	 * returned as is, otherwise it is wrapped in an action that can invoke a
	 * method on the service bean.
	 * @param artifact the service bean
	 * @return the action
	 */
	protected Action toAction(Object artifact, Map properties) {
		if (artifact instanceof Action) {
			return (Action)artifact;
		}
		else {
			return new LocalBeanInvokingAction(artifact);
		}
	}
}