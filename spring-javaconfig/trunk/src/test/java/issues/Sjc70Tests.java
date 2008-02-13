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
