package org.springframework.binding.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
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

	/**
	 * Conversion service for converting arguments to the neccessary type if
	 * required.
	 */
	private ConversionService conversionService = new DefaultConversionService();

	protected static final Log logger = LogFactory.getLog(MethodInvoker.class);

	/**
	 * A cache of invoked bean methods, keyed weakly.
	 */
	private CachingMapDecorator methodCache = new CachingMapDecorator(true) {
		public Object create(Object key) {
			Signature signature = (Signature)key;
			try {
				return signature.lookupMethod();
			}
			catch (NoSuchMethodException e) {
				throw new InvalidMethodSignatureException(signature, e);
			}
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
	 * @param argumentSource the source for method argument values
	 * @return the invoked method's return value
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invoke(MethodKey methodKey, Object bean, AttributeSource argumentSource)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		Object[] args = new Object[methodKey.getArguments().size()];
		Iterator it = methodKey.getArguments().iterator();
		int i = 0;
		while (it.hasNext()) {
			Argument argument = (Argument)it.next();
			args[i] = applyTypeConversion(argumentSource.getAttribute(argument.getName()), argument.getType());
			i++;
		}
		Class[] argumentTypes = methodKey.getArguments().getTypesArray();
		for (int j = 0; j < argumentTypes.length; j++) {
			if (argumentTypes[j] == null) {
				argumentTypes[j] = args[j].getClass();
			}
		}
		Signature signature = new Signature(bean.getClass(), methodKey.getMethodName(), argumentTypes);
		Method method = (Method)methodCache.get(signature);
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking method with signature: " + signature + " with arguments: " + StylerUtils.style(args) + " on bean: " + bean);
		}
		// TODO - catch and throw strongly typed unchecked exceptions here?
		Object returnValue = method.invoke(bean, args);
		if (logger.isDebugEnabled()) {
			logger.debug("Invoked method: '" + signature.getMethodName() + "', method returned value: " + returnValue);
		}
		return returnValue;
	}

	/**
	 * Apply type conversion on the event parameter if neccessary
	 * 
	 * @param rawArgument the raw argument value
	 * @param targetType the target type for the matching method argument
	 * @return the converted method argument
	 */
	protected Object applyTypeConversion(Object rawArgument, Class targetType) {
		if (rawArgument == null || targetType == null) {
			return rawArgument;
		}
		return conversionService.getConversionExecutor(rawArgument.getClass(), targetType).execute(rawArgument);
	}
}