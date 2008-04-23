package org.springframework.config.java.context;

import static java.lang.String.format;
import static org.springframework.util.Assert.notNull;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class CglibConfigurationEnhancer implements ConfigurationEnhancer {
	private static final Log log = LogFactory.getLog(CglibConfigurationEnhancer.class);
	private final BeanFactory beanFactory;

	public CglibConfigurationEnhancer(BeanFactory beanFactory) {
		notNull(beanFactory, "beanFactory must be non-null");
		this.beanFactory = beanFactory;
	}

	public String enhance(String configClassName) {
		if(log.isInfoEnabled())
			log.info("Enhancing " + configClassName);

		Class<?> configClass = loadClassFromName(configClassName);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(configClass);
		enhancer.setUseFactory(false);
		enhancer.setCallbackFilter(new CallbackFilter() {
			public int accept(Method candidateMethod) {
				if(AnnotationUtils.findAnnotation(candidateMethod, ExternalBean.class) != null)
					return 1;
				return 0;
			}
		});
		enhancer.setCallbackTypes(new Class<?>[] { NoOp.class, ExternalBeanMethodInterceptor.class });

		Class<?> enhancedSubclass = enhancer.createClass();

		Enhancer.registerCallbacks(enhancedSubclass, new Callback[] { NoOp.INSTANCE, new ExternalBeanMethodInterceptor(beanFactory) });

		if(log.isDebugEnabled())
			log.debug(format("Successfully enhanced %s; enhanced class name is: %s",
			                  configClassName, enhancedSubclass.getName()));

		return enhancedSubclass.getName();
	}

	private Class<?> loadClassFromName(String configClassName) {
		// TODO: handle exception more gracefully
		try {
			return Class.forName(configClassName);
		}
		catch (ClassNotFoundException ex) { throw new RuntimeException(ex); }
	}

	public static class ExternalBeanMethodInterceptor implements MethodInterceptor {
		private final BeanFactory beanFactory;

		public ExternalBeanMethodInterceptor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

    	public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
    		final String name;

    		ExternalBean extBean = AnnotationUtils.findAnnotation(m, ExternalBean.class);
    		Assert.notNull(extBean, "ExternalBean methods must be annotated with @ExternalBean");

    		String alternateName = extBean.value();
    		if(StringUtils.hasLength(alternateName))
    			name = alternateName;
    		else
    			name = m.getName();

    		return beanFactory.getBean(name);
    	}
	}

}
