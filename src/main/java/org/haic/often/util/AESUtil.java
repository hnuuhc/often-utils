package org.haic.often.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.haic.often.Judge;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * AES工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/9/29 10:21
 */
public class AESUtil {

	private static final String ALGORITHM = "AES";

	static {
		Security.addProvider(new BouncyCastleProvider()); // 解除AES限制
	}

	/**
	 * AES解密
	 *
	 * @param bytes 待解密的数组
	 * @param key   解密key
	 * @param param 填充参数
	 * @return 解密后的数组
	 */
	public static byte[] decode(byte[] bytes, @NotNull String key, @NotNull String param) {
		try {
			byte[] params;
			if (Judge.isEmpty(param.length())) {
				params = new byte[16];
			} else if (param.length() < 16) {
				throw new RuntimeException("param length is error");
			} else {
				params = param.substring(0, 16).getBytes();
			}
			SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(params));
			return cipher.doFinal(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * AES加密
	 *
	 * @param bytes 待加密的数组
	 * @param key   加密key
	 * @return 加密后的数组
	 */
	public static byte[] encode(byte[] bytes, @NotNull String key) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);//算法是AES
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), ALGORITHM));
			return cipher.doFinal(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
