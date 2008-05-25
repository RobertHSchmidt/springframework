package org.springframework.config.java.internal.model;


interface ValidatableMethod {

	public ValidationErrors validate(ValidationErrors errors);

}
