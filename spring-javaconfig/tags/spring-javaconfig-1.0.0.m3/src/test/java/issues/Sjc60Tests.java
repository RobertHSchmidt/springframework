/*
 * Copyright 2002-2008 the original author or authors.
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
package issues;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SJC-60 is an open bug that demonstrates JavaConfig calling refresh() multiple
 * times in the hierarchical context situation below.
 * 
 * @author Serge Bogatyrev
 * @author Chris Beams
 */
public class Sjc60Tests {

	// work in progress
	@Ignore
	@Test
	public void repro() {
		ApplicationContext parent = new ClassPathXmlApplicationContext("issues/Sjc60-parent.xml");
		new ClassPathXmlApplicationContext(new String[] { "issues/Sjc60-child.xml" }, parent);
	}

	@Test
	public void reproWithParentAsJavaConfigApplicationContext() {
		ApplicationContext parent = new JavaConfigApplicationContext(ApplicationConfiguration.class);
		new ClassPathXmlApplicationContext(new String[] { "issues/Sjc60-child.xml" }, parent);
	}

	@Test
	public void reproWithChildAsJavaConfigApplicationContext() {
		ApplicationContext parent = new ClassPathXmlApplicationContext("issues/Sjc60-parent.xml");
		new JavaConfigApplicationContext(parent, ApplicationConfiguration2.class);
	}

	@Test
	public void reproWithBothContextsAsJavaConfigApplicationContext() {
		ApplicationContext parent = new JavaConfigApplicationContext(ApplicationConfiguration.class);
		new JavaConfigApplicationContext(parent, ApplicationConfiguration2.class);
	}

	@Configuration
	public static class ApplicationConfiguration {

		@Bean
		public Object foo() {
			return "Hello!";
		}

	}

	@Configuration
	public static class ApplicationConfiguration2 {

		@Bean
		public Object foo2() {
			return "Hello!";
		}

	}
}
