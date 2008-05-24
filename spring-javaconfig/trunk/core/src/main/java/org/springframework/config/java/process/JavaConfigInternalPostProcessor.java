package org.springframework.config.java.process;

import org.springframework.config.java.factory.JavaConfigBeanFactory;

/**
 * Marker interface indicating that a BFPP is JavaConfig internal and
 * should not be copied to child bean factories
 *
 * @see JavaConfigBeanFactory
 * @see InternalBeanFactoryEstablishingBeanFactoryPostProcessor
 * @see ConfigurationPostProcessor
 */
public interface JavaConfigInternalPostProcessor {

}
