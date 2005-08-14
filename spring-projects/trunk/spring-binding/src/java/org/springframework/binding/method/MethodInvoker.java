package org.springframework.binding.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.springframework.binding.AttributeSource;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
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

	/**
	 * A cache of invoked bean methods, keyed weakly.
	 */
	private CachingMapDecorator methodCache = new CachingMapDecorator(true) {
		public Object create(Object key) {
			TypeMethodKey methodKey = (TypeMethodKey)key;
			try {
				return methodKey.lookupMethod();
			}
			catch (NoSuchMethodException e) {
				throw new InvalidMethodKeyException(methodKey, e);
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
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = (Method)methodCache.get(new TypeMethodKey(bean.getClass(), methodKey));
		Object[] args = new Object[methodKey.getArguments().size()];
		Iterator it = methodKey.getArguments().iterator();
		int i = 0;
		while (it.hasNext()) {
			Argument argument = (Argument)it.next();
			args[i] = applyTypeConversion(argumentSource.getAttribute(argument.getName()), argument.getType());
			i++;
		}
		// TODO - catch and throw strongly typed unchecked exceptions here?
		return method.invoke(bean, args);
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