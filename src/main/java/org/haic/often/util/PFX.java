package org.haic.often.util;

import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * pfx证书格式文件转keystore格式
 */
public class PFX {

	public static KeyStore tokeyStore(String path, char[] password) {
		try (var fis = new FileInputStream(path)) {
			var inputKeyStore = KeyStore.getInstance("PKCS12");
			inputKeyStore.load(fis, password);
			var outputKeyStore = KeyStore.getInstance("JKS");
			outputKeyStore.load(null, password);

			var enums = inputKeyStore.aliases();
			while (enums.hasMoreElements()) {
				var keyAlias = enums.nextElement();
				if (inputKeyStore.isKeyEntry(keyAlias)) {
					var key = inputKeyStore.getKey(keyAlias, password);
					var certChain = inputKeyStore.getCertificateChain(keyAlias);
					outputKeyStore.setKeyEntry(keyAlias, key, password, certChain);
				}
			}
			return outputKeyStore;
		} catch (Exception e) {
			return null;
		}
	}

}
