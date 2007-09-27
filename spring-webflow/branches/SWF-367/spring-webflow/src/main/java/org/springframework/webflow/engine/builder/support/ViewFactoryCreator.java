package org.springframework.webflow.engine.builder.support;

import org.springframework.binding.expression.Expression;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;

public interface ViewFactoryCreator {

	public ViewFactory createViewFactory(Expression viewName);

	public Action createFinalResponseAction(Expression viewName);

}
