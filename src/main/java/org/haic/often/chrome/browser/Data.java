package org.haic.often.chrome.browser;

import java.io.File;

/**
 * 用于存储浏览器读取的数据
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/3/27 13:47
 */
public class Data {

	protected String name;
	protected String value;
	protected String domain;
	protected File cookieStore;

	public Data(String name, String value, String domain) {
		this.name = name;
		this.value = value;
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public String getDomain() {
		return domain;
	}

	public String getValue() {
		return value;
	}

	public String toString() {
		return "Data [name=" + name + ", value=" + value + "]";
	}

}
