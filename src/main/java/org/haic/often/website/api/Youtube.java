package org.haic.often.website.api;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

/**
 * Youtube接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:45
 */
public abstract class Youtube {

	/**
	 * 构建解密函数,由于会下载js代码,注意需要提前设置代理
	 *
	 * @return this
	 */
	public abstract Youtube updateFunction();

	/**
	 * signatureCipher解密,如果未构建解密函数,将会下载js代码,注意需要提前设置代理
	 *
	 * @param signatureCipher signatureCipher
	 * @return 已解密的signatureCipher
	 */
	@Contract(pure = true)
	public abstract String signatureCipherDecrypt(@NotNull String signatureCipher);

}
