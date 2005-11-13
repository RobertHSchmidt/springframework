package org.springframework.webflow.config;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.webflow.Flow;

/**
 * Abstract base implementation of a flow builder defining common functionality
 * needed by most concrete flow builder implementations.
 * <p>
 * The Flow definition implementation created by this builder may be customized
 * by configuring a custom {@link FlowCreator}.
 * <p>
 * Subclasses may delegate to a configured {@link FlowArtifactFactory} to
 * resolve any externally managed flow artifacts the flow being built depends on
 * (actions, subflows, etc.)
 * 
 * @see org.springframework.webflow.config.FlowCreator
 * @see org.springframework.webflow.config.FlowArtifactFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class BaseFlowBuilder implements FlowBuilder {

	/**
	 * A logger instance that can be used in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The <code>Flow</code> built by this builder.
	 */
	private Flow flow;

	/**
	 * Creates the implementation of the flow built by this builder.
	 */
	private FlowCreator flowCreator = new DefaultFlowCreator();

	/**
	 * Locates actions, attribute mappers, and other artifacts usable by the
	 * flow built by this builder.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * The conversion service to convert to flow-related artifacts, typically
	 * from string encoded representations.
	 */
	private ConversionService conversionService;

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
	}

	/**
	 * Creates a flow builder using the locator to link in artifacts
	 * @param flowArtifactFactory the flow artifact locator.
	 */
	protected BaseFlowBuilder(FlowArtifactFactory flowArtifactFactory) {
		setFlowArtifactFactory(flowArtifactFactory);
	}

	/**
	 * Returns the flow creator.
	 */
	protected FlowCreator getFlowCreator() {
		return flowCreator;
	}

	/**
	 * Sets the flow creator.
	 */
	public void setFlowCreator(FlowCreator flowCreator) {
		this.flowCreator = flowCreator;
	}

	/**
	 * Returns the artifact locator.
	 */
	protected FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	/**
	 * Returns the artifact locator
	 * @throws an IllegalStateException if the artifact locator is not set
	 */
	protected FlowArtifactFactory getRequiredFlowArtifactFactory() {
		if (flowArtifactFactory == null) {
			throw new IllegalStateException("The 'flowArtifactFactory' property must be set before you can use it to "
					+ "load actions, attribute mappers, subflows, and other Flow artifacts needed by this builder");
		}
		return getFlowArtifactFactory();
	}

	/**
	 * Sets the artifact locator.
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactLocator) {
		this.flowArtifactFactory = flowArtifactLocator;
	}

	/**
	 * Returns the conversion service.
	 */
	protected ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Sets the conversion service.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Initialize this builder's conversion service and register default
	 * converters. Called by subclasses who wish to use the conversion
	 * infrastructure.
	 */
	protected void initConversionService() {
		if (getConversionService() == null) {
			DefaultConversionService service = new DefaultConversionService();
			service.addConverter(new TextToTransitionCriteria(getFlowArtifactFactory()));
			service.addConverter(new TextToViewSelector(getFlowArtifactFactory(), service));
			setConversionService(service);
		}
	}

	/**
	 * Returns a conversion executor capable of converting string objects to the
	 * target class aliased by the provided alias.
	 * @param targetAlias the target class alias, e.g "long" or "float"
	 * @return the conversion executor, or <code>null</code> if no suitable
	 * converter exists for given alias
	 */
	protected ConversionExecutor fromStringToAliased(String targetAlias) {
		return getConversionService().getConversionExecutorByTargetAlias(String.class, targetAlias);
	}

	/**
	 * Returns a converter capable of converting a string value to the given
	 * type.
	 * @param targetType the type you wish to convert to (from a string)
	 * @return the converter
	 * @throws ConversionException when the converter cannot be found
	 */
	protected ConversionExecutor fromStringTo(Class targetType) throws ConversionException {
		return getConversionService().getConversionExecutor(String.class, targetType);
	}

	/**
	 * Get the flow (result) built by this builder.
	 */
	protected Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow being built by this builder.
	 */
	protected void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Flow getResult() {
		getFlow().resolveStateTransitionsTargetStates();
		return getFlow();
	}
}