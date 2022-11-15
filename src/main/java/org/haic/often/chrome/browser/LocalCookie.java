package org.haic.often.chrome.browser;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.haic.often.util.FileUtil;
import org.haic.often.util.ReadWriteUtil;
import org.haic.often.util.SystemUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取本地浏览器cookie
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/12/24 23:15
 */
public class LocalCookie {

	private LocalCookie() {
	}

	/**
	 * 本地谷歌浏览器(Edge)用户数据目录(User Data)
	 *
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public static Browser home() {
		return home(new File(SystemUtil.DEFAULT_USER_HOME, "AppData\\Local\\Microsoft\\Edge\\User Data"));
	}

	/**
	 * 本地谷歌浏览器用户数据目录(User Data)
	 *
	 * @param home User Data目录路径
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public static Browser home(@NotNull String home) {
		return home(new File(home));
	}

	/**
	 * 本地谷歌浏览器用户数据目录(User Data)
	 *
	 * @param home User Data目录
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public static Browser home(@NotNull File home) {
		return new ChromeBrowser(home);
	}

	public static abstract class Cookie {

		protected String name;
		protected byte[] value;
		protected Date expires;
		protected String path;
		protected String domain;
		protected File cookieStore;

		private Cookie(String name, byte[] value, Date expires, String path, String domain, File cookieStore) {
			this.name = name;
			this.value = value;
			this.expires = expires;
			this.path = path;
			this.domain = domain;
			this.cookieStore = cookieStore;
		}

		public String getName() {
			return name;
		}

		public byte[] getValueBytes() {
			return value;
		}

		public Date getExpires() {
			return expires;
		}

		public String getPath() {
			return path;
		}

		public String getDomain() {
			return domain;
		}

		public File getCookieStore() {
			return cookieStore;
		}

		public String getValue() {
			return new String(value);
		}

		public abstract boolean isDecrypted();

	}

	private static class DecryptedCookie extends Cookie {

		private final String decryptedValue;

		private DecryptedCookie(String name, byte[] encryptedValue, String decryptedValue, Date expires, String path, String domain, File cookieStore) {
			super(name, encryptedValue, expires, path, domain, cookieStore);
			this.decryptedValue = decryptedValue;
		}

		@Override
		public boolean isDecrypted() {
			return true;
		}

		@Override
		public String toString() {
			return "Cookie [name=" + name + ", value=" + decryptedValue + Symbol.CLOSE_BRACKET;
		}

		@Override
		public String getValue() {
			return decryptedValue;
		}

	}

	private static class EncryptedCookie extends Cookie {

		public EncryptedCookie(String name, byte[] encryptedValue, Date expires, String path, String domain, File cookieStore) {
			super(name, encryptedValue, expires, path, domain, cookieStore);
		}

		@Override
		public boolean isDecrypted() {
			return false;
		}

		@Override
		public String toString() {
			return "Cookie [name=" + name + " (encrypted)]";
		}

	}

	private static class ChromeBrowser extends Browser {

		private final String encryptedKey;
		private File storageCopy = new File(SystemUtil.DEFAULT_TEMP_DIR, RandomStringUtils.randomAlphanumeric(32) + ".cookies.db");

		private ChromeBrowser(@NotNull File home) {
			encryptedKey = Decrypt.getEncryptedKey(this.home = home);
			File folder = new File(home, "Default");
			storage = new File(folder, "Network\\Cookies"); // 新版本位置
			storage = storage.exists() ? storage : new File(folder, "Cookies"); // 旧版本位置
		}

		@Contract(pure = true)
		public Browser setProfile(@NotNull String name) {
			File folder = new File(home, Judge.isEmpty(name) ? "Default" : JSONObject.parseObject(ReadWriteUtil.orgin(new File(home, "Local State")).read()).getJSONObject("profile").getJSONObject("info_cache").entrySet().stream().filter(l -> ((JSONObject) l.getValue()).getString("shortcut_name").equals(name)).findFirst().orElseThrow().getKey());
			storage = new File(folder, "Network\\Cookies"); // 新版本位置
			storage = storage.exists() ? storage : new File(folder, "Cookies"); // 旧版本位置
			return this;
		}

		@Contract(pure = true)
		public Browser setTempDir(@NotNull String folder) {
			FileUtil.createFolder(folder);
			storageCopy = new File(folder, RandomStringUtils.randomAlphanumeric(32) + ".cookies.db");
			return this;
		}

		@Contract(pure = true)
		public Map<String, Map<String, String>> getForAll() {
			Map<String, Map<String, String>> result = new HashMap<>();
			Set<Cookie> cookies = processCookies(null);
			for (Cookie cookie : cookies) {
				String domain = cookie.getDomain();
				Map<String, String> info = result.get(domain);
				if (info == null) {
					result.put(cookie.getDomain(), new HashMap<>(Map.of(cookie.getName(), cookie.getValue())));
				} else {
					info.put(cookie.getName(), cookie.getValue());
				}
			}
			return result;
		}

		@Contract(pure = true)
		public Map<String, String> getForDomain(@NotNull String domain) {
			return processCookies(domain).parallelStream().filter(l -> !Judge.isEmpty(l.getValue())).collect(Collectors.toMap(LocalCookie.Cookie::getName, LocalCookie.Cookie::getValue, (e1, e2) -> e2));
		}

		/**
		 * Processes all cookies in the cookie store for a given domain or all domains if domainFilter is null
		 *
		 * @param domainFilter domain
		 * @return decrypted cookie
		 */
		@Contract(pure = true)
		private Set<Cookie> processCookies(String domainFilter) {
			Set<Cookie> cookies = new HashSet<>();
			try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + storageCopy.getAbsolutePath())) {
				Class.forName("org.sqlite.JDBC");  // load the sqlite-JDBC driver using the current class loader
				storageCopy.delete();
				FileUtil.copyFile(storage, storageCopy);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 seconds
				ResultSet result = Judge.isEmpty(domainFilter) ? statement.executeQuery("select * from cookies") : statement.executeQuery("select * from cookies where host_key like \"%" + domainFilter + "%\"");
				while (result.next()) {
					String name = result.getString("name");
					byte[] encryptedBytes = result.getBytes("encrypted_value");
					String path = result.getString("path");
					String domain = result.getString("host_key");
					Date expires = result.getDate("expires_utc");
					cookies.add(decrypt(new EncryptedCookie(name, encryptedBytes, expires, path, domain, storage)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally { // 删除备份
				storageCopy.delete();
			}
			return cookies;
		}

		/**
		 * Decrypts an encrypted cookie
		 *
		 * @param encryptedCookie decrypted cookie
		 * @return decrypted cookie
		 */
		private DecryptedCookie decrypt(EncryptedCookie encryptedCookie) {
			return new DecryptedCookie(encryptedCookie.getName(), encryptedCookie.getValueBytes(), new String(Decrypt.DPAPIDecode(encryptedCookie.value, encryptedKey)), encryptedCookie.getExpires(), encryptedCookie.getPath(), encryptedCookie.getDomain(), encryptedCookie.getCookieStore());
		}

	}

}
