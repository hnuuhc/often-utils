package org.haic.often.thread;

import org.haic.often.thread.futuretask.*;

import java.util.concurrent.Callable;

/**
 * FutureTaskThread defines a thread with a generic parameter
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/12 13:49
 */
public class FutureTaskThread<A, B, C, D, E, F, G, H, I, J, V> implements Callable<V> {

	private final Callable<V> callable;

	public FutureTaskThread(Callable<V> callable) {
		this.callable = callable;
	}

	public FutureTaskThread(A A, OneFutureTask.Callable<A, V> callable) {
		this.callable = new OneFutureTask<>(A, callable);
	}

	public FutureTaskThread(A A, B B, TwoFutureTask.Callable<A, B, V> callable) {
		this.callable = new TwoFutureTask<>(A, B, callable);
	}

	public FutureTaskThread(A A, B B, C C, ThreeFutureTask.Callable<A, B, C, V> callable) {
		this.callable = new ThreeFutureTask<>(A, B, C, callable);
	}

	public FutureTaskThread(A A, B B, C C, D D, FourFutureTask.Callable<A, B, C, D, V> callable) {
		this.callable = new FourFutureTask<>(A, B, C, D, callable);
	}

	public FutureTaskThread(A A, B B, C C, D D, E E, FiveFutureTask.Callable<A, B, C, D, E, V> callable) {
		this.callable = new FiveFutureTask<>(A, B, C, D, E, callable);
	}

	public FutureTaskThread(A A, B B, C C, D D, E E, F F, SixFutureTask.Callable<A, B, C, D, E, F, V> callable) {
		this.callable = new SixFutureTask<>(A, B, C, D, E, F, callable);
	}

	public FutureTaskThread(A A, B B, C C, D D, E E, F F, G G, SevenFutureTask.Callable<A, B, C, D, E, F, G, V> callable) {
		this.callable = new SevenFutureTask<>(A, B, C, D, E, F, G, callable);
	}

	public FutureTaskThread(A A, B B, C C, D D, E E, F F, G G, H H, EightFutureTask.Callable<A, B, C, D, E, F, G, H, V> callable) {
		this.callable = new EightFutureTask<>(A, B, C, D, E, F, G, H, callable);
	}

	public FutureTaskThread(A A, B B, C C, D D, E E, F F, G G, H H, I I, NineFutureTask.Callable<A, B, C, D, E, F, G, H, I, V> callable) {
		this.callable = new NineFutureTask<>(A, B, C, D, E, F, G, H, I, callable);
	}

	public FutureTaskThread(A A, B B, C C, D D, E E, F F, G G, H H, I I, J J, TenFutureTask.Callable<A, B, C, D, E, F, G, H, I, J, V> callable) {
		this.callable = new TenFutureTask<>(A, B, C, D, E, F, G, H, I, J, callable);
	}

	@Override
	public V call() throws Exception {
		return callable.call();
	}

}