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
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.Validator;
import org.springframework.samples.petclinic.validation.OwnerValidator;
import org.springframework.samples.petclinic.validation.PetValidator; 
import org.springframework.samples.petclinic.validation.VisitValidator; 
import org.springframework.samples.petclinic.web.ClinicController;
import org.springframework.samples.petclinic.Clinic;
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
	public RequestToViewNameTranslator viewNameTranslator() {
		DefaultRequestToViewNameTranslator translator = new DefaultRequestToViewNameTranslator();
		translator.setSuffix("View");
		return translator;
	}

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
		ctrl.setMethodNameResolver(clinicControllerResolver());
		ctrl.setClinic((Clinic)getBean("clinic"));
		return ctrl;
	}
	
	@ExternalBean
	public MethodNameResolver clinicControllerResolver() {
		// will never be executed
		return null;
	}
}
