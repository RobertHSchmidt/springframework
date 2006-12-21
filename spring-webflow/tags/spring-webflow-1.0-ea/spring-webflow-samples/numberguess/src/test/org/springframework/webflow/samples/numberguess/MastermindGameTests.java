package org.springframework.webflow.samples.numberguess;

import junit.framework.TestCase;

import org.springframework.webflow.Event;
import org.springframework.webflow.samples.numberguess.MastermindGame.GameData;
import org.springframework.webflow.test.MockRequestContext;

public class MastermindGameTests extends TestCase {
	public void testGuessNoInputProvided() throws Exception {
		MockRequestContext context = new MockRequestContext();
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessInputInvalidLength() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.putRequestParameter("guess", "123");
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessInputNotAllDigits() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.putRequestParameter("guess", "12AB");
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessInputNotUniqueDigits() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.putRequestParameter("guess", "1111");
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessRetry() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.putRequestParameter("guess", "1234");
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("retry", result.getId());
	}

	public void testGuessCorrect() throws Exception {
		MockRequestContext context = new MockRequestContext();
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		GameData data = action.getData();
		String answer = data.getAnswer();
		context.putRequestParameter("guess", answer);
		result = action.guess(context);
		assertEquals("correct", result.getId());
	}
}