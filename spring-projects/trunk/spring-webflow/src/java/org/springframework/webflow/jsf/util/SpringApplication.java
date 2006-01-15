/*
 * Copyright 2005-2006 the original author or authors.
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

package org.springframework.webflow.jsf.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Spring-specific implementation of the standard JSF Application interface.
 * Has the ability to obtain Spring-managed Converters, Validators, and
 * UIComponents in the case that they are not configured in the JSF environment.
 * <p>
 * For all other behavior, this class delegates to the original JSF Application
 * implementation.
 * <p>
 * 
 * @author Rick Hightower
 * @author Ken Weiner
 * @author Colin Sampaleanu
 */
public class SpringApplication extends Application {

	protected final Log logger = LogFactory.getLog(getClass());

	private Application originalApplication;

	public SpringApplication() {
		super();
	}

	public SpringApplication(Application originalApplication) {
		this.originalApplication = originalApplication;
	}

	public ActionListener getActionListener() {
		return this.originalApplication.getActionListener();
	}

	public void setActionListener(ActionListener listener) {
		this.originalApplication.setActionListener(listener);
	}

	public Locale getDefaultLocale() {
		return this.originalApplication.getDefaultLocale();
	}

	public void setDefaultLocale(Locale locale) {
		this.originalApplication.setDefaultLocale(locale);
	}

	public String getDefaultRenderKitId() {
		return this.originalApplication.getDefaultRenderKitId();
	}

	public void setDefaultRenderKitId(String renderKitId) {
		this.originalApplication.setDefaultRenderKitId(renderKitId);
	}

	public String getMessageBundle() {
		return this.originalApplication.getMessageBundle();
	}

	public void setMessageBundle(String bundle) {
		this.originalApplication.setMessageBundle(bundle);
	}

	public NavigationHandler getNavigationHandler() {
		return this.originalApplication.getNavigationHandler();
	}

	public void setNavigationHandler(NavigationHandler handler) {
		this.originalApplication.setNavigationHandler(handler);
	}

	public PropertyResolver getPropertyResolver() {
		return this.originalApplication.getPropertyResolver();
	}

	public void setPropertyResolver(PropertyResolver resolver) {
		this.originalApplication.setPropertyResolver(resolver);
	}

	public VariableResolver getVariableResolver() {
		return this.originalApplication.getVariableResolver();
	}

	public void setVariableResolver(VariableResolver resolver) {
		this.originalApplication.setVariableResolver(resolver);
	}

	public ViewHandler getViewHandler() {
		return this.originalApplication.getViewHandler();
	}

	public void setViewHandler(ViewHandler handler) {
		this.originalApplication.setViewHandler(handler);
	}

	public StateManager getStateManager() {
		return this.originalApplication.getStateManager();
	}

	public void setStateManager(StateManager manager) {
		this.originalApplication.setStateManager(manager);
	}

	public void addComponent(String componentType, String componentClass) {
		this.originalApplication.addComponent(componentType, componentClass);
	}

