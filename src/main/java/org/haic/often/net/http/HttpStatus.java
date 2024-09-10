package org.haic.often.net.http;

/**
 * Http状态码类,拓展了一些未收录的状态码
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/2/13 17:55
 */
public interface HttpStatus {

	// --- 1xx Informational ---

	/** {@code 100 Continue} (HTTP/1.1 - RFC 2616) */
	int SC_CONTINUE = 100;
	/** {@code 101 Switching Protocols} (HTTP/1.1 - RFC 2616) */
	int SC_SWITCHING_PROTOCOLS = 101;
	/** {@code 102 Processing} (WebDAV - RFC 2518) */
	int SC_PROCESSING = 102;

	// --- 2xx Success ---

	/** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
	int SC_OK = 200;
	/** {@code 201 Created} (HTTP/1.0 - RFC 1945) */
	int SC_CREATED = 201;
	/** {@code 202 Accepted} (HTTP/1.0 - RFC 1945) */
	int SC_ACCEPTED = 202;
	/** {@code 203 Non Authoritative Information} (HTTP/1.1 - RFC 2616) */
	int SC_NON_AUTHORITATIVE_INFORMATION = 203;
	/** {@code 204 No Content} (HTTP/1.0 - RFC 1945) */
	int SC_NO_CONTENT = 204;
	/** {@code 205 Reset Content} (HTTP/1.1 - RFC 2616) */
	int SC_RESET_CONTENT = 205;
	/** {@code 206 Partial Content} (HTTP/1.1 - RFC 2616) */
	int SC_PARTIAL_CONTENT = 206;
	/**
	 * {@code 207 Multi-Status} (WebDAV - RFC 2518)
	 * or
	 * {@code 207 Partial Update OK} (HTTP/1.1 - draft-ietf-http-v11-spec-rev-01?)
	 */
	int SC_MULTI_STATUS = 207;

	// --- 3xx Redirection ---

	/** {@code 300 Mutliple Choices} (HTTP/1.1 - RFC 2616) */
	int SC_MULTIPLE_CHOICES = 300;
	/** {@code 301 Moved Permanently} (HTTP/1.0 - RFC 1945) */
	int SC_MOVED_PERMANENTLY = 301;
	/** {@code 302 Moved Temporarily} (Sometimes {@code Found}) (HTTP/1.0 - RFC 1945) */
	int SC_MOVED_TEMPORARILY = 302;
	/** {@code 303 See Other} (HTTP/1.1 - RFC 2616) */
	int SC_SEE_OTHER = 303;
	/** {@code 304 Not Modified} (HTTP/1.0 - RFC 1945) */
	int SC_NOT_MODIFIED = 304;
	/** {@code 305 Use Proxy} (HTTP/1.1 - RFC 2616) */
	int SC_USE_PROXY = 305;
	/** {@code 307 Temporary Redirect} (HTTP/1.1 - RFC 2616) */
	int SC_TEMPORARY_REDIRECT = 307;

	// --- 4xx Client Error ---

	/** {@code 400 Bad Request} (HTTP/1.1 - RFC 2616) */
	int SC_BAD_REQUEST = 400;
	/** {@code 401 Unauthorized} (HTTP/1.0 - RFC 1945) */
	int SC_UNAUTHORIZED = 401;
	/** {@code 402 Payment Required} (HTTP/1.1 - RFC 2616) */
	int SC_PAYMENT_REQUIRED = 402;
	/** {@code 403 Forbidden} (HTTP/1.0 - RFC 1945) */
	int SC_FORBIDDEN = 403;
	/** {@code 404 Not Found} (HTTP/1.0 - RFC 1945) */
	int SC_NOT_FOUND = 404;
	/** {@code 405 Method Not Allowed} (HTTP/1.1 - RFC 2616) */
	int SC_METHOD_NOT_ALLOWED = 405;
	/** {@code 406 Not Acceptable} (HTTP/1.1 - RFC 2616) */
	int SC_NOT_ACCEPTABLE = 406;
	/** {@code 407 Proxy Authentication Required} (HTTP/1.1 - RFC 2616) */
	int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
	/** {@code 408 Request Timeout} (HTTP/1.1 - RFC 2616) */
	int SC_REQUEST_TIMEOUT = 408;
	/** {@code 409 Conflict} (HTTP/1.1 - RFC 2616) */
	int SC_CONFLICT = 409;
	/** {@code 410 Gone} (HTTP/1.1 - RFC 2616) */
	int SC_GONE = 410;
	/** {@code 411 Length Required} (HTTP/1.1 - RFC 2616) */
	int SC_LENGTH_REQUIRED = 411;
	/** {@code 412 Precondition Failed} (HTTP/1.1 - RFC 2616) */
	int SC_PRECONDITION_FAILED = 412;
	/** {@code 413 Request Entity Too Large} (HTTP/1.1 - RFC 2616) */
	int SC_REQUEST_TOO_LONG = 413;
	/** {@code 414 Request-URI Too Long} (HTTP/1.1 - RFC 2616) */
	int SC_REQUEST_URI_TOO_LONG = 414;
	/** {@code 415 Unsupported Media Type} (HTTP/1.1 - RFC 2616) */
	int SC_UNSUPPORTED_MEDIA_TYPE = 415;
	/** {@code 416 Requested Range Not Satisfiable} (HTTP/1.1 - RFC 2616) */
	int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	/** {@code 417 Expectation Failed} (HTTP/1.1 - RFC 2616) */
	int SC_EXPECTATION_FAILED = 417;

