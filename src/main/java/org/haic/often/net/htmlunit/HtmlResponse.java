package org.haic.often.net.htmlunit;

import org.haic.often.annotations.Contract;
import org.haic.often.net.http.Response;

/**
 * 响应接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/16 11:52
 */
public abstract class HtmlResponse extends Response {

	/**
	 * 如果此页面是 HtmlPage，则返回 true。
	 *
	 * @return true or false
	 */
	@Contract(pure = true)
	public abstract boolean isHtmlPage();

}
