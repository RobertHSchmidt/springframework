package org.springframework.binding.method;

import java.lang.reflect.Method;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.CachingMapDecorator;

/**
 * A helper for invoking typed methods on abritrary objects, with support for
 * argument value type conversion from values retrieved from a argument
 * attribute source.
 * 
 * @author Keith Donald
 */
public class MethodInvoker {

	protected static final Log logger = LogFactory.getLog(MethodInvoker.class);

	/**
	 * Conversion service for converting arguments to the neccessary type if
	 * required.
	 */
	private ConversionService conversionService = new DefaultConversionService();

	/**
	 * A cache of invoked bean methods, keyed weakly.
	 */
	private CachingMapDecorator methodCache = new CachingMapDecorator(true) {
		public Object create(Object key) {
			return ((Signature)key).getMethod();
		}
	};

	/**
	 * Sets the conversion service to convert argument values as needed.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Invoke the method on the bean provided. Argument values are pulled from
	 * the provided argument source.
	 * 
	 * @param methodKey the definition of the method to invoke, including the
	 * method name and the method argument types
	 * @param bean the bean to invoke
	 * @param parameterValueSource the source for method parameter values
	 * @return the invoked method's return value
	 * @throws MethodInvocationException the method could not be invoked
	 */
	public Object invoke(MethodKey methodKey, Object bean, Object parameterValueSource)
			throws MethodInvocationException {
		Parameters parameters = methodKey.getParameters();
		Object[] parameterValues = new Object[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = (Parameter)parameters.getParameter(i);
			Object parameterValue = parameter.getName().evaluateAgainst(parameterValueSource, Collections.EMPTY_MAP);
			parameterValues[i] = applyTypeConversion(parameterValue, parameter.getType());
		}
		Class[] parameterTypes = parameters.getTypesArray();
		for (int i = 0; i < parameterTypes.length; i++) {
			if (parameterTypes[i] == null) {
				Object parameterValue = parameterValues[i];
				if (parameterValue != null) {
					parameterTypes[i] = parameterValue.getClass();
				}
			}
		}
		Signature signature = new Signature(bean.getClass(), methodKey.getMethodName(), parameterTypes);
		try {
			Method method = (Method)methodCache.get(signature);
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking method with signature [" + signature + "] with arguments "
						+ StylerUtils.style(parameterValues) + " on bean [" + bean + "]");

			}
			Object returnValue = method.invoke(bean, parameterValues);
			if (logger.isDebugEnabled()) {
				logger.debug("Invoked method with signature [" + signature + "]' returned value [" + returnValue + "]");
			}
			return returnValue;
		}
		catch (Exception e) {
			throw new MethodInvocationException(signature, parameterValues, e);
		}
	}

	/**
	 * Apply type conversion on the event parameter if neccessary
	 * 
	 * @param parameterValue the raw argument value
	 * @param targetType the target type for the matching method argument
	 * @return the converted method argument
	 */
	protected Object applyTypeConversion(Object parameterValue, Class targetType) {
		if (parameterValue == null || targetType == null) {
			return parameterValue;
		}
		return conversionService.getConversionExecutor(parameterValue.getClass(), targetType).execute(parameterValue);
	}
}