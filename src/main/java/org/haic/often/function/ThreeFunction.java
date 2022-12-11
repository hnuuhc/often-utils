package org.haic.often.function;

import org.haic.often.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object, Object)}.
 *
 * @param <A> the type of the first argument to the function
 * @param <B> the type of the second argument to the function
 * @param <C> the type of the third argument to the function
 * @param <R> the type of the result of the function
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface ThreeFunction<A, B, C, R> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param a the first function argument
	 * @param b the second function argument
	 * @param c the third function argument
	 * @return the function result
	 */
	R apply(A a, B b, C c);

	/**
	 * Returns a composed function that first applies this function to
	 * its input, and then applies the {@code after} function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V>   the type of output of the {@code after} function, and of the
	 *              composed function
	 * @param after the function to apply after this function is applied
	 * @return a composed function that first applies this function and then
	 * 		applies the {@code after} function
	 * @throws NullPointerException if after is null
	 */
	default <V> ThreeFunction<A, B, C, V> andThen(@NotNull Function<? super R, ? extends V> after) {
		return (A a, B b, C c) -> after.apply(apply(a, b, c));
	}
}
