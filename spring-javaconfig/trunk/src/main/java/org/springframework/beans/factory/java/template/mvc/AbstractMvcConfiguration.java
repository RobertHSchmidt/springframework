package org.springframework.beans.factory.java.template.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.java.template.ConfigurationSupport;
import org.springframework.web.portlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration(defaultAutowire = Autowire.BY_TYPE)
public abstract class AbstractMvcConfiguration extends ConfigurationSupport {
	
	@Bean
	public ViewResolver defaultViewResolver() {
		return jspResolver();
	}
	
	protected ViewResolver jspResolver() {
		InternalResourceViewResolver ivr = new InternalResourceViewResolver();
		ivr.setSuffix(".jsp");
		ivr.setPrefix("/WEB-INF/jsp");
		return ivr;
	}

	@Bean
	public HandlerMapping annotationHandlerMapping() {
		// TODO order value is one always
		throw new UnsupportedOperationException();
	}
	
	// TODO Annotation handler mapping
	
	
	
	@Bean
	@Url("/foo/bar.html")
	public Controller myController() {
		return new Controller() {
			public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
				return null;
			}
		};
	}
}
