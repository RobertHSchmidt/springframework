/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java.template.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.web.servlet.HandlerMapping;
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
			public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				return null;
			}
		};
	}
}
