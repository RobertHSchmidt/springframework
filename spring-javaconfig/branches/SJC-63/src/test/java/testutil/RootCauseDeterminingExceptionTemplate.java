package testutil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hamcrest.CoreMatchers;

/**
 * Useful when root cause exceptions may be wrapped in reflection exceptions ({@link InvocationTargetException},
 * for example). JUnit's Test(expected = ...) is not smart enough to handle
 * these cases.
 * 
 * @author Chris Beams
 */
public class RootCauseDeterminingExceptionTemplate {
	private final Runnable block;

	private final Class<? extends Throwable> expected;

	public RootCauseDeterminingExceptionTemplate(Runnable block, Class<? extends Throwable> expected) {
		this.expected = expected;
		this.block = block;
	}

	public void execute() {
		Throwable thrown = null;
		try {
			block.run();
		}
		catch (Throwable t) {
			thrown = t;
		}
		assertNotNull("no exception was thrown when expecting exception of type " + expected, thrown);
		assertThat("an unexpected exception was thrown", ExceptionUtils.getRootCause(thrown), CoreMatchers
				.instanceOf(expected));
	}
}
