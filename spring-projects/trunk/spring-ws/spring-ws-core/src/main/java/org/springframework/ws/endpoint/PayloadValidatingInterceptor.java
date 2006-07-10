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

package org.springframework.ws.endpoint;

import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapEndpointInterceptor;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.ws.soap.support.SoapMessageUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Interceptor that validates the contents of <code>WebServiceMessage</code>s using a schema. Allows for both W3C XML
 * and RELAX NG schemas.
 * <p/>
 * When the payload is invalid, this interceptor stops processing of the interceptor chain. Additionally, if the message
 * is a SOAP request message, a SOAP Fault is created as reply. Invalid SOAP responses do not result in a fault.
 * <p/>
 * The schema to validate against is set with the <code>schema</code> property. By default, only the request message is
 * validated, but this behaviour can be changed using the <code>validateRequest</code> and <code>validateResponse</code>
 * properties. Responses that contains faults are not validated.
 *
 * @author Arjen Poutsma
 * @see #setSchema
 * @see #setValidateRequest(boolean)
 * @see #setValidateResponse(boolean)
 */
public class PayloadValidatingInterceptor extends TransformerObjectSupport
        implements SoapEndpointInterceptor, InitializingBean {

    /**
     * Default SOAP Fault string used when a validation errors occur on the request.
     *
     * @see #setValidationErrorFaultString(String)
     */
    public static final String DEFAULT_VALIDATION_ERROR_FAULT_STRING = "Validation error";

    /**
     * Default SOAP Fault Detail name used when a validation errors occur on the request.
     *
     * @see #setValidationErrorDetailElementName(javax.xml.namespace.QName)
     */
    public static final QName DEFAULT_VALIDATION_ERROR_DETAIL_ELEMENT_NAME =
            new QName("http://springframework.org/spring-ws", "ValidationError", "spring-ws");

    private static final Log logger = LogFactory.getLog(PayloadValidatingInterceptor.class);

    private String schemaLanguage = XmlValidatorFactory.SCHEMA_W3C_XML;

    private Resource[] schemaResources;

    private boolean validateRequest = true;

    private boolean validateResponse = false;

    private XmlValidator validator;

    private String validationErrorFaultString = DEFAULT_VALIDATION_ERROR_FAULT_STRING;

    private QName validationErrorDetailElementName = DEFAULT_VALIDATION_ERROR_DETAIL_ELEMENT_NAME;

    private boolean addValidationErrorDetail = true;

    /**
     * Indicates whether a SOAP Fault detail element should be created when a validation error occurs. This detail
     * element will contain the exact validation errors. Defaults to <code>true</code>.
     */
    public void setAddValidationErrorDetail(boolean addValidationErrorDetail) {
        this.addValidationErrorDetail = addValidationErrorDetail;
    }

    /**
     * Sets the schema resource to use for validation.
     */
    public void setSchema(Resource schemaResource) {
        this.schemaResources = new Resource[]{schemaResource};
    }

    /**
     * Sets the schema resources to use for validation.
     */
    public void setSchemas(Resource[] schemaResources) {
        this.schemaResources = schemaResources;
    }

    /**
     * Sets the schema language. Default is the W3C XML Schema: <code>http://www.w3.org/2001/XMLSchema"</code>.
     *
     * @see XmlValidatorFactory#SCHEMA_W3C_XML
     * @see XmlValidatorFactory#SCHEMA_RELAX_NG
     */
    public void setSchemaLanguage(String schemaLanguage) {
        this.schemaLanguage = schemaLanguage;
    }

    /**
     * Indicates whether the request should be validated against the schema. Default is <code>true</code>.
     */
    public void setValidateRequest(boolean validateRequest) {
        this.validateRequest = validateRequest;
    }

    /**
     * Indicates whether the response should be validated against the schema. Default is <code>false</code>.
     */
    public void setValidateResponse(boolean validateResponse) {
        this.validateResponse = validateResponse;
    }

    /**
     * Sets the fault detail element name when validation errors occur on the request. Defaults to
     * <code>DEFAULT_VALIDATION_ERROR_DETAIL_ELEMENT_NAME</code>.
     *
     * @see #DEFAULT_VALIDATION_ERROR_DETAIL_ELEMENT_NAME
     */
    public void setValidationErrorDetailElementName(QName validationErrorDetailElementName) {
        this.validationErrorDetailElementName = validationErrorDetailElementName;
    }

    /**
     * Sets the fault string used when validation errors occur on the request. Defaults to
     * <code>DEFAULT_VALIDATION_ERROR_FAULT_STRING</code>.
     *
     * @see #DEFAULT_VALIDATION_ERROR_FAULT_STRING
     */
    public void setValidationErrorFaultString(String validationErrorFaultString) {
        this.validationErrorFaultString = validationErrorFaultString;
    }

    /**
     * Validates the request message in the given message context. Validation only occurs if
     * <code>validateRequest</code> is set to <code>true</code>, which is the default.
     * <p/>
     * Returns <code>true</code> if the request is valid, or <code>false</code> if it isn't. Additionally, when the
     * <code>messageContext</code> is a <code>SoapMessageContext</code>, a SOAP Fault is added as response.
     *
     * @param messageContext the message context
     * @return <code>true</code> if the message is valid; <code>false</code> otherwise
     * @see #setValidateRequest(boolean)
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint)
            throws IOException, SAXException, TransformerException {
        if (validateRequest) {
            SAXParseException[] errors = validator.validate(messageContext.getRequest().getPayloadSource());
            if (!ObjectUtils.isEmpty(errors)) {
                for (int i = 0; i < errors.length; i++) {
                    logger.warn("XML validation error on request: " + errors[i].getMessage());
                }
                if (messageContext instanceof SoapMessageContext) {
                    createRequestValidationFault((SoapMessageContext) messageContext, errors);
                }
                return false;
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("Request message validated");
            }
        }
        return true;
    }

    /**
     * Validates the response message in the given message context. Validation only occurs if
     * <code>validateResponse</code> is set to <code>true</code>, which is <strong>not</strong> the default.
     * <p/>
     * Returns <code>true</code> if the request is valid, or <code>false</code> if it isn't.
     *
     * @param messageContext the message context.
     * @return <code>true</code> if the response is valid; <code>false</code> otherwise
     * @see #setValidateResponse(boolean)
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws IOException, SAXException {
        if (validateResponse) {
            SAXParseException[] errors = validator.validate(messageContext.getResponse().getPayloadSource());
            if (!ObjectUtils.isEmpty(errors)) {
                for (int i = 0; i < errors.length; i++) {
                    logger.error("XML validation error on response: " + errors[i].getMessage());
                }
                return false;
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("Response message validated");
            }
        }
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(schemaResources, "setting either the schema or schemas property is required");
        Assert.hasLength(schemaLanguage, "schemaLanguage is required");
        for (int i = 0; i < schemaResources.length; i++) {
            Assert.isTrue(schemaResources[i].exists(), "schema [" + schemaResources + "] does not exist");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Validating using " + StringUtils.arrayToCommaDelimitedString(schemaResources));
        }
        validator = XmlValidatorFactory.createValidator(schemaResources, schemaLanguage);
    }

    /**
     * Returns <code>true</code>, i.e. SOAP Faults are not validated.
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    /**
     * Returns <code>false</code>, i.e. all SOAP Headers are not understood.
     */
    public boolean understands(SoapHeaderElement header) {
        return false;
    }

    /**
     * Creates a response soap message containing a <code>SoapFault</code> that descibes the validation errors.
     */
    protected void createRequestValidationFault(SoapMessageContext context, SAXParseException[] errors)
            throws TransformerException {
        SoapMessage response = context.getSoapResponse();
        SoapFault fault = SoapMessageUtils.addSenderFault(response, validationErrorFaultString);
        if (addValidationErrorDetail) {
            SoapFaultDetail detail = fault.addFaultDetail();
            for (int i = 0; i < errors.length; i++) {
                SoapFaultDetailElement detailElement = detail.addFaultDetailElement(validationErrorDetailElementName);
                detailElement.addText(errors[0].getMessage());
            }
        }
    }
}
