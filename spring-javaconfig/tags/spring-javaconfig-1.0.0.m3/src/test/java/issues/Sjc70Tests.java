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
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * SJC-70 revealed the fact that {@link AutoBean} methods do not execute their
 * method body if present. This should be the default, with the returned bean
 * then being available for further (setter) autowiring. A number of other
 * issues presented themselves while writing this test; their verbosely-named
 * methods tell the tale.
 * 
 * @author Chris Beams
 * @author Jim Moore
 */
public class Sjc70Tests {
	// currently breaking
	@Ignore
	@Test
	public void autoBeanMethodsShouldFavorMethodBodyIfPresent() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(
				ConfigurationAnnotatedAutoBeanConfiguration.class);

		// fails, can't find default constructor
		ctx.getBean(GmailImapServer.class);
	}

	// currently breaking
	@Ignore
	@Test
	public void autoBeanConstructorWiringWorksWhenMethodIsAbstract() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(
				ConstructorInjectingAutoBeanConfiguration.class);

		// fails, can't find default constructor
		ctx.getBean(JdbcContactRepository.class);
	}

	// currently breaking
	@Ignore
	@Test
	public void nonConfigurationAnnotatedClassDeclaringOnlyAutoBeanMethodsMustBeEligibleForProcessing() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(
				NonConfigurationAnnotatedAutoBeanConfiguration.class);

		// fails, config class was never processed because it wasn't recognized
		// as eligible
		ctx.getBean(GmailImapServer.class);
	}

	static class NonConfigurationAnnotatedAutoBeanConfiguration {
		@AutoBean
		public GmailImapServer gmailServer() throws Exception {
			return new GmailImapServer("username", "pw");
		}
	}

	@Configuration
	static class ConfigurationAnnotatedAutoBeanConfiguration {
		@AutoBean
		public GmailImapServer gmailServer() throws Exception {
			return new GmailImapServer("username", "pw");
		}
	}

	@Configuration
	static abstract class ConstructorInjectingAutoBeanConfiguration {
		@AutoBean
		public abstract JdbcContactRepository accountRepository();

		@Bean
		public DataSource dataSource() {
			return new DataSource();
		}
	}

	static class GmailImapServer {

		@SuppressWarnings("unused")
		public GmailImapServer(String username, String password) {
		}

	}
}
