package org.springframework.webflow.action.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.BeanStatePersister;

/**
 * Uses annotations to determine which beans are stateful and which fields on
 * those stateful beans should be saved and restored.
 * @author Keith Donald
 */
public class AnnotationBeanStatePersister implements BeanStatePersister {

	public void saveState(Object bean, RequestContext context) throws Exception {
		if (!bean.getClass().isAnnotationPresent(Stateful.class)) {
			return;
		}
		Stateful stateful = bean.getClass().getAnnotation(Stateful.class);
		Map memento = (Map)context.getFlowScope().getAttribute(stateful.name());
		Field[] fields = bean.getClass().getDeclaredFields();
		if (memento == null) {
			memento = new HashMap(fields.length);
			context.getFlowScope().setAttribute(stateful.name(), memento);
		}
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!field.isAnnotationPresent(Transient.class)) {
				field.setAccessible(true);
				memento.put(field.getName(), field.get(bean));
				field.setAccessible(false);
			}
		}
	}

	public void restoreState(Object bean, RequestContext context) throws Exception {
		if (!bean.getClass().isAnnotationPresent(Stateful.class)) {
			return;
		}
		Stateful stateful = bean.getClass().getAnnotation(Stateful.class);
		Map memento = (Map)context.getFlowScope().get(stateful.name());
		if (memento != null) {
			Field[] fields = bean.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (!field.isAnnotationPresent(Transient.class)) {
					field.setAccessible(true);
					field.set(bean, memento.get(field.getName()));
					field.setAccessible(false);
				}
			}
		}
	}
}