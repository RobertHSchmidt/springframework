package org.springframework.config.java.process;

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.process.ConfigurationListener.BeanDefinitionRegistration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

public class Main {
	public static void main(String... args) {
		ConfigurationProcessor2 processor = new ConfigurationProcessor2();

		MyConfig config = new MyConfig();

		processor.process(config);
	}
}

@Configuration
class MyConfig {
	@Bean
	public TestBean beanMethod() {
		return new TestBean("aBean");
	}

	public TestBean nonBeanMethod() {
		return new TestBean("aNonBean");
	}

}

interface Reactor {
	void sourceEvent(Event event);
}

interface Event {
	Object getSource();
}

abstract class EventSupport implements Event {
	protected final Object source;

	EventSupport(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}
}

class MethodEvent extends EventSupport {

	final Method method;

	final Class<?> clazz;

	MethodEvent(Object source, Class<?> clazz, Method method) {
		super(source);
		this.clazz = clazz;
		this.method = method;
	}

	@Override
	public String toString() {
		return format("{%s:source=%s,method=%s.%s}", this.getClass().getSimpleName(), source, method
				.getDeclaringClass().getSimpleName(), method.getName());
	}

}

class BeanMethodEvent extends MethodEvent {

	final Bean beanAnnotation;

	final BeanDefinitionRegistration beanDefinitionRegistration;

	BeanMethodEvent(Object source, Class<?> clazz, Method method, Bean beanAnnotation, BeanDefinitionRegistration bdr) {
		super(source, clazz, method);
		this.beanAnnotation = beanAnnotation;
		this.beanDefinitionRegistration = bdr;
	}

}

class ClassEvent extends EventSupport {

	final Class<?> clazz;

	public ClassEvent(Object source, Class<?> clazz) {
		super(source);
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return format("{%s:source=%s,class=%s}", this.getClass().getSimpleName(), source, clazz.getName());
	}
}

class ListenerRegistry {
	private static final List<ConfigurationListener2> listeners = new ArrayList<ConfigurationListener2>();
	static {
		listeners.add(new StandardBeanConfigurationListener2());
		listeners.add(new OtherListener());
	}

	public List<ConfigurationListener2> getConfigurationListeners() {
		return listeners;
	}
}

class ConfigurationProcessor2 implements Reactor {
	private static final Logger log = Logger.getLogger(ConfigurationProcessor.class);

	private final ListenerRegistry listenerRegistry = new ListenerRegistry();

	public void process(Object bean) {
		final Class<?> clazz = bean.getClass();

		sourceEvent(new ClassEvent(this, clazz));

		ReflectionUtils.doWithMethods(clazz, new MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				if (m.getDeclaringClass().equals(Object.class))
					return; // skip Object methods for performance

				sourceEvent(new MethodEvent(ConfigurationProcessor2.this, clazz, m));
			}
		});

	}

	public void sourceEvent(Event event) {
		Object source = event.getSource();
		log.info(format("sourcing event %s from %s", event, source));
		for (ConfigurationListener2 listener : listenerRegistry.getConfigurationListeners())
			if (listener == source)
				log.info(format("skipping listener %s: it is the object that sourced this event", listener));
			else
				listener.handleEvent(this, event);
	}

	@Override
	public String toString() {
		return String.format("{%s}", this.getClass().getSimpleName());
	}
}

interface ConfigurationListener2 {

	void handleEvent(Reactor processor, Event event);

	void handleEvent(Reactor reactor, MethodEvent event);

	void handleEvent(Reactor reactor, BeanMethodEvent event);

	boolean understands(Class<?> clazz);

}

class StandardBeanConfigurationListener2 implements ConfigurationListener2 {
	private static final Logger log = Logger.getLogger(StandardBeanConfigurationListener.class);

	public void handleEvent(Reactor reactor, Event event) {
		log.info("got event: " + event);
		if (event instanceof MethodEvent) {
			MethodEvent methodEvent = (MethodEvent) event;
			Bean beanAnnotation = AnnotationUtils.findAnnotation(methodEvent.method, Bean.class);
			if (beanAnnotation != null) {
				reactor.sourceEvent(new BeanMethodEvent(this, methodEvent.clazz, methodEvent.method, beanAnnotation,
						null));
			}
		}
	}

	public boolean understands(Class<?> clazz) {
		return false;
	}

	@Override
	public String toString() {
		return String.format("{%s}", this.getClass().getSimpleName());
	}

	public void handleEvent(Reactor reactor, MethodEvent event) {
		throw new UnsupportedOperationException();
	}

	public void handleEvent(Reactor reactor, BeanMethodEvent event) {
		throw new UnsupportedOperationException();
	}
}

class OtherListener implements ConfigurationListener2 {
	private static final Logger log = Logger.getLogger(OtherListener.class);

	public void handleEvent(Reactor reactor, MethodEvent event) {
		throw new UnsupportedOperationException();
	}

	public void handleEvent(final Reactor reactor, final Event event) {
		log.info("got event: " + event);

		ReflectionUtils.doWithMethods(this.getClass(), new MethodCallback() {

			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				if (method.getDeclaringClass().equals(Object.class))
					return;
				log.warn("method: " + method);
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length != 2)
					return;
				log.warn(event.getClass());
				if (parameterTypes[0].isAssignableFrom(reactor.getClass())
						&& parameterTypes[1].equals(event.getClass())) {
					try {
						method.invoke(OtherListener.this, reactor, event);
					}
					catch (InvocationTargetException ex) {
						throw new RuntimeException(ex);
					}
				}
			}

		});
	}

	public void handleEvent(Reactor reactor, ClassEvent event) {
		log.error("GOT HERE!!! " + event);
	}

	public void handleEvent(Reactor reactor, BeanMethodEvent event) {
		throw new UnsupportedOperationException();
	}

	public boolean understands(Class<?> clazz) {
		return false;
	}

	@Override
	public String toString() {
		return String.format("{%s}", this.getClass().getSimpleName());
	}
}
