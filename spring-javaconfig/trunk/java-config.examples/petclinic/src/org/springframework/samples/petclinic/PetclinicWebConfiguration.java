/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.samples.petclinic;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.java.template.ConfigurationSupport;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.validation.OwnerValidator;
import org.springframework.samples.petclinic.validation.PetValidator;
import org.springframework.samples.petclinic.validation.VisitValidator;
import org.springframework.samples.petclinic.web.ClinicController;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;

/**
 * @author Costin Leau
 * 
 */
@Configuration(defaultAutowire = Autowire.NO)
public class PetclinicWebConfiguration extends ConfigurationSupport {

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		source.setBasename("messages");
		return source;
	}

	@Bean
	public HandlerExceptionResolver exceptionResolver() {
		SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();

		Properties mappings = new Properties();
		mappings.put(DataAccessException.class.getName(), "dataAccessFailure");
		mappings.put(TransactionException.class.getName(), "dataAccessFailure");
		resolver.setExceptionMappings(mappings);
		return resolver;
	}

	@Bean
	public RequestToViewNameTranslator viewNameTranslator() {
		DefaultRequestToViewNameTranslator translator = new DefaultRequestToViewNameTranslator();
		translator.setSuffix("View");
		return translator;
	}

	/*
	 * do not add this as it will break petclinic (the views are not properly
	 * resolved
	 */
	/*
	 * @Bean public ViewResolver viewResolver() { ResourceBundleViewResolver
	 * resolver = new ResourceBundleViewResolver();
	 * resolver.setBasename("views"); return resolver; }
	 */

	@Bean
	public Validator visitValidator() {
		return new VisitValidator();
	}

	@Bean
	public Validator petValidator() {
		return new PetValidator();
	}

	@Bean
	public Validator ownerValidator() {
		return new OwnerValidator();
	}

	@Bean
	public Controller clinicController() {
		ClinicController ctrl = new ClinicController();
		ctrl.setMethodNameResolver((MethodNameResolver) getBean("clinicControllerResolver"));
		ctrl.setClinic((Clinic) getBean("clinic"));
		return ctrl;
	}
}
