package org.haic.often.util;

import org.haic.often.exception.AESException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * AES工具CBC/PKCS7Padding加解密
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/13 12:59
 */
public class PKCS7Padding {

	/**
	 * AES加密
	 *
	 * @param data 数据
	 * @param key  密钥
	 * @param iv   偏移量
	 * @return 加密后的数据
	 */
	public static byte[] encode(byte[] data, byte[] key, byte[] iv) {
		try {
			if (iv.length != 16) throw new AESException("偏移量IV长度不为16");
			int padding = 16 - (data.length % 16); // 数据长度应该为16的倍数
			data = Arrays.copyOf(data, data.length + padding);
			for (int i = data.length - padding; i < data.length; i++) data[i] = (byte) padding; // 填充参数
			SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new AESException(e);
		}
	}

	/**
	 * AES解密
	 *
	 * @param data 数据
	 * @param key  密钥
	 * @param iv   偏移量
	 * @return 解密后的数据
	 */
	public static byte[] decode(byte[] data, byte[] key, byte[] iv) {
		try {
			if (iv.length != 16) throw new AESException("偏移量IV长度不为16");
			SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] original = cipher.doFinal(data);
			int replenish = original[original.length - 1]; // 获取填充参数
			return Arrays.copyOf(original, original.length - replenish); // 去除填充参数
		} catch (Exception e) {
			throw new AESException(e);
		}
	}

	/**
	 * 将字符串转换为IV偏移量
	 *
	 * @param hexString 偏移量字符串
	 * @return byte数组
	 */
	public static byte[] toIV(String hexString) {
		byte[] byteArray = new byte[(hexString = hexString.toLowerCase()).length() >> 1];
		for (int i = 0, index = 0; i < hexString.length(); i++) {
			if (index > hexString.length() - 1) return byteArray;
			byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
			byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
			byteArray[i] = (byte) (highDit << 4 | lowDit);
			index += 2;
		}
		return byteArray;
	}

}
