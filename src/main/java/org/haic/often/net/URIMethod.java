package org.haic.often.net;

/**
 * URI协议常量
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/9/27 11:03
 */
public enum URIMethod {

	/**
	 * http 协议
	 */
	HTTP("http"),
	/**
	 * https 协议
	 */
	HTTPS("https"),
	/**
	 * ws 协议
	 */
	WS("ws"),
	/**
	 * wws 协议
	 */
	WWS("wws");

	private final String value;

	URIMethod(String value) {
		this.value = value;
	}

	/**
	 * 获得 枚举方法的值
	 *
	 * @return value
	 */
	public String getValue() {
		return value;
	}

}