	/**
	 * Static constant for a 419 error.
	 * {@code 419 Insufficient Space on Resource}
	 * (WebDAV - draft-ietf-webdav-protocol-05?)
	 * or {@code 419 Proxy Reauthentication Required}
	 * (HTTP/1.1 drafts?)
	 */
	int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
	/**
	 * Static constant for a 420 error.
	 * {@code 420 Method Failure}
	 * (WebDAV - draft-ietf-webdav-protocol-05?)
	 */
	int SC_METHOD_FAILURE = 420;
	/** {@code 422 Unprocessable Entity} (WebDAV - RFC 2518) */
	int SC_UNPROCESSABLE_ENTITY = 422;
	/** {@code 423 Locked} (WebDAV - RFC 2518) */
	int SC_LOCKED = 423;
	/** {@code 424 Failed Dependency} (WebDAV - RFC 2518) */
	int SC_FAILED_DEPENDENCY = 424;
	/** {@code 429 Too Many Requests} (Additional HTTP Status Codes - RFC 6585) */
	int SC_TOO_MANY_REQUESTS = 429;

	// --- 5xx Server Error ---

	/** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
	int SC_INTERNAL_SERVER_ERROR = 500;
	/** {@code 501 Not Implemented} (HTTP/1.0 - RFC 1945) */
	int SC_NOT_IMPLEMENTED = 501;
	/** {@code 502 Bad Gateway} (HTTP/1.0 - RFC 1945) */
	int SC_BAD_GATEWAY = 502;
	/** {@code 503 Service Unavailable} (HTTP/1.0 - RFC 1945) */
	int SC_SERVICE_UNAVAILABLE = 503;
	/** {@code 504 Gateway Timeout} (HTTP/1.1 - RFC 2616) */
	int SC_GATEWAY_TIMEOUT = 504;
	/** {@code 505 HTTP Version Not Supported} (HTTP/1.1 - RFC 2616) */
	int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

	/** {@code 507 Insufficient Storage} (WebDAV - RFC 2518) */
	int SC_INSUFFICIENT_STORAGE = 507;

	/**
	 * @code 425
	 * <p>
	 * 服务器不愿意冒风险来处理该请求，原因是处理该请求可能会被“重放”，从而造成潜在的重放攻击
	 */
	int SC_TOO_EARLY = 425;
	/**
	 * @code 426
	 * <p>
	 * 一种HTTP协议的错误状态代码，表示服务器拒绝处理客户端使用当前协议发送的请求，但是可以接受其使用升级后的协议发送的请求
	 * <p>
	 * 服务器会在响应中使用 Upgrade (en-US) 首部来指定要求的协议。
	 */
	int SC_UPGRADE_REQUIRED = 426;
	/**
	 * @code 428
	 * <p>
	 * 服务器端要求发送条件请求
	 * <p>
	 * 一般的，这种情况意味着必要的条件首部——如 If-Match ——的缺失
	 * <p>
	 * 当一个条件首部的值不能匹配服务器端的状态的时候，应答的状态码应该是 412 Precondition Failed，前置条件验证失败
	 */
	int SC_PRECONDITION_REQUIRED = 428;
	/**
	 * @code 429
	 * <p>
	 * 在一定的时间内用户发送了太多的请求，即超出了“频次限制”
	 * <p>
	 * 在响应中，可以提供一个  Retry-After 首部来提示用户需要等待多长时间之后再发送新的请求。
	 */
	int SC_TOO_MANY_REQUEST = 429;
	/**
	 * @code 431
	 * <p>
	 * 表示由于请求中的首部字段的值过大，服务器拒绝接受客户端的请求。客户端可以在缩减首部字段的体积后再次发送请求
	 * <p>
	 * 该响应码可以用于首部总体体积过大的情况，也可以用于单个首部体积过大的情况。
	 * <p>
	 * 这种错误不应该出现于经过良好测试的投入使用的系统当中，而是更多出现于测试新系统的时候
	 */
	int SC_REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
	/**
	 * @code 451
	 * <p>
	 * (因法律原因不可用）是一种HTTP协议的错误状态代码，表示服务器由于法律原因，无法提供客户端请求的资源，例如可能会导致法律诉讼的页面。
	 */
	int SC_UNAVAILABLE_FOR_LEGAL_REASONS = 451;

	/**
	 * @code 510
	 * <p>
	 * 服务器资源错误或不可用
	 * <p>
	 * 私有协议状态码，由于服务器内部错误，导致资源错误或不可用
	 */
	int SC_SERVER_RESOURCE_ERROR = 510;

}
