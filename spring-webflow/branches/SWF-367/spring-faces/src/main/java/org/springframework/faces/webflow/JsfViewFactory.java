package org.springframework.faces.webflow;

import java.util.Iterator;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.ValueBinding;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;

public class JsfViewFactory implements ViewFactory {

	private final FacesContextFactory facesContextFactory;

	private final Lifecycle facesLifecycle;

	private final String viewName;

	public JsfViewFactory(FacesContextFactory facesContextFactory, Lifecycle facesLifecycle, String viewName) {

		this.facesContextFactory = facesContextFactory;
		this.facesLifecycle = facesLifecycle;
		this.viewName = viewName;
	}

	public View getView(RequestContext context) {

		boolean restored = false;

		notifyBeforeListeners(context, PhaseId.RESTORE_VIEW);

		JsfView view;

		ViewHandler handler = getFacesContext(context).getApplication().getViewHandler();

		if (viewExists(context)) {
			view = new JsfView(getFacesContext(context).getViewRoot());
			restored = true;
		} else {
			UIViewRoot root = handler.restoreView(getFacesContext(context), viewName);
			if (root != null) {
				view = new JsfView(root);
				restored = true;
			} else {
				view = new JsfView(handler.createView(getFacesContext(context), viewName));
				restored = false;
			}
		}

		getFacesContext(context).setViewRoot(view.getViewRoot());

		processBindings(view.getViewRoot());

		notifyAfterListeners(context, PhaseId.RESTORE_VIEW);

		if (restored && !getFacesContext(context).getResponseComplete()
				&& !getFacesContext(context).getRenderResponse()) {
			facesLifecycle.execute(getFacesContext(context));
			getFacesContext(context).renderResponse();
		}

		return view;
	}

	private boolean viewExists(RequestContext context) {
		if (getFacesContext(context).getViewRoot() != null
				&& getFacesContext(context).getViewRoot().getViewId().equals(viewName)) {
			return true;
		}
		return false;
	}

	private void notifyAfterListeners(RequestContext context, PhaseId phaseId) {
		PhaseEvent afterPhaseEvent = new PhaseEvent(getFacesContext(context), phaseId, facesLifecycle);
		for (int i = 0; i < facesLifecycle.getPhaseListeners().length; i++) {
			PhaseListener listener = facesLifecycle.getPhaseListeners()[i];
			if (listener.getPhaseId() == phaseId || listener.getPhaseId() == PhaseId.ANY_PHASE) {
				listener.afterPhase(afterPhaseEvent);
			}
		}
	}

	private void notifyBeforeListeners(RequestContext context, PhaseId phaseId) {
		PhaseEvent beforePhaseEvent = new PhaseEvent(getFacesContext(context), phaseId, facesLifecycle);
		for (int i = 0; i < facesLifecycle.getPhaseListeners().length; i++) {
			PhaseListener listener = facesLifecycle.getPhaseListeners()[i];
			if (listener.getPhaseId() == phaseId || listener.getPhaseId() == PhaseId.ANY_PHASE) {
				listener.beforePhase(beforePhaseEvent);
			}
		}
	}

	private void processBindings(UIComponent component) {

		ValueBinding binding = (ValueBinding) component.getValueBinding("binding");
		if (binding != null) {
			binding.setValue(FacesContext.getCurrentInstance(), component);
		}

		Iterator i = component.getChildren().iterator();
		while (i.hasNext()) {
			UIComponent child = (UIComponent) i.next();
			processBindings(component);
		}
	}

	private FacesContext getFacesContext(RequestContext requestContext) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			facesContext = facesContextFactory.getFacesContext(requestContext.getExternalContext().getContext(),
					requestContext.getExternalContext().getRequest(),
					requestContext.getExternalContext().getResponse(), facesLifecycle);
		}
		return facesContext;
	}
}
