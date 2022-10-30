package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class four parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record FourFutureTask<A, B, C, D, V>(A A, B B, C C, D D, Callable<A, B, C, D, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A, B, C, D);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, D, V> {
		V call(A A, B B, C C, D D) throws Exception;
	}

}