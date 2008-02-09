package testutil;

import java.util.concurrent.Callable;

import org.apache.commons.lang.exception.ExceptionUtils;

public class RootCauseRethrowingExceptionTemplate {

	private final Callable<?> block;

	public RootCauseRethrowingExceptionTemplate(Callable<?> block) {
		this.block = block;
	}

	public void execute() throws Throwable {
		try {
			block.call();
		}
		catch (Throwable t) {
			throw (ExceptionUtils.getRootCause(t));
		}
	}
}
