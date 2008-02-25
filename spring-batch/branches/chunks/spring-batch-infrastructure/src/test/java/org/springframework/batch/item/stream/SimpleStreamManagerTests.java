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
package org.springframework.batch.item.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.exception.StreamException;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 * 
 */
public class SimpleStreamManagerTests extends TestCase {

	private SimpleStreamManager manager = new SimpleStreamManager(new ResourcelessTransactionManager());

	private ItemStreamSupport stream = new StubItemStream();

	private List list = new ArrayList();

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#SimpleStreamManager(org.springframework.transaction.PlatformTransactionManager)}.
	 */
	public void testSimpleStreamManagerPlatformTransactionManager() {
		manager = new SimpleStreamManager();
		try {
			manager.getTransaction("foo");
			fail("Expected NullPointerException");
		}
		catch (NullPointerException e) {
			// expected;
		}
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#setTransactionManager(org.springframework.transaction.PlatformTransactionManager)}.
	 */
	public void testSetTransactionManager() {
		manager.setTransactionManager(new ResourcelessTransactionManager() {
			protected Object doGetTransaction() throws TransactionException {
				list.add("bar");
				return super.doGetTransaction();
			}
		});
		manager.getTransaction("foo");
		assertEquals("bar", list.get(0));
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#getExecutionContext(java.lang.Object)}.
	 */
	public void testGetStreamContextEmpty() {
		ExecutionContext streamContext = manager.getExecutionContext("foo");
		assertEquals(0, streamContext.entrySet().size());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#getExecutionContext(java.lang.Object)}.
	 */
	public void testGetStreamContextNotEmpty() {
		manager.register("foo", stream);
		ExecutionContext streamContext = manager.getExecutionContext("foo");
		assertEquals(1, streamContext.entrySet().size());
		assertEquals("bar", streamContext.getString(ClassUtils.getQualifiedName(stream.getClass()) + ".foo"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#getExecutionContext(java.lang.Object)}.
	 */
	public void testGetStreamContextNotEmptyAndRestore() {
		testGetStreamContextNotEmpty();
		ExecutionContext context = manager.getExecutionContext("foo");
		// Register again, now with the context that was created from the same
		// stream...
		manager.restoreFrom("foo", context);
		assertEquals(1, list.size());
		// The list should have the foo= map value from the sub-context
		assertEquals("bar", list.get(0));
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#getExecutionContext(java.lang.Object)}.
	 */
	public void testGetStreamContextNotEmptyAndRestoreWithNoPrefix() {
		ExecutionContext context = new ExecutionContext(PropertiesConverter.stringToProperties("foo=bar"));
		manager.setUseClassNameAsPrefix(false);
		manager.register("foo", stream);
		manager.restoreFrom("foo", context);
		assertEquals(1, list.size());
		// The list should have the foo= map value from the sub-context
		assertEquals("bar", list.get(0));
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#getExecutionContext(java.lang.Object)}.
	 */
	public void testGetStreamContextWithNoPrefix() {
		manager.setUseClassNameAsPrefix(false);
		manager.register("foo", stream);
		ExecutionContext context = manager.getExecutionContext("foo");
		assertEquals(1, context.entrySet().size());
		// The list should have the foo= map value from the sub-context
		assertEquals("bar", context.getString("foo"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#getExecutionContext(java.lang.Object)}.
	 */
	public void testGetStreamContextTwoRegistrations() {
		manager.register("foo", new ItemStreamSupport() {
			public ExecutionContext getExecutionContext() {
				return new ExecutionContext(PropertiesConverter.stringToProperties("foo=bar"));
			}
		});
		manager.register("foo", new ItemStreamSupport() {
			public ExecutionContext getExecutionContext() {
				return new ExecutionContext(PropertiesConverter.stringToProperties("foo=spam"));
			}
		});
		ExecutionContext streamContext = manager.getExecutionContext("foo");
		assertEquals(2, streamContext.entrySet().size());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#close(java.lang.Object)}.
	 */
	public void testClose() {
		manager.register("foo", new ItemStreamSupport() {
			public void close() throws StreamException {
				list.add("bar");
				super.close();
			}
		});
		manager.close("foo");
		assertEquals(1, list.size());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#commit(org.springframework.transaction.TransactionStatus)}.
	 */
	public void testCommitWithoutMark() {
		manager.register("foo", new ItemStreamSupport() {
			public void mark() {
				list.add("bar");
			}
		});
		TransactionStatus status = manager.getTransaction("foo");
		manager.commit(status);
		assertEquals(0, list.size());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.stream.SimpleStreamManager#rollback(org.springframework.transaction.TransactionStatus)}.
	 */
	public void testRollbackWithoutMark() {
		manager.register("foo", new ItemStreamSupport() {
			public void reset() {
				list.add("bar");
			}
		});
		TransactionStatus status = manager.getTransaction("foo");
		manager.rollback(status);
		assertEquals(0, list.size());
	}

	/**
	 * Make sure the values from registered stream are present in 
	 * manager's execution context.
	 */
	public void testGetExecutionContextPreservesValues() {
		stream = new ItemStreamSupport() {
			public ExecutionContext getExecutionContext() {
				ExecutionContext ctx = new ExecutionContext();
				ctx.putString("string", "testString");
				ctx.putDouble("double", 5.5);
				ctx.putLong("long", 7);
				return ctx;
			}
		};
		manager.register("foo", stream);
		ExecutionContext streamContext = stream.getExecutionContext();
		ExecutionContext managerContext = manager.getExecutionContext("foo");
		for (Iterator it = streamContext.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			assertTrue(managerContext.containsValue(entry.getValue()));
		}
		
	}
	private final class StubItemStream extends ItemStreamSupport {
		public ExecutionContext getExecutionContext() {
			return new ExecutionContext(PropertiesConverter.stringToProperties("foo=bar"));
		}

		public void restoreFrom(ExecutionContext context) {
			list.add(context.getString("foo"));
		}
	}

}
