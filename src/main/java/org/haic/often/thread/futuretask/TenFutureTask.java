package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class ten parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record TenFutureTask<A, B, C, D, E, F, G, H, I, J, V>(A A, B B, C C, D D, E E, F F, G G, H H, I I, J J, Callable<A, B, C, D, E, F, G, H, I, J, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A, B, C, D, E, F, G, H, I, J);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, D, E, F, G, H, I, J, V> {
		V call(A A, B B, C C, D D, E E, F F, G G, H H, I I, J J) throws Exception;
	}

}