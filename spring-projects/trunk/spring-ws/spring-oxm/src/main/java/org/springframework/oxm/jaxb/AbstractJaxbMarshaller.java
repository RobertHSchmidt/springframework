/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.oxm.jaxb;

import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for implementations of the <code>Marshaller</code> and <code>Unmarshaller</code> interfaces that
 * use JAXB. This base class is responsible for creating JAXB marshallers from a <code>JAXBContext</code>.
 * <p/>
 * JAXB 2.0 added  breaking API changes, so specific subclasses must be used for JAXB 1.0 and 2.0
 * (<code>Jaxb1Marshaller</code> and <code>Jaxb2Marshaller</code> respectivaly).
 *
 * @author Arjen Poutsma
 * @see Jaxb1Marshaller
 * @see Jaxb2Marshaller
 */
public abstract class AbstractJaxbMarshaller
        implements org.springframework.oxm.Marshaller, org.springframework.oxm.Unmarshaller, InitializingBean {

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private String contextPath;

    private Map marshallerProperties;

    private Map unmarshallerProperties;

    /**
     * Returns the JAXB marshaller.
     */
    protected Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Returns the JAXB unmarshaller.
     */
    protected Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Sets the JAXB Context path.
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Sets the JAXB <code>Marshaller</code> properties. These properties will be set on the underlying JAXB
     * <code>Marshaller</code>, and allow for features such as indentation.
     *
     * @param properties the properties
     * @see javax.xml.bind.Marshaller#setProperty(String, Object)
     * @see javax.xml.bind.Marshaller#JAXB_ENCODING
     * @see javax.xml.bind.Marshaller#JAXB_FORMATTED_OUTPUT
     * @see javax.xml.bind.Marshaller#JAXB_NO_NAMESPACE_SCHEMA_LOCATION
     * @see javax.xml.bind.Marshaller#JAXB_SCHEMA_LOCATION
     */
    public void setMarshallerProperties(Map properties) {
        this.marshallerProperties = properties;
    }

    /**
     * Sets the JAXB <code>Unmarshaller</code> properties. These properties will be set on the underlying JAXB
     * <code>Unmarshaller</code>.
     *
     * @param properties the properties
     * @see javax.xml.bind.Unmarshaller#setProperty(String, Object)
     */
    public void setUnmarshallerProperties(Map properties) {
        this.unmarshallerProperties = properties;
    }

    public final void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasLength(contextPath)) {
            throw new IllegalArgumentException("contextPath is required");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Using context path [" + contextPath + "]");
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
            marshaller = jaxbContext.createMarshaller();
            unmarshaller = jaxbContext.createUnmarshaller();
            setJaxbProperties();
            initJaxbMarshaller();
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    /**
     * Convert the given <code>JAXBException</code> to an appropriate exception from the
     * <code>org.springframework.oxm</code> hierarchy.
     * <p/>
     * The default implementation delegates to <code>JaxbUtils</code>. Can be overridden in subclasses.
     *
     * @param ex <code>JAXBException</code> that occured
     * @return the corresponding <code>XmlMappingException</code> instance
     * @see JaxbUtils#convertJaxbException
     */
    protected XmlMappingException convertJaxbException(JAXBException ex) {
        return JaxbUtils.convertJaxbException(ex);
    }

    /**
     * Overridden by concrete JAXB marshallers for custom initialization behavior. Gets called after creation of JAXB
     * <code>Marshaller</code> and <code>Unmarshaller</code>, and after the respective properties have been set.
     */
    protected abstract void initJaxbMarshaller() throws Exception;

    public void marshal(Object graph, Result result) {
        try {
            marshaller.marshal(graph, result);
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    private void setJaxbProperties() throws PropertyException {
        if (marshallerProperties != null) {
            for (Iterator iterator = marshallerProperties.keySet().iterator(); iterator.hasNext();) {
                String name = (String) iterator.next();
                marshaller.setProperty(name, marshallerProperties.get(name));
            }
        }
        if (unmarshallerProperties != null) {
            for (Iterator iterator = unmarshallerProperties.keySet().iterator(); iterator.hasNext();) {
                String name = (String) iterator.next();
                unmarshaller.setProperty(name, unmarshallerProperties.get(name));
            }
        }
    }

    public Object unmarshal(Source source) {
        try {
            return this.unmarshaller.unmarshal(source);
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

}
