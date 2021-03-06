package org.springframework.binding.expression;

/**
 * Input provided to an expression parser that can influence an expression parsing/compilation routine.
 * @author Keith Donald
 */
public interface ParserContext {

	/**
	 * Returns the type of context object the parsed expression will evaluate in. An expression parser may use this
	 * value to install custom variable resolves for that particular type of context.
	 * @return the evaluation context type
	 */
	public Class getEvaluationContextType();

	/**
	 * Returns the expected type of object returned from evaluating the parsed expression. An expression parser may use
	 * this value to coerce an raw evaluation result before it is returned.
	 * @return the expected evaluation result type
	 */
	public Class getExpectedEvaluationResultType();

	/**
	 * Returns additional expression variables or aliases that can be referenced during expression evaluation. An
	 * expression parser will register these variables for reference during evaluation.
	 */
	public ExpressionVariable[] getExpressionVariables();

	/**
	 * Whether or not the expression being parsed is a template. A template expression consists of literal text that can
	 * be mixed with evaluatable blocks. Some examples:
	 * 
	 * <pre>
	 * 	   Some literal text
	 *     Hello #{name.firstName}!
	 *     #{3 + 4}
	 * </pre>
	 * 
	 * @return true if the expression is a template, false otherwise
	 */
	public boolean isTemplate();

}