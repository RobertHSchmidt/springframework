package org.springframework.config.java.context;

import org.springframework.config.java.process.NewConfigurationPostProcessor;

/**
 * Marker interface indicating that a BFPP is JavaConfig internal and
 * should not be copied to child bean factories
 *
 * @see JavaConfigBeanFactory
 * @see InternalBeanFactoryEstablishingBeanFactoryPostProcessor
 * @see NewConfigurationPostProcessor
 */
public interface JavaConfigInternalPostProcessor {

}
