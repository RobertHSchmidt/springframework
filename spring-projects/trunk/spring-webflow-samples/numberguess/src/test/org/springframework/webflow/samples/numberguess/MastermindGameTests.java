package org.springframework.webflow.samples.numberguess;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.Event;
import org.springframework.webflow.samples.numberguess.MastermindGame.GameData;
import org.springframework.webflow.test.MockRequestContext;

public class MastermindGameTests extends TestCase {
	public void testGuessNoInputProvided() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.setSourceEvent(new Event(this, "submit"));
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}
	
	public void testGuessInputInvalidLength() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "123");
		context.setSourceEvent(new Event(this, "submit", parameters));
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessInputNotAllDigits() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "12AB");
		context.setSourceEvent(new Event(this, "submit", parameters));
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessInputNotUniqueDigits() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "1111");
		context.setSourceEvent(new Event(this, "submit", parameters));
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessRetry() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "1234");
		context.setSourceEvent(new Event(this, "submit", parameters));
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		assertEquals("retry", result.getId());
	}
	
	public void testGuessCorrect() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		context.setSourceEvent(new Event(this, "submit", parameters));
		MastermindGame action = new MastermindGame();
		Event result = action.guess(context);
		GameData data = action.getData();
		String answer = data.getAnswer();
		parameters.put("guess", answer);
		context.setSourceEvent(new Event(this, "submit", parameters));
		result = action.guess(context);
		assertEquals("correct", result.getId());
	}
}