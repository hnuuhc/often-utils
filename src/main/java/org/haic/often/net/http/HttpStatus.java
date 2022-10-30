package org.haic.often.net.http;

/**
 * Http状态码类,拓展了一些未收录的状态码
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/2/13 17:55
 */
public class HttpStatus implements org.apache.http.HttpStatus {

	/**
	 * @code 307
	 * 		<p>
	 * 		临时重定向响应状态码，表示请求的资源暂时地被移动到了响应的 Location 首部所指向的 URL 上。
	 * 		<p>
	 * 		原始请求中的请求方法和消息主体会在重定向请求中被重用。在确实需要将重定向请求的方法转换为 GET 的场景下，可以考虑使用 303 See Other 状态码。例如，在使用 PUT 方法进行文件上传操作时，如果需要返回一条确认信息（例如“你已经成功上传了 XYZ”），而不是返回上传的资源本身，就可以使用这个状态码。
	 * 		<p>
	 * 		状态码 307 与 302 之间的唯一区别在于，当发送重定向请求的时候，307 状态码可以确保请求方法和消息主体不会发生变化。如果使用 302 响应状态码，一些旧客户端会错误地将请求方法转换为 GET：也就是说，在 Web 中，如果使用了 GET 以外的请求方法，且返回了 302 状态码，则重定向后的请求方法是不可预测的；但如果使用 307
	 * 		状态码，之后的请求方法就是可预测的。对于 GET 请求来说，两种情况没有区别。
	 */
	public static final int SC_TEMPORARY_REDIRECT = 307;
	/**
	 * @code 425
	 * 		<p>
	 * 		服务器不愿意冒风险来处理该请求，原因是处理该请求可能会被“重放”，从而造成潜在的重放攻击
	 */
	public static final int SC_TOO_EARLY = 425;
	/**
	 * @code 426
	 * 		<p>
	 * 		一种HTTP协议的错误状态代码，表示服务器拒绝处理客户端使用当前协议发送的请求，但是可以接受其使用升级后的协议发送的请求
	 * 		<p>
	 * 		服务器会在响应中使用 Upgrade (en-US) 首部来指定要求的协议。
	 */
	public static final int SC_UPGRADE_REQUIRED = 426;
	/**
	 * @code 428
	 * 		<p>
	 * 		服务器端要求发送条件请求
	 * 		<p>
	 * 		一般的，这种情况意味着必要的条件首部——如 If-Match ——的缺失
	 * 		<p>
	 * 		当一个条件首部的值不能匹配服务器端的状态的时候，应答的状态码应该是 412 Precondition Failed，前置条件验证失败
	 */
	public static final int SC_PRECONDITION_REQUIRED = 428;
	/**
	 * @code 429
	 * 		<p>
	 * 		在一定的时间内用户发送了太多的请求，即超出了“频次限制”
	 * 		<p>
	 * 		在响应中，可以提供一个  Retry-After 首部来提示用户需要等待多长时间之后再发送新的请求。
	 */
	public static final int SC_TOO_MANY_REQUEST = 429;
	/**
	 * @code 431
	 * 		<p>
	 * 		表示由于请求中的首部字段的值过大，服务器拒绝接受客户端的请求。客户端可以在缩减首部字段的体积后再次发送请求
	 * 		<p>
	 * 		该响应码可以用于首部总体体积过大的情况，也可以用于单个首部体积过大的情况。
	 * 		<p>
	 * 		这种错误不应该出现于经过良好测试的投入使用的系统当中，而是更多出现于测试新系统的时候
	 */
	public static final int SC_REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
	/**
	 * @code 451
	 * 		<p>
	 * 		(因法律原因不可用）是一种HTTP协议的错误状态代码，表示服务器由于法律原因，无法提供客户端请求的资源，例如可能会导致法律诉讼的页面。
	 */
	public static final int SC_UNAVAILABLE_FOR_LEGAL_REASONS = 451;

	/**
	 * @code 510
	 * 		<p>
	 * 		服务器资源错误或不可用
	 * 		<p>
	 * 		私有协议状态码，由于服务器内部错误，导致资源错误或不可用
	 */
	public static final int SC_SERVER_RESOURCE_ERROR = 510;

}
