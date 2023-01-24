package org.haic.often.net.htmlunit;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.http.Response;

import java.nio.charset.Charset;

/**
 * 响应接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/16 11:52
 */
public abstract class HtmlResponse extends Response {

	/**
	 * Response字符集（ 字符串 字符集）<br/>
	 * 设置/覆盖响应字符集。解析文档正文时，它将使用此字符集。
	 *
	 * @param charsetName 字符集格式名称
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public HtmlResponse charset(@NotNull String charsetName) {
		return charset(Charset.forName(charsetName));
	}

	/**
	 * Response字符集（ 字符串 字符集）<br/>
	 * 设置/覆盖响应字符集。解析文档正文时，它将使用此字符集。
	 *
	 * @param charset 字符集格式
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public HtmlResponse charset(@NotNull Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 如果此页面是 HtmlPage，则返回 true。
	 *
	 * @return true or false
	 */
	@Contract(pure = true)
	public abstract boolean isHtmlPage();

}
