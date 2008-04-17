package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Primarily exists to test equals() implementation of model objects
 *
 * @author Chris Beams
 */
public class JavaConfigurationModelTests {


	public @Test void emptyModelsAreEqual() {
		JavaConfigurationModel modelA = new JavaConfigurationModel();
		JavaConfigurationModel modelB = new JavaConfigurationModel();

		assertThat(modelA, equalTo(modelB));
	}

	public @Test void emptyVsNonEmptyModelsAreNotEqual() {
		JavaConfigurationModel empty = new JavaConfigurationModel();
		JavaConfigurationModel nonEmpty = new JavaConfigurationModel().addConfigurationClass(new ConfigurationClass());

		assertThat(empty, not(equalTo(nonEmpty)));
	}

	public @Test void equivalentModelsAreEqual() {
		JavaConfigurationModel modelA = new JavaConfigurationModel().addConfigurationClass(new ConfigurationClass("a"));
		JavaConfigurationModel modelB = new JavaConfigurationModel().addConfigurationClass(new ConfigurationClass("a"));

		assertThat(modelA, equalTo(modelB));
	}

	public @Test void nonEquivalentModelsAreNotEqual() {
		JavaConfigurationModel modelA = new JavaConfigurationModel().addConfigurationClass(new ConfigurationClass("a"));
		JavaConfigurationModel modelB = new JavaConfigurationModel().addConfigurationClass(new ConfigurationClass("b"));

		assertThat(modelA, not(equalTo(modelB)));
	}

	public @Test void equivalentDeepModelsAreEqual() {
		class util {
			JavaConfigurationModel createDeepModel() {
				return new JavaConfigurationModel()
					.addConfigurationClass(new ConfigurationClass("classX")
						.addBeanMethod(new BeanMethod("methodX")));
			}
		}
		JavaConfigurationModel modelA = new util().createDeepModel();
		JavaConfigurationModel modelB = new util().createDeepModel();

		assertThat(modelA, not(sameInstance(modelB)));
		assertThat(modelA, equalTo(modelB));
	}

	public @Test void differentlyOrderedModelsAreNotEqual() {
		JavaConfigurationModel modelA = new JavaConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classA"))
			.addConfigurationClass(new ConfigurationClass("classB"));

		JavaConfigurationModel modelB = new JavaConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classB"))
			.addConfigurationClass(new ConfigurationClass("classA"));

		assertThat(modelA, not(equalTo(modelB)));
	}

	/** @see {@link JavaConfigurationModelPopulatorTests#beanMethodOrderIsNotSignificantA() */
	public @Test void differentBeanMethodOrderingDoesNotAffectEquality() {
		JavaConfigurationModel modelA = new JavaConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classA")
				.addBeanMethod(new BeanMethod("methodA"))
				.addBeanMethod(new BeanMethod("methodB")))
			.addConfigurationClass(new ConfigurationClass("classB")
				.addBeanMethod(new BeanMethod("methodC"))
				.addBeanMethod(new BeanMethod("methodD")));

		JavaConfigurationModel modelB = new JavaConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classA")
				.addBeanMethod(new BeanMethod("methodA"))
				.addBeanMethod(new BeanMethod("methodB")))
			.addConfigurationClass(new ConfigurationClass("classB")
				.addBeanMethod(new BeanMethod("methodD")) // only difference!
				.addBeanMethod(new BeanMethod("methodC")));

		assertThat(modelA, equalTo(modelB));
	}

}
