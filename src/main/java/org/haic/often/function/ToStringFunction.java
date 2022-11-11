package org.haic.often.function;

import java.util.function.Function;

/**
 * Represents a function that produces an String-valued result.  This is the
 * {@code String}-producing primitive specialization for {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @author haicdust
 * @version 1.0
 * @see Function
 * @since 2022/11/11 21:58
 */
@FunctionalInterface
public interface ToStringFunction<T> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 * @return the function result
	 */
	String apply(T value);
}
