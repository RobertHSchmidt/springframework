package org.springframework.samples.petclinic

import org.springframework.config.java.annotation.Bean
import org.springframework.config.java.annotation.Configuration
import org.springframework.config.java.support.ConfigurationSupport
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import org.springframework.samples.petclinic.web.FindOwnersForm
import org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessException
import org.springframework.transaction.TransactionException
import org.springframework.validation.Validator
import org.springframework.samples.petclinic.Clinic
import org.springframework.config.java.annotation.ExternalBean
import org.springframework.samples.petclinic.web.AddOwnerForm
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import org.springframework.samples.petclinic.web.EditOwnerForm

@Configuration
abstract class GroovyConfiguration extends ConfigurationSupport {

	/**
	 * Convert a map to a Properties object.
	 */
	Properties convertToProps(Map map) {
  	    Properties props = new Properties()
  	    props.putAll(map)
  	    return props
	}
	
	@Bean
	HandlerExceptionResolver exceptionResolver() {
		SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver()

  	    def mappings = [(DataAccessException.class.name):"dataAccessFailure", (TransactionException.class.name):"dataAccessFailure"]
		resolver.exceptionMappings = convertToProps(mappings)
		return resolver
	}		
		

	@Bean
	MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource()
		messageSource.basename = "messages"
		return messageSource
	}
    
    @Bean
    SimpleUrlHandlerMapping urlMapping() {
	    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping()
	    def mappings = ["/welcome.htm" : "clinicController", "/vets.htm" : "clinicController", "/findOwners.htm" : "findOwnersForm", "/owner.htm" : "clinicController",
	    				"/addOwner.htm" : "addOwnerForm", "/editOwner.htm" : "editOwnerForm", "/addPet.htm" : "addPetForm", "/editPet.htm" : "editPetForm",
	    				"/addVisit.htm" : "addVisitForm"]
		mapping.mappings = convertToProps(mappings)
		return mapping
    }

	@Bean
	PropertiesMethodNameResolver clinicControllerResolver() {
	   PropertiesMethodNameResolver resolver = new PropertiesMethodNameResolver()
	   def mappings = ["/welcome.htm":"welcomeHandler", "/vets.htm":"vetsHandler", "/owner.htm":"ownerHandler"]
	   resolver.mappings = convertToProps(mappings)
	   return resolver
	}
    	
	@Bean
	FindOwnersForm findOwnersForm() {
	   FindOwnersForm form = new FindOwnersForm()
	   
	   form.formView = "findOwnersForm"
	   form.selectView = "selectOwnerView"
	   form.successView = "ownerRedirect"
	   form.clinic = getBean("clinic")
	   
	   return form
	}
	
	@Bean
	AddOwnerForm addOwnerForm() {
		AddOwnerForm form = new AddOwnerForm()
		
		form.formView = "ownerForm"
		form.successView = "ownerRedirect"
		form.validator = ownerValidator()
		form.clinic = clinic()
	
		return form
	}


	@Bean
	EditOwnerForm editOwnerForm() {
		EditOwnerForm form = new EditOwnerForm()

		form.formView = "ownerForm"
		form.successView = "ownerRedirect"
		form.validator = ownerValidator()
		form.clinic = clinic()
		
		return form
	}
	
	
	
	/**
	 * The abstract method will be populated at runtime with a bean factory lookup
	 */
    @ExternalBean
    abstract Clinic clinic()
    
    @ExternalBean
    abstract Validator ownerValidator()

}