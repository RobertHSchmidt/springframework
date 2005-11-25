/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.webflow.Flow;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Flow builder that builds a flow definition from an XML file. XML files read by
 * this class should use the following doctype:
 * 
 * <pre>
 *     &lt;!DOCTYPE flow PUBLIC &quot;-//SPRING//DTD WEBFLOW 1.0//EN&quot;
 *     &quot;http://www.springframework.org/dtd/spring-webflow-1.0.dtd&quot;&gt;
 * </pre>
 * 
 * Consult the <a href="http://www.springframework.org/dtd/spring-webflow-1.0.dtd">web flow DTD</a>
 * for more information on the XML flow definition format.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name </b></td>
 * <td><b>default </b></td>
 * <td><b>description </b></td>
 * </tr>
 * <tr>
 * <td>location</td>
 * <td><i>null</i></td>
 * <td>Specifies the resource location from which the XML-based flow definition
 * is loaded. This "input stream source" is a required property.</td>
 * </tr>
 * <tr>
 * <td>validating</td>
 * <td><i>true</i></td>
 * <td>Set if the XML parser should validate the document and thus enforce a
 * DTD.</td>
 * </tr>
 * <tr>
 * <td>entityResolver</td>
 * <td><i>{@link WebFlowDtdResolver}</i></td>
 * <td>Set a SAX entity resolver to be used for parsing.</td>
 * </tr>
 * </table>
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class XmlFlowBuilder extends DomFlowBuilder implements ResourceHolder {

	/**
	 * Flag indicating if the the XML document parser will perform DTD
	 * validation.
	 */
	private boolean validating = true;

	/**
	 * The spring-webflow DTD resolution strategy.
	 */
	private EntityResolver entityResolver = new WebFlowDtdResolver();
			
	/**
	 * Default constructor for bean style usage.
	 * @see #setLocation(Resource)
	 */
	public XmlFlowBuilder() {
	}

	/**
	 * Creates a new XML flow builder that builds the flow contained within the
	 * provided XML document.
	 * @param location the location of the XML document resource
	 */
	public XmlFlowBuilder(Resource location) {
		setLocation(location);
	}

	/**
	 * Creates a new XML flow builder that builds the flow contained within the
	 * provided XML document.
	 * @param location the location of the XML document resource
	 * @param beanFactory the bean factory defining this flow builder
	 */
	public XmlFlowBuilder(Resource location, BeanFactory beanFactory) {
		super(beanFactory);
		setLocation(location);
	}

	/**
	 * Returns the XML resource from which the flow definition is read.
	 */
	public Resource getLocation() {
		return getDocumentResource();
	}

	/**
	 * Set the resource from which the XML flow definition will be read.
	 */
	public void setLocation(Resource location) {
		super.setDocumentResource(location);
	}

	public Resource getResource() {
		return getLocation();
	}

	/**
	 * Returns whether or not the XML parser will validate the document.
	 */
	public boolean isValidating() {
		return validating;
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a
	 * DTD. Defaults to true.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Returns the SAX entity resolver used by the XML parser.
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default,
	 * WebFlowDtdResolver will be used. Can be overridden for custom entity
	 * resolution, for example relative to some specific base path.
	 * 
	 * @see org.springframework.webflow.config.WebFlowDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}
	
	public Flow init(String flowId, Map flowProperties) throws FlowBuilderException {
		Assert.notNull(getDocumentResource(), "The location property specifying the XML flow definition resource location is required");
		try {
			setDocumentElement(loadDocument().getDocumentElement());
		}
		catch (IOException e) {
			throw new FlowBuilderException(this, "Cannot load the XML flow definition resource '" + getDocumentResource() + "'", e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException(this, "Cannot configure the parser to parse the XML flow definition", e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException(this, "Cannot parse the flow definition XML document at'" + getDocumentResource() + "'", e);
		}
		
		return super.init(flowId, flowProperties);
	}

	/**
	 * Load the flow definition from the configured resource and return the resulting
	 * DOM document.
	 */
	protected Document loadDocument() throws IOException, ParserConfigurationException, SAXException {
		InputStream is = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(isValidating());
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new SimpleSaxErrorHandler(logger));
			docBuilder.setEntityResolver(getEntityResolver());
			is = getDocumentResource().getInputStream();
			return docBuilder.parse(is);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ex) {
					logger.warn("Could not close InputStream", ex);
				}
			}
		}
	}

	public void dispose() {
		super.dispose();
	}

}