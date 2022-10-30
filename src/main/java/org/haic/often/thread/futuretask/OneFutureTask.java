package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class one parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public record OneFutureTask<A, V>(A A, Callable<A, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, V> {
		V call(A A) throws Exception;
	}
}