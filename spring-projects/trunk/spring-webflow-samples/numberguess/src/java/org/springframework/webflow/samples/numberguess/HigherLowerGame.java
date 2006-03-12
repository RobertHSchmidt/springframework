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

import org.springframework.core.enums.StaticLabeledEnum;

/**
 * Action that encapsulates logic for the number guess sample flow. Note that
 * this is a stateful action: it holds modifiable state in instance members!
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class HigherLowerGame implements Serializable {

	private static final Random random = new Random();

	private Calendar start = Calendar.getInstance();

	private int answer = random.nextInt(101);

	private int guesses = 0;

	public int getAnswer() {
		return answer;
	}

	public long getGameDuration() {
		Calendar now = Calendar.getInstance();
		long durationMilliseconds = now.getTime().getTime() - start.getTime().getTime();
		return durationMilliseconds / 1000;
	}

	public int getGuesses() {
		return guesses;
	}

	public GuessResult makeGuess(int guess) {
		if (guess < 0 || guess > 100) {
			return GuessResult.INVALID;
		}
		else {
			guesses++;
			if (answer < guess) {
				return GuessResult.TOO_LOW;
			}
			else if (answer > guess) {
				return GuessResult.TOO_HIGH;
			}
			else {
				return GuessResult.CORRECT;
			}
		}
	}

	public static class GuessResult extends StaticLabeledEnum {
		public static final GuessResult INVALID = new GuessResult(0, "Invalid");

		public static final GuessResult TOO_LOW = new GuessResult(1, "Too low");

		public static final GuessResult TOO_HIGH = new GuessResult(2, "Too high");

		public static final GuessResult CORRECT = new GuessResult(3, "Correct");

		private GuessResult(int code, String label) {
			super(code, label);
		}
	}
}