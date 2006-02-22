/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.samples.numberguess;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.MultiAction;

/**
 * Action that encapsulates logic for the number guess sample flow. Note that
 * this is a stateful action: it holds modifiable state in instance memebers!
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class HigherLowerGame extends MultiAction implements Serializable {

	private static final String GUESS_PARAMETER = "guess";

	private static final Random random = new Random();

	private Calendar start = Calendar.getInstance();

	private int answer = random.nextInt(101);

	private int guesses = 0;

	private String lastGuessResult = "";

	private long durationSeconds = -1;

	public int getAnswer() {
		return answer;
	}

	public long getDurationSeconds() {
		return durationSeconds;
	}

	public int getGuesses() {
		return guesses;
	}

	public String getLastGuessResult() {
		return lastGuessResult;
	}

	public Event guess(RequestContext context) throws Exception {
		int guess = context.getRequestParameters().getInteger(GUESS_PARAMETER, new Integer(-1)).intValue();
		if (guess < 0 || guess > 100) {
			lastGuessResult = "invalid";
			return result("invalidInput");
		}
		else {
			guesses++;
			if (answer < guess) {
				lastGuessResult = "too high!";
				return result("retry");
			}
			else if (answer > guess) {
				lastGuessResult = "too low!";
				return result("retry");
			}
			else {
				lastGuessResult = "correct!";
				Calendar now = Calendar.getInstance();
				long durationMilliseconds = now.getTime().getTime() - start.getTime().getTime();
				durationSeconds = durationMilliseconds / 1000;
				return result("correct");
			}
		}
	}
}