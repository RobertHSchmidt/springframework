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

package org.springframework.xml.validation;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.xml.JaxpVersion;

/**
 * Factory for <code>XmlValidator</code>s, being aware of JAXP 1.3 <code>XmlValidator</code>s, and JAXP 1.0 parsing
 * capababilities. Mainly for internal use within the framework.
 * <p/>
 * The goal of this class is to avoid runtime dependencies on JAXP 1.3 by using the best validation implementation that
 * is available. Prefers JAXP 1.3 <code>XmlValidator</code> implementations to a custom, SAX-based implementation.
 *
 * @author Arjen Poutsma
 * @see XmlValidator
 */
public abstract class XmlValidatorFactory {

    private static final Log logger = LogFactory.getLog(XmlValidatorFactory.class);

    /** Constant that defines a W3C XML Schema. */
    public static final String SCHEMA_W3C_XML = "http://www.w3.org/2001/XMLSchema";

    /** Constant that defines a RELAX NG Schema. */
    public static final String SCHEMA_RELAX_NG = "http://relaxng.org/ns/structure/1.0";

    /**
     * Create a <code>XmlValidator</code> with the given schema resource and schema language type. The schema language
     * must be one of the <code>SCHEMA_XXX</code> constants.
     *
     * @param schemaResource a resource that locates the schema to validate against
     * @param schemaLanguage the language of the schema
     * @return a validator
     * @throws IOException              if the schema resource cannot be read
     * @throws IllegalArgumentException if the schema language is not supported
     * @throws IllegalStateException    if JAXP 1.0 cannot be located
     * @throws XmlValidationException   if a <code>XmlValidator</code> cannot be created
     * @see #SCHEMA_RELAX_NG
     * @see #SCHEMA_W3C_XML
     */
    public static XmlValidator createValidator(Resource schemaResource, String schemaLanguage) throws IOException {
        return createValidator(new Resource[]{schemaResource}, schemaLanguage);
    }

    /**
     * Create a <code>XmlValidator</code> with the given schema resources and schema language type. The schema language
     * must be one of the <code>SCHEMA_XXX</code> constants.
     *
     * @param schemaResources an array of resource that locate the schemas to validate against
     * @param schemaLanguage  the language of the schemas
     * @return a validator
     * @throws IOException              if the schema resource cannot be read
     * @throws IllegalArgumentException if the schema language is not supported
     * @throws IllegalStateException    if JAXP 1.0 cannot be located
     * @throws XmlValidationException   if a <code>XmlValidator</code> cannot be created
     * @see #SCHEMA_RELAX_NG
     * @see #SCHEMA_W3C_XML
     */
    public static XmlValidator createValidator(Resource[] schemaResources, String schemaLanguage) throws IOException {
        Assert.notEmpty(schemaResources, "No resources given");
        Assert.hasLength(schemaLanguage, "No schema language provided");
        Assert.isTrue(SCHEMA_W3C_XML.equals(schemaLanguage) || SCHEMA_RELAX_NG.equals(schemaLanguage),
                "Invalid schema language: " + schemaLanguage);
        for (int i = 0; i < schemaResources.length; i++) {
            Assert.isTrue(schemaResources[i].exists(), "schema [" + schemaResources[i] + "] does not exist");
        }
        if (JaxpVersion.getJaxpVersion() >= JaxpVersion.JAXP_13) {
            logger.trace("Creating JAXP 1.3 XmlValidator");
            return Jaxp13ValidatorFactory.createValidator(schemaResources, schemaLanguage);
        }
        else if (JaxpVersion.getJaxpVersion() >= JaxpVersion.JAXP_10) {
            logger.trace("Creating JAXP 1.0 XmlValidator");
            return Jaxp10ValidatorFactory.createValidator(schemaResources, schemaLanguage);
        }
        else {
            throw new IllegalStateException("Could not locate JAXP 1.0 or higher.");
        }
    }

}
