package org.haic.often.website.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Youtube接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:45
 */
public abstract class Youtube {

	/**
	 * signatureCipher解密
	 *
	 * @param signatureCipher signatureCipher
	 * @return 已解密的signatureCipher
	 */
	@Contract(pure = true)
	public abstract String signatureCipherDecrypt(@NotNull String signatureCipher);

}
