package org.springframework.config.java.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Primarily exists to test equals() implementation of model objects
 *
 * @author Chris Beams
 */
public class ConfigurationModel_EquivalenceTests {


	public @Test void emptyModelsAreEqual() {
		ConfigurationModel modelA = new ConfigurationModel();
		ConfigurationModel modelB = new ConfigurationModel();

		assertThat(modelA, equalTo(modelB));
	}

	public @Test void emptyVsNonEmptyModelsAreNotEqual() {
		ConfigurationModel empty = new ConfigurationModel();
		ConfigurationModel nonEmpty = new ConfigurationModel().addConfigurationClass(new ConfigurationClass("a"));

		assertThat(empty, not(equalTo(nonEmpty)));
	}

	public @Test void equivalentModelsAreEqual() {
		ConfigurationModel modelA = new ConfigurationModel().addConfigurationClass(new ConfigurationClass("a"));
		ConfigurationModel modelB = new ConfigurationModel().addConfigurationClass(new ConfigurationClass("a"));

		assertThat(modelA, equalTo(modelB));
	}

	public @Test void nonEquivalentModelsAreNotEqual() {
		ConfigurationModel modelA = new ConfigurationModel().addConfigurationClass(new ConfigurationClass("a"));
		ConfigurationModel modelB = new ConfigurationModel().addConfigurationClass(new ConfigurationClass("b"));

		assertThat(modelA, not(equalTo(modelB)));
	}

	public @Test void equivalentDeepModelsAreEqual() {
		class util {
			ConfigurationModel createDeepModel() {
				return new ConfigurationModel()
					.addConfigurationClass(new ConfigurationClass("classX")
						.addBeanMethod(new BeanMethod("methodX")));
			}
		}
		ConfigurationModel modelA = new util().createDeepModel();
		ConfigurationModel modelB = new util().createDeepModel();

		assertThat(modelA, not(sameInstance(modelB)));
		assertThat(modelA, equalTo(modelB));
	}

	public @Test void differentlyOrderedModelsAreNotEqual() {
		ConfigurationModel modelA = new ConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classA"))
			.addConfigurationClass(new ConfigurationClass("classB"));

		ConfigurationModel modelB = new ConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classB"))
			.addConfigurationClass(new ConfigurationClass("classA"));

		assertThat(modelA, not(equalTo(modelB)));
	}

	/** @see {@link ConfigurationParserTests#beanMethodOrderIsNotSignificantA() */
	public @Test void differentBeanMethodOrderingDoesNotAffectEquality() {
		ConfigurationModel modelA = new ConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classA")
				.addBeanMethod(new BeanMethod("methodA"))
				.addBeanMethod(new BeanMethod("methodB")))
			.addConfigurationClass(new ConfigurationClass("classB")
				.addBeanMethod(new BeanMethod("methodC"))
				.addBeanMethod(new BeanMethod("methodD")));

		ConfigurationModel modelB = new ConfigurationModel()
			.addConfigurationClass(new ConfigurationClass("classA")
				.addBeanMethod(new BeanMethod("methodA"))
				.addBeanMethod(new BeanMethod("methodB")))
			.addConfigurationClass(new ConfigurationClass("classB")
				.addBeanMethod(new BeanMethod("methodD")) // only difference!
				.addBeanMethod(new BeanMethod("methodC")));

		assertThat(modelA, equalTo(modelB));
	}

}
