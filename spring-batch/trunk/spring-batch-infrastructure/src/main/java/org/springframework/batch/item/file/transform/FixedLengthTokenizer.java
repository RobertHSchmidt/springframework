/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.batch.item.file.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizer used to process data obtained from files with fixed-length format.
 * Columns are specified by array of Range objects ({@link #setColumns(Range[])}).
 * 
 * @author tomas.slanina
 * @author peter.zozom
 * @author Dave Syer
 * @author Lucas Ward
 */
public class FixedLengthTokenizer extends AbstractLineTokenizer {

	private Range[] ranges;
	private int maxRange;

	/**
	 * Set the column ranges. Used in conjunction with the
	 * {@link RangeArrayPropertyEditor} this property can be set in the form of
	 * a String describing the range boundaries, e.g. "1,4,7" or "1-3,4-6,7" or
	 * "1-2,4-5,7-10".
	 * 
	 * @param ranges the column ranges expected in the input
	 */
	public void setColumns(Range[] ranges) {
		this.ranges = ranges;
		
		calculateMaxRange(ranges);
	}
	
	private void calculateMaxRange(Range[] ranges){
		if(ranges == null || ranges.length == 0){
			maxRange = 0;
			return;
		}
		
		maxRange = ranges[0].getMax();
		
		for(int i = 0; i < ranges.length; i++){
			if(ranges[i].getMax() > maxRange){
				maxRange = ranges[i].getMax();
			}
		}
	}

	/**
	 * Yields the tokens resulting from the splitting of the supplied
	 * <code>line</code>.
	 * 
	 * @param line
	 *            the line to be tokenised (can be <code>null</code>)
	 * 
	 * @return the resulting tokens (empty if the line is null)
	 * @throws IncorrectLineLengthException if line length is greater than
	 * or less than the max range set.
	 */
	protected List doTokenize(String line) {
		List tokens = new ArrayList(ranges.length);
		int lineLength;
		String token;

		lineLength = line.length();
		
		if(lineLength > maxRange || lineLength < maxRange){
			//line is longer than max range, throw exception
			throw new IncorrectLineLengthException(maxRange, lineLength);
		}

		for (int i = 0; i < ranges.length; i++) {

			int startPos = ranges[i].getMin() - 1;
			int endPos = ranges[i].getMax();

			if (lineLength >= endPos) {
				token = line.substring(startPos, endPos);
			} else if (lineLength >= startPos) {
				token = line.substring(startPos);
			} else {
				token = "";
			}

			tokens.add(token);
		}

		return tokens;
	}
}
