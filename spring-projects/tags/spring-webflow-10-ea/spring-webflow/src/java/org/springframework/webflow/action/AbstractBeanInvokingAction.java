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
package org.springframework.webflow.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.method.MethodInvoker;
import org.springframework.binding.method.MethodSignature;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.support.EventFactory;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Base class for actions that delegate to methods on abritrary beans. Acts as
 * an adapter that adapts an Object's method to the SWF Action contract.
 * <p>
 * The method to invoke is determined by the value of the
 * {@link org.springframework.webflow.AnnotatedAction#METHOD_PROPERTY} action
 * execution property, typically set when provisioning this Action's use as part
 * of an {@link org.springframework.webflow.ActionState}.
 * 
 * @author Keith Donald
 */
public abstract class AbstractBeanInvokingAction extends AbstractAction {

	/**
	 * The method invoker that performs the action-to-bean method binding.
	 */
	private MethodInvoker methodInvoker = new MethodInvoker();

	/**
	 * The strategy that saves and restores stateful bean fields in flow scope.
	 */
	private BeanStatePersister statePersister = new NoOpBeanStatePersister();

	/**
	 * The strategy that adapts method return values to Event objects.
	 */
	private EventFactory eventFactory = new DefaultBeanReturnValueEventFactory();

	/**
	 * Returns the bean state management strategy used by this action.
	 */
	protected BeanStatePersister getStatePersister() {
		return statePersister;
	}

	/**
	 * Set the conversion service to perform type conversion of event parameters
	 * to method arguments as neccessary.
	 */
	public void setConversionService(ConversionService conversionService) {
		methodInvoker.setConversionService(conversionService);
	}

	/**
	 * Set the bean state management strategy.
	 */
	public void setStatePersister(BeanStatePersister statePersister) {
		this.statePersister = statePersister;
	}

	/**
	 * Returns the event adaption strategy used by this action.
	 */
	protected EventFactory getEventFactory() {
		return eventFactory;
	}

	/**
	 * Set the return value -> event adaption strategy.
	 */
	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	/**
	 * Returns the bean method invoker helper.
	 */
	protected MethodInvoker getMethodInvoker() {
		return methodInvoker;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBean(context);
		getStatePersister().restoreState(bean, context);
		MethodSignature methodKey = (MethodSignature)context.getAttributes().getRequired(AnnotatedAction.METHOD_PROPERTY,
				MethodSignature.class);
		Object returnValue = getMethodInvoker().invoke(methodKey, bean, context);
		processMethodReturnValue(returnValue, context);
		Event resultEvent = getEventFactory().createEvent(returnValue, context);
		getStatePersister().saveState(bean, context);
		return resultEvent;
	}

	/**
	 * Retrieves the bean to invoke a method on. Subclasses need to implement
	 * this method.
	 */
	protected abstract Object getBean(RequestContext context);

	/**
	 * Template method for post processing the invoked method's return value.
	 * This implementation exposes the return value as an attribute in a
	 * configured scope, if necessary. Subclasses may override.
	 * @param returnValue the return value
	 * @param context the request context
	 */
	protected void processMethodReturnValue(Object returnValue, RequestContext context) {
		String resultName = context.getAttributes().getString(AnnotatedAction.RESULT_NAME_PROPERTY, null);
		if (resultName != null) {
			ScopeType scopeType = (ScopeType)context.getAttributes().get(
					AnnotatedAction.RESULT_SCOPE_PROPERTY, ScopeType.REQUEST);
			scopeType.getScope(context).put(resultName, returnValue);
		}
	}

	/**
	 * State persister that doesn't take any action - default implementation.
	 * 
	 * @author Keith Donald
	 */
	public static class NoOpBeanStatePersister implements BeanStatePersister {
		public void restoreState(Object bean, RequestContext context) throws Exception {
		}

		public void saveState(Object bean, RequestContext context) throws Exception {
		}
	}

	/**
	 * Default implementation of the event adapter interface.
	 * @author Keith Donald
	 */
	public static class DefaultBeanReturnValueEventFactory extends EventFactorySupport implements EventFactory {

		private static final String NULL_EVENT_ID = "null";

		private static final String JAVA_LANG_ENUM_CLASSNAME = "java.lang.Enum";

		private static Class java5EnumClass;

		private static Method java5EnumNameMethod;

		static {
			try {
				java5EnumClass = Class.forName(JAVA_LANG_ENUM_CLASSNAME);
				try {
					java5EnumNameMethod = java5EnumClass.getMethod("name", null);
				}
				catch (NoSuchMethodException e) {
					throw new RuntimeException("Should not happen on JDK 1.5");
				}
			}
			catch (ClassNotFoundException ex) {
			}
		}

		public Event createEvent(Object resultObject, RequestContext context) {
			if (resultObject instanceof Event) {
				return (Event)resultObject;
			}
			if (context.getCurrentState() instanceof DecisionState) {
				return toDecisionStateEvent(resultObject, context);
			}
			else {
				// simply return success, saving the return value as an event
				// parameter
				String resultParameterName = context.getAttributes().getString(RESULT_PARAMETER, RESULT_PARAMETER);
				return success(resultParameterName, resultObject);
			}
		}

		/**
		 * Called when this action is invoked by a decision state - adapts the
		 * invoked method's return value to an event identifier the decision
		 * state can respond to.
		 * @param context the request context
		 * @param resultObject the return value
		 * @return the decision event
		 */
		protected Event toDecisionStateEvent(Object resultObject, RequestContext context) {
			if (resultObject == null) {
				return result(NULL_EVENT_ID, RESULT_PARAMETER, null);
			}
			if (resultObject instanceof Boolean) {
				return yesOrNo(((Boolean)resultObject).booleanValue());
			}
			else {
				// handle special event adaption for enum return values
				if (java5EnumClass != null && java5EnumClass.equals(resultObject.getClass())) {
					return jdk5EnumResult(resultObject);
				}
				else if (resultObject instanceof LabeledEnum) {
					String resultId = ((LabeledEnum)resultObject).getLabel();
					return result(resultId, RESULT_PARAMETER, resultObject);
				}
			}
			return result(String.valueOf(resultObject), RESULT_PARAMETER, resultObject);
		}

		protected Event jdk5EnumResult(Object returnEnumValue) {
			try {
				String resultEventId = (String)java5EnumNameMethod.invoke(returnEnumValue, null);
				return result(resultEventId, RESULT_PARAMETER, returnEnumValue);
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException("Should not happen on JDK 1.5");
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException("Should not happen on JDK 1.5");
			}
		}
	}
}