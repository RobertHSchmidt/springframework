package issues;

import static org.junit.Assert.*;
import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.aspects.RequiredMethodInvocationTracker;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SJC-91 deals with a compatibility issue between JavaConfig and Spring Core -
 * checking {@link Required @Required} annotations. This is difficult to do in
 * JavaConfig because the user has programmatic control over the objects during
 * dependency injection time. The only way to track whether Required methods
 * have been invoked is to do bytecode weaving, and so this has been done using
 * AspectJ
 * 
 * @see RequiredMethodInvocationTracker
 * 
 * IMPORTANT:
 * 
 * Some tests herein will fail if aspect weaving has not been performed. If
 * failures are occuring in your IDE, you should turn on load time weaving with
 * VM args:
 * 
 * -javaagent:/path/to/.m2/repository/aspectj/aspectjweaver/1.5.3/aspectjweaver-1.5.3.jar
 * -Daj.weaving.verbose=true
 * 
 * If, on the other hand, failures occur via the maven build (which should
 * always be doing build-time weaving), something more serious is going on.
 * Either the aspectj-maven-plugin has gotten misconfigured or something is
 * actually broken with processing Required annotations.
 */
public class Sjc91Tests {
	private JavaConfigApplicationContext ctx;

	@Before
	public void setUp() {
		ctx = new JavaConfigApplicationContext();
	}

	// -----------------------------------------------

	@Test
	public void completeBeanInjectionShouldNotThrow() {
		ctx.addConfigClass(CompleteBeanInjectionConfig.class);
		ctx.refresh();
	}

	@Configuration(checkRequired = true)
	static class CompleteBeanInjectionConfig {
		@Bean
		public Alice alice() {
			Alice alice = new Alice();
			// set the required method - this makes it 'complete'
			alice.setLocation("tea party");
			return alice;
		}
	}

	// -----------------------------------------------

	@Test(expected = BeanCreationException.class)
	public void incompleteBeanInjectionShouldThrow() {
		ctx.addConfigClass(IncompleteBeanInjectionConfig.class);
		ctx.refresh();
	}

	@Configuration(checkRequired = true)
	static class IncompleteBeanInjectionConfig {
		@Bean
		public Alice alice() {
			Alice alice = new Alice();
			// don't set the required method - this makes it incomplete
			// alice.setLocation("tea party");
			return alice;
		}
	}

	// -----------------------------------------------

	@Test
	public void checkRequiredExplicitlyFalse() {
		ctx.addConfigClass(CheckRequiredExplicitlyFalseConfig.class);
		ctx.refresh();
	}

	@Configuration(checkRequired = false)
	static class CheckRequiredExplicitlyFalseConfig {
		@Bean
		public Alice alice() {
			Alice alice = new Alice();
			return alice;
		}
	}

	// -----------------------------------------------

	@Test
	public void checkRequiredDefault() {
		ctx.addConfigClass(CheckRequiredDefaultConfig.class);
		ctx.refresh();
	}

	@Configuration
	static class CheckRequiredDefaultConfig {
		@Bean
		public Alice alice() {
			Alice alice = new Alice();
			return alice;
		}
	}

	// -----------------------------------------------

	@Test
	public void checkRequiredWithNoConfigurationAnnotation() {
		ctx.addConfigClass(CheckRequiredWithNoConfigurationAnnotationConfig.class);
		ctx.refresh();
	}

	static class CheckRequiredWithNoConfigurationAnnotationConfig {
		@Bean
		public Alice alice() {
			Alice alice = new Alice();
			return alice;
		}
	}

	// -----------------------------------------------

	/**
	 * The process for tracking whether required methods have been set on a
	 * given bean must be fine-grained enough to differentiate between different
	 * object instances
	 */
	@Test(expected = BeanCreationException.class)
	public void multipleBeansOfSameType() {
		ctx.addConfigClass(MultipleBeansOfSameTypeConfig.class);
		ctx.refresh();
	}

	@Configuration(checkRequired = true)
	static class MultipleBeansOfSameTypeConfig {

		@Bean
		public Alice alice1() {
			Alice alice = new Alice();
			alice.setLocation("queen's court");
			return alice;
		}

		// this one should cause an exception
		@Bean
		public Alice alice2() {
			return new Alice();
		}

		@Bean
		public Alice alice3() {
			Alice alice = new Alice();
			alice.setLocation("looking glass");
			return alice;
		}
	}

	// -----------------------------------------------

	/**
	 * Create two application contexts, where the first initializes a bean of
	 * type Alice successfully (sets all required methods) and follow this up
	 * with a second context that misconfigures an Alice bean. Expect an
	 * exception from the second. This test proves that the registry tracking
	 * mechanism is fine-grained enough to know the difference between different
	 * object instances. It is similar to the {@link #multipleBeansOfSameType()}
	 * test.
	 */
	@Test
	public void subsequentApplicationContextsShouldNotConflict() {
		boolean threw = false;

		// first should be fine
		new JavaConfigApplicationContext(CompleteBeanInjectionConfig.class);

		// second should throw
		try {
			new JavaConfigApplicationContext(IncompleteBeanInjectionConfig.class);
		}
		catch (BeanCreationException ex) {
			threw = true;
		}

		assertTrue("second context should have thrown", threw);
	}

	// -----------------------------------------------

	/**
	 * JavaConfig and Spring Core should throw the same exception and message
	 * when detecting uncalled Required methods.
	 */
	@Test
	public void compareRequiredAnnotationExceptionMessages() {
		Throwable coreEx = getCoreSpringRequiredAnnotationException();
		Throwable sjcEx = getJavaConfigRequiredAnnotationException();

		// ensure that the exception thrown by both frameworks is the same
		// if it is not, also dump the stack trace for clarity to the developer
		if (!coreEx.getClass().equals(sjcEx.getClass())) {
			sjcEx.printStackTrace();
			assertEquals(coreEx.getClass(), sjcEx.getClass());
		}

		String coreMsg = coreEx.getMessage();
		String sjcMsg = sjcEx.getMessage();

		// If core spring changes its required-property message, JavaConfig
		// should change its own as well (not likely to occur, of course).
		assertEquals("Core Spring message is different than expected",
				"Property 'location' is required for bean 'alice'", coreMsg);
		assertEquals("JavaConfig message is different than expected",
				"Method 'setLocation' is required for bean 'alice'", sjcMsg);
	}

	private Throwable getCoreSpringRequiredAnnotationException() {
		try {
			new ClassPathXmlApplicationContext("Sjc91Tests.xml", getClass());
		}
		catch (BeanCreationException t) {
			Throwable rootCause = t.getRootCause();
			assertEquals("Root cause of exception in Core Spring should have been a BeanInitializationException",
					BeanInitializationException.class, rootCause.getClass());
			return rootCause;
		}
		catch (Throwable t) {
			fail("Core Spring should have thrown a BeanCreationException, but actually got: " + t);
		}

		throw new AssertionFailedError("Core Spring should have thrown an exception");
	}

	private Throwable getJavaConfigRequiredAnnotationException() {
		try {
			ctx.addConfigClass(IncompleteBeanInjectionConfig.class);
			ctx.refresh();
		}
		catch (Throwable t) {
			return t.getCause();
		}

		throw new AssertionFailedError("JavaConfig should have thrown an exception");
	}

}

// --------------------------------------

class Alice {
	private String location;

	@Required
	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "{name=[alice], location=[" + location + "]}";
	}
}