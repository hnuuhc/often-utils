package org.haic.often.function;

import java.util.function.Function;

/**
 * Represents a function that accepts an String-valued argument and produces a
 * result.  This is the {@code String}-consuming primitive specialization for
 * {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(String)}.
 *
 * @param <T> the type of the input to the function
 * @author haicdust
 * @version 1.0
 * @see Function
 * @since 2022/11/11 21:58
 */
@FunctionalInterface
public interface StringFunction<T> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 * @return the function result
	 */
	T apply(String value);

}
