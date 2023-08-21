package org.haic.often.function;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts three input arguments and returns no
 * result.  This is the two-arity specialization of {@link Consumer}.
 * Unlike most other functional interfaces, {@code ThreeBiConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <A> the type of the first argument to the operation
 * @param <B> the type of the second argument to the operation
 * @param <C> the type of the third argument to the operation
 * @author haicdust
 * @version 1.0
 * @since 2023/5/6 18:28
 */
@FunctionalInterface
public interface ThreeBiConsumer<A, B, C> {

	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param a the first input argument
	 * @param b the second input argument
	 * @param c the third input argument
	 */
	void accept(A a, B b, C c);

	/**
	 * Returns a composed {@code ThreeBiConsumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation.  If performing this operation throws an exception,
	 * the {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code ThreeBiConsumer} that performs in sequence this
	 * 		operation followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	default ThreeBiConsumer<A, B, C> andThen(ThreeBiConsumer<? super A, ? super B, ? super C> after) {
		Objects.requireNonNull(after);

		return (a, b, c) -> {
			accept(a, b, c);
			after.accept(a, b, c);
		};
	}

}
