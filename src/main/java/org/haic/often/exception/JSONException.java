package org.haic.often.exception;

import org.haic.often.parser.ParserStringBuilder;

/**
 * JSON解析时异常
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/11 23:08
 */
public class JSONException extends RuntimeException {

	/**
	 * Constructs a new runtime exception with {@code null} as its
	 * detail message.  The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link #initCause}.
	 */
	public JSONException() {
		super();
	}

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for
	 *                later retrieval by the {@link #getMessage()} method.
	 */
	public JSONException(String message) {
		super(message);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 *
	 * @param body the detail message. The detail message is saved for
	 *             later retrieval by the {@link #getMessage()} method.
	 */
	public JSONException(ParserStringBuilder body) {
		super("位置 " + body.site() + " 处期望值错误: " + new StringBuilder(body.substring(body.site() < 10 ? 0 : body.site() - 10, Math.min(body.site() + 10, body.length()))).insert(10, "[错误]").toString().replaceAll("\n", ""));
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this runtime exception's detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval
	 *                by the {@link #getMessage()} method).
	 * @param cause   the cause (which is saved for later retrieval by the
	 *                {@link #getCause()} method).  (A {@code null} value is
	 *                permitted, and indicates that the cause is nonexistent or
	 *                unknown.)
	 * @since 1.4
	 */
	public JSONException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new runtime exception with the specified cause and a
	 * detail message of {@code (cause==null ? null : cause.toString())}
	 * (which typically contains the class and detail message of
	 * {@code cause}).  This constructor is useful for runtime exceptions
	 * that are little more than wrappers for other throwables.
	 *
	 * @param cause the cause (which is saved for later retrieval by the
	 *              {@link #getCause()} method).  (A {@code null} value is
	 *              permitted, and indicates that the cause is nonexistent or
	 *              unknown.)
	 * @since 1.4
	 */
	public JSONException(Throwable cause) {
		super(cause);
	}

}
