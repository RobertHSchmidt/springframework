package org.springframework.samples.petclinic

import org.springframework.beans.factory.annotation.Bean
import org.springframework.beans.factory.annotation.Configuration
import org.springframework.web.servlet.handler.HandlerExceptionResolver
import org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver


@Configuration
class GroovyConfiguration extends ConfigurationSupport{

	@Bean
	HandlerExceptionResolver exceptionResolver() {
		resolver = new SimpleMappingExceptionResolver()

	    mappings = new Properties()
		mappings[DataAccessException.name]="dataAccessFailure"
		mappings[TransactionException.name]="dataAccessFailure"
		resolver.exceptionMappings=mappings
		return resolver
	}  

	@Bean	
	PropertiesMethodNameResolver clinicControllerResolver() {
	   resolver = new PropertiesMethodNameResolver()
	   
	   resolver.urlMap = ["/welcome.htm":"welcomeHandler", "/vets.htm":"vetsHandler", "/owner.htm":"ownerHandler"]
	   return resolver
	}
	
	@Bean
	FindOwnersForm findOwnersForm() {
	   form = new FindOwnersForm()
	   
	   form.formView = "findOwnersForm"
	   form.selectView = "selectOwnerView"
	   form.successView = "ownerRedirect"
	   form.clinic = getBean("clinic")
	   
	   return form
	}
}