package org.springframework.beans.factory.java;

public class Person {
	
	private long id;
	
	private int age;
	
	private String name;
	
	public long getId() {
		return id;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

}
