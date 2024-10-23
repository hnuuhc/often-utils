package org.haic.often.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class AESUtil {

    private static final String AES = "AES";
    private static final String AES_CBC = "AES/CBC/PKCS5Padding";

    /**
     * 加密
     *
     * @param data 待加密明文数据
     * @param key  加密密码
     * @param iv   加密向量（偏移量）
     * @return 加密值
     */
    public static byte[] encrypt(String data, String key, String iv) {
        return aes(data.getBytes(), decodeHex(key), decodeHex(iv), Cipher.ENCRYPT_MODE);
    }

    /**
     * 加密
     *
     * @param data 待加密明文数据
     * @param key  加密密码
     * @param iv   加密向量（偏移量）
     * @return 加密值
     */
    public static byte[] encrypt(byte[] data, String key, String iv) {
        return aes(data, decodeHex(key), decodeHex(iv), Cipher.ENCRYPT_MODE);
    }

    /**
     * 解密
     *
     * @param data 加密后的数据
     * @param key  解密密码
     * @param iv   解密向量（偏移量）
     * @return 解密值
     */
    public static byte[] decrypt(String data, String key, String iv) {
        return aes(data.getBytes(), decodeHex(key), decodeHex(iv), Cipher.DECRYPT_MODE);
    }

    /**
     * 解密
     *
     * @param data 加密后的数据
     * @param key  解密密码
     * @param iv   解密向量（偏移量）
     * @return 解密值
     */
    public static byte[] decrypt(byte[] data, String key, String iv) {
        return aes(data, decodeHex(key), decodeHex(iv), Cipher.DECRYPT_MODE);
    }

    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     *
     * @param input 原始字节数组
     * @param key   符合AES要求的密钥
     * @param iv    初始向量
     * @param mode  Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    private static byte[] aes(byte[] input, byte[] key, byte[] iv, int mode) {
        try {
            var secretKey = new SecretKeySpec(key, AES);
            var ivSpec = new IvParameterSpec(iv);
            var cipher = Cipher.getInstance(AES_CBC);
            cipher.init(mode, secretKey, ivSpec);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decodeHex(final char[] data) throws Exception {

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new Exception("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * Hex解码.
     */
    public static byte[] decodeHex(String input) {
        try {
            return decodeHex(input.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int toDigit(final char ch, final int index) throws Exception {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new Exception("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

}
