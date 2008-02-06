/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.config.java.testing;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Costin Leau
 * 
 */
public class Company {

	private String name;

	private Collection<Owner> owners;

	private Collection<Worker> workers;

	public Company() {
		owners = new ArrayList<Owner>();
		workers = new ArrayList<Worker>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Worker> getWorkers() {
		return workers;
	}

	public Collection<Owner> getOwners() {
		return owners;
	}

	public void addOwner(Owner owner) {
		owners.add(owner);
	}

	public void removeOwner(Owner owner) {
		owners.remove(owner);
	}

	public void hire(Worker employee) {
		employee.setCompany(this);
		workers.add(employee);
	}

	public void fire(Worker employee) {
		employee.setCompany(null);
		workers.remove(employee);
	}
}
