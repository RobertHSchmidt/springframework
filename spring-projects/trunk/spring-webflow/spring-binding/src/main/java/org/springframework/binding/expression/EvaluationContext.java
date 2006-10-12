package org.springframework.binding.expression;

import java.util.Map;

/**
 * A context object with two main responsibities:
 * <ol>
 * <ul>Exposing information to an expression to influence 
 * an evaluation attempt.
 * <ul>Providing operations for recording progress or 
 * errors during the expression evaluation process.
 * </ol>
 * @author Keith Donald
 */
public interface EvaluationContext {

	/**
	 * Returns a map of attributes that can be used to influence expression evaluation.
	 * @return the evaluation attributes
	 */
	public Map getAttributes();

}