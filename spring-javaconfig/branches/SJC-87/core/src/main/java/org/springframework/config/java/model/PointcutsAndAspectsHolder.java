package org.springframework.config.java.model;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;

// TODO: make thread local
public class PointcutsAndAspectsHolder {
	public static Map<String, Pointcut> pointcuts = new HashMap<String, Pointcut>();
	public static Map<String, Advice> advice = new HashMap<String, Advice>();
}
