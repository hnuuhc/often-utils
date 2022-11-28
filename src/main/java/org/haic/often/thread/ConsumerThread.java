package org.haic.often.thread;

import org.haic.often.thread.consumer.*;

/**
 * Represents an operation that accepts multiple input argument and returns no result
 *
 * <p>This is a functional interface</a>
 * whose functional method is {@link #run()}.
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public class ConsumerThread implements Runnable {

	private final Runnable runnable;

	public ConsumerThread(Runnable runnable) {
		this.runnable = runnable;
	}

	public <A> ConsumerThread(A A, OneConsumer.Runnable<A> runnable) {
		this.runnable = new OneConsumer<>(A, runnable);
	}

	public <A, B> ConsumerThread(A A, B B, TwoConsumer.Runnable<A, B> runnable) {
		this.runnable = new TwoConsumer<>(A, B, runnable);
	}

	public <A, B, C> ConsumerThread(A A, B B, C C, ThreeConsumer.Runnable<A, B, C> runnable) {
		this.runnable = new ThreeConsumer<>(A, B, C, runnable);
	}

	public <A, B, C, D> ConsumerThread(A A, B B, C C, D D, FourConsumer.Runnable<A, B, C, D> runnable) {
		this.runnable = new FourConsumer<>(A, B, C, D, runnable);
	}

	public <A, B, C, D, E> ConsumerThread(A A, B B, C C, D D, E E, FiveConsumer.Runnable<A, B, C, D, E> runnable) {
		this.runnable = new FiveConsumer<>(A, B, C, D, E, runnable);
	}

	public <A, B, C, D, E, F> ConsumerThread(A A, B B, C C, D D, E E, F F, SixConsumer.Runnable<A, B, C, D, E, F> runnable) {
		this.runnable = new SixConsumer<>(A, B, C, D, E, F, runnable);
	}

	public <A, B, C, D, E, F, G> ConsumerThread(A A, B B, C C, D D, E E, F F, G G, SevenConsumer.Runnable<A, B, C, D, E, F, G> runnable) {
		this.runnable = new SevenConsumer<>(A, B, C, D, E, F, G, runnable);
	}

	public <A, B, C, D, E, F, G, H> ConsumerThread(A A, B B, C C, D D, E E, F F, G G, H H, EightConsumer.Runnable<A, B, C, D, E, F, G, H> runnable) {
		this.runnable = new EightConsumer<>(A, B, C, D, E, F, G, H, runnable);
	}

	public <A, B, C, D, E, F, G, H, I> ConsumerThread(A A, B B, C C, D D, E E, F F, G G, H H, I I, NineConsumer.Runnable<A, B, C, D, E, F, G, H, I> runnable) {
		this.runnable = new NineConsumer<>(A, B, C, D, E, F, G, H, I, runnable);
	}

	public <A, B, C, D, E, F, G, H, I, J> ConsumerThread(A A, B B, C C, D D, E E, F F, G G, H H, I I, J J, TenConsumer.Runnable<A, B, C, D, E, F, G, H, I, J> runnable) {
		this.runnable = new TenConsumer<>(A, B, C, D, E, F, G, H, I, J, runnable);
	}

	@Override
	public void run() {
		runnable.run();
	}

}