	/**
     * Tries to create a UI component via the original Application and looks for
     * the UI component in Spring's root application context if it is not
     * obtainable through JSF's Application.
     * 
     * @param componentType
     *            the component type and Spring bean name for the UIComponent
     * @return the resulting UIComponent through either the JSF Application or
     *         Spring Application Context.
     * @throws FacesException
     *             if the component could not be created or located in Spring
     */
	public UIComponent createComponent(String componentType) throws FacesException {
		FacesException originalException = null;
		try {
			// Create component with original application
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to create component with type '" + componentType
						+ "' using original Application");
			}
			UIComponent originalComponent = this.originalApplication
					.createComponent(componentType);
			if (originalComponent != null) {
				return originalComponent;
			}
			originalException = new FacesException(
					"Original Application returned a null component");
		} catch (FacesException e) {
			originalException = e;
		}

		// Get component from Spring root context
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to find component '" + componentType
					+ "' in root WebApplicationContext");
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		WebApplicationContext wac = getWebApplicationContext(facesContext);
		if (wac.containsBean(componentType)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully found component '" + componentType
						+ "' in root WebApplicationContext");
			}
			return (UIComponent) wac.getBean(componentType);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Could not create component '" + componentType + "'");
		}
		throw new FacesException(
				"Could not create component using original Application.  "
						+ "Also, could not find component in root WebApplicationContext",
				originalException);
	}

	/**
     * Tries to obtain a UI component though the given value binding. If a
     * non-null component is not obtained, then this method simply delegates to
     * {@link #createComponent(String)}, passing the componentType.
     * 
     * @param componentBinding
     *            the component value binding
     * @param context
     *            the FacesContext
     * @param componentType
     *            the component type
     * @return the resulting UIComponent through either the value binding, JSF
     *         Application or Spring Application Context.
     * @throws FacesException
     *             if the component could not be created or located in Spring
     */
	public UIComponent createComponent(ValueBinding componentBinding,
			FacesContext context, String componentType) throws FacesException {

		FacesException originalException = null;
		try {
			// Create component with value binding
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to create component with value binding");
			}
			Object value = componentBinding.getValue(context);
			if (value != null && value instanceof UIComponent) {
				return (UIComponent) value;
			}
			originalException = new FacesException(
					"Original Application returned a null component");
		} catch (FacesException e) {
			originalException = e;
		}

		try {
			// Value binding did not return a UIComponent, so attempt creation
            // by type
			return this.createComponent(componentType);
		} catch (FacesException e) {
			throw new FacesException(
					originalException.getMessage()
							+ " Additionally, the component could not be created based on the component type",
					e);
		}
	}

	public Iterator getComponentTypes() {
		return this.originalApplication.getComponentTypes();
	}

	public void addConverter(String converterId, String converterClass) {
		this.originalApplication.addConverter(converterId, converterClass);
	}

	public void addConverter(Class targetClass, String converterClass) {
		this.originalApplication.addConverter(targetClass, converterClass);
	}

	/**
     * Tries to create converter via the original Application and looks for the
     * converter in Spring's root application context if it is not obtainable
     * through JSF's Application.
     * 
     * @param converterId
     *            the converter ID and Spring bean name for the Converter
     * @return the resulting Converter
     * @throws FacesException
     *             if the converter could not be created or located in Spring
     */
	public Converter createConverter(String converterId) throws FacesException {
		FacesException originalException = null;
		try {
			// Create converter with original application
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to create converter with id '" + converterId
						+ "' using original Application");
			}
			Converter originalConverter = this.originalApplication
					.createConverter(converterId);
			if (originalConverter != null) {
				return originalConverter;
			}
			originalException = new FacesException(
					"Original Application returned a null Converter");
		} catch (FacesException e) {
			originalException = e;
		}

		// Get converter from Spring root context
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to find converter '" + converterId
					+ "' in root WebApplicationContext");
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		WebApplicationContext wac = getWebApplicationContext(facesContext);
		if (wac.containsBean(converterId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully found converter '" + converterId
						+ "' in root WebApplicationContext");
			}
			return (Converter) wac.getBean(converterId);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Could not create converter '" + converterId + "'");
		}
		throw new FacesException(
				"Could not create converter using original Application.  "
						+ "Also, could not find converter in root WebApplicationContext",
				originalException);
	}

	/**
     * Tries to create converter via the original Application and looks for the
     * converter in Spring's root application context if it is not obtainable
     * through JSF's Application.
     * <p>
     * When looking for the converter in Spring, the bean name will be a
     * concatenation of the fully-qualified class name of the targetClass with
     * the suffix <code>-Converter</code>. For example, if the target class
     * is <code>java.util.Locale</code>, then the corresponding converter
     * would need to have the bean name <code>java.util.Locale-Converter</code>.
     * 
     * @param targetClass
     *            the class on which the converter operates
     * @return the resulting Converter
     * @throws FacesException
     *             if the converter could not be created or located in Spring
     */
	public Converter createConverter(Class targetClass) throws FacesException {
		FacesException originalException = null;
		try {
			// Create converter with original application
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to create converter for class '"
						+ targetClass.getName() + "' using original Application");
			}
			Converter originalConverter = this.originalApplication
					.createConverter(targetClass);
			if (originalConverter != null) {
				return originalConverter;
			}
			originalException = new FacesException(
					"Original Application returned a null Converter");
		} catch (FacesException e) {
			originalException = e;
		}

		// Get converter from Spring root context
		String converterBeanName = targetClass.getName() + "-Converter";
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to find converter '" + converterBeanName
					+ "' in root WebApplicationContext");
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		WebApplicationContext wac = getWebApplicationContext(facesContext);
		if (wac.containsBean(converterBeanName)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully found converter '" + converterBeanName
						+ "' in root WebApplicationContext");
			}
			return (Converter) wac.getBean(converterBeanName);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Could not create converter for class '" + targetClass.getName()
					+ "'");
		}
		throw new FacesException(
				"Could not create converter using original Application.  "
						+ "Also, could not find converter in root WebApplicationContext",
				originalException);
	}

	public Iterator getConverterIds() {
		return this.originalApplication.getConverterIds();
	}

	public Iterator getConverterTypes() {
		return this.originalApplication.getConverterTypes();
	}

	public MethodBinding createMethodBinding(String ref, Class[] params)
			throws ReferenceSyntaxException {
		return this.originalApplication.createMethodBinding(ref, params);
	}

	public Iterator getSupportedLocales() {
		return this.originalApplication.getSupportedLocales();
	}

	public void setSupportedLocales(Collection locales) {
		this.originalApplication.setSupportedLocales(locales);
	}

	public void addValidator(String validatorId, String validatorClass) {
		this.originalApplication.addValidator(validatorId, validatorClass);
	}

	/**
     * Tries to create validator via the original Application and looks for the
     * validator in Spring's root application context if it is not obtainable
     * through JSF's Application.
     * 
     * @param validatorId
     *            the validator ID and Spring bean name for the Validator
     * @return the resulting Validator
     * @throws FacesException
     *             if the validator could not be created or located in Spring
     */
	public Validator createValidator(String validatorId) throws FacesException {
		FacesException originalException = null;
		try {
			// Create validator with original application
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to create validator with id '" + validatorId
						+ "' using original Application");
			}
			Validator originalValidator = this.originalApplication
					.createValidator(validatorId);
			if (originalValidator != null) {
				return originalValidator;
			}
			originalException = new FacesException(
					"Original Application returned a null Validator");
		} catch (FacesException e) {
			originalException = e;
		}

		// Get validator from Spring root context
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to find validator '" + validatorId
					+ "' in root WebApplicationContext");
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		WebApplicationContext wac = getWebApplicationContext(facesContext);
		if (wac.containsBean(validatorId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully found validator '" + validatorId
						+ "' in root WebApplicationContext");
			}
			return (Validator) wac.getBean(validatorId);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Could not create validator '" + validatorId + "'");
		}
		throw new FacesException(
				"Could not create validator using original Application.  "
						+ "Also, could not find validator in root WebApplicationContext",
				originalException);
	}

	public Iterator getValidatorIds() {
		return this.originalApplication.getValidatorIds();
	}

	public ValueBinding createValueBinding(String ref) throws ReferenceSyntaxException {
		return this.originalApplication.createValueBinding(ref);
	}

	/**
     * Retrieve the web application context to delegate bean name resolution to.
     * Default implementation delegates to FacesContextUtils.
     * 
     * @param facesContext
     *            the current JSF context
     * @return the Spring web application context
     * @see FacesContextUtils#getRequiredWebApplicationContext
     */
	protected WebApplicationContext getWebApplicationContext(FacesContext facesContext) {
		return FacesContextUtils.getRequiredWebApplicationContext(facesContext);
	}
}
