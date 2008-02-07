package org.springframework.config.java.process;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.annotation.Import;

class ImportConfigurationListener extends ConfigurationListenerSupport {

	@Override
	public boolean understands(Class<?> configurationClass) {
		// TODO: does Import work with inheritance?
		return configurationClass.isAnnotationPresent(Import.class);
	}

	public void handleEvent(Reactor reactor, ClassEvent event) {
		ProcessingContext pc = ProcessingContext.getCurrentContext();
		ConfigurableListableBeanFactory owningBeanFactory = pc.owningBeanFactory;

		Class<?> configurationClass = event.clazz;
		Import importAnnotation = configurationClass.getAnnotation(Import.class);
		Class<?>[] configurationClassesToImport = reverse(importAnnotation.value());
		for (Class<?> configurationClassToImport : configurationClassesToImport) {
			// duplicate check - process only if we've never encountered before
			if (!owningBeanFactory.containsBeanDefinition(configurationClassToImport.getName()))
				/* TODO: SJC-63
				reactor.sourceClassEvent(new ClassEvent(this, configurationClassToImport));
				*/
				new ClassConfigurationListener().handleEvent(reactor, new ClassEvent(this, configurationClassToImport));
		}
	}

	/**
	 * Reverse the contents of <var>array</var>.
	 * 
	 * <p/>This method is used for reversing the order of classes passed into
	 * constructors of
	 * {@link org.springframework.config.java.context.JavaConfigApplicationContext}
	 * or {@link org.springframework.config.java.annotation.Import}.
	 * 
	 * <p/>TODO: shouldn't actually be necessary. Root out the real issue with
	 * ordering
	 * 
	 * @see org.springframework.config.java.context.JavaConfigApplicationContext#reverse()
	 * @param array - array to reverse
	 * @return reverse of <var>array</var>, null if <var>array</var> is null.
	 */
	private static Class<?>[] reverse(Class<?>[] array) {
		if (array == null)
			return array;

		int size = array.length;
		Class<?>[] reversed = new Class<?>[size];

		for (int i = 0; i < size; i++)
			reversed[size - i - 1] = array[i];

		return reversed;
	}

}
