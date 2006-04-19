package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.Action;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.AbstractBeanInvokingAction;
import org.springframework.webflow.action.BeanFactoryBeanInvokingAction;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.action.MementoBeanStatePersister;
import org.springframework.webflow.action.MementoOriginator;
import org.springframework.webflow.action.ResultEventFactory;
import org.springframework.webflow.action.ResultSpecification;
import org.springframework.webflow.action.StatefulBeanInvokingAction;

/**
 * Extension of {@link FlowArtifactParameters} to support initializing the
 * configuration of a {@link AbstractBeanInvokingAction}. Used only during flow
 * construction/configuration time.
 * 
 * Also acts as a factory, see the
 * {@link #createAction(Object, ResultEventFactory, ConversionService)} and
 * {@link #createStatefulAction(Object, BeanFactory, ResultEventFactory, ConversionService)}
 * factory methods.
 * 
 * @author Keith Donald
 */
public class BeanInvokingActionParameters extends FlowArtifactParameters {

	/**
	 * Instructions about what bean method to invoke.
	 */
	private MethodSignature methodSignature;

	/**
	 * Instructions about what bean method to invoke.
	 */
	private ResultSpecification resultSpecification;

	/**
	 * The scope to expose the invoked bean in (optional).
	 */
	private ScopeType beanScope;

	/**
	 * Creates a new parameter object.
	 * @param id the action id
	 * @param methodParameters the method parameters
	 * @param beanScope the bean scope
	 * @param customAttributes custom initialization attributes
	 */
	public BeanInvokingActionParameters(String id, MethodSignature methodSignature,
			ResultSpecification resultSpecification, ScopeType beanScope, AttributeCollection customAttributes) {
		super(id, customAttributes);
		this.methodSignature = methodSignature;
		this.resultSpecification = resultSpecification;
		this.beanScope = beanScope;
	}

	public MethodSignature getMethodSignature() {
		return methodSignature;
	}

	public ResultSpecification getResultSpecification() {
		return resultSpecification;
	}

	public ScopeType getBeanScope() {
		return beanScope;
	}

	/**
	 * Factory method that creates a new local bean invoking action.
	 * @param bean
	 * @param resultEventFactorySelector
	 * @param conversionService
	 * @return the action
	 */
	public AbstractBeanInvokingAction createAction(Object bean, ResultEventFactorySelector resultEventFactorySelector,
			ConversionService conversionService) {
		LocalBeanInvokingAction action = new LocalBeanInvokingAction(methodSignature, bean);
		configureCommonProperties(action, bean.getClass(), resultEventFactorySelector, conversionService);
		return action;
	}

	/**
	 * Factory method that creates a new stateful bean invoking action.
	 * @param bean the bean 
	 * @param beanRegistry
	 * @param resultEventFactorySelector
	 * @param conversionService
	 * @return the action
	 */
	public Action createStatefulAction(BeanFactory beanRegistry,
			ResultEventFactorySelector resultEventFactorySelector, ConversionService conversionService) {
		Class beanClass = beanRegistry.getType(getId());
		if (MementoOriginator.class.isAssignableFrom(beanClass)) {
			BeanFactoryBeanInvokingAction action = new BeanFactoryBeanInvokingAction(methodSignature, getId(),
					beanRegistry);
			action.setBeanStatePersister(new MementoBeanStatePersister());
			configureCommonProperties(action, beanClass, resultEventFactorySelector, conversionService);
			return action;
		}
		else {
			StatefulBeanInvokingAction action = new StatefulBeanInvokingAction(methodSignature, getId(), beanRegistry);
			action.setBeanScope(beanScope);
			configureCommonProperties(action, beanClass, resultEventFactorySelector, conversionService);
			return action;
		}
	}

	private void configureCommonProperties(AbstractBeanInvokingAction action, Class beanClass,
			ResultEventFactorySelector resultEventFactorySelector, ConversionService conversionService) {
		action.setResultSpecification(resultSpecification);
		action.setResultEventFactory(resultEventFactorySelector.forMethod(methodSignature, beanClass));
		action.setConversionService(conversionService);
	}
}