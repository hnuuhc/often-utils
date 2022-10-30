package org.haic.often.chrome.browser;

import com.alibaba.fastjson2.JSONObject;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.haic.often.util.FileUtil;
import org.haic.often.util.ReadWriteUtil;
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
 * 获取本地浏览器账号密码
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/20 20:04
 */
public class LocalLoginData {

	private LocalLoginData() {
	}

	/**
	 * 本地谷歌浏览器(Edge)用户数据目录(User Data)
	 *
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public static Browser home() {
		return home(new File(System.getProperty("user.home"), "AppData\\Local\\Microsoft\\Edge\\User Data"));
	}

	/**
	 * 本地谷歌浏览器用户数据目录(User Data)
	 *
	 * @param userHome User Data目录路径
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public static Browser home(@NotNull String userHome) {
		return home(new File(userHome));
	}

	/**
	 * 本地谷歌浏览器用户数据目录(User Data)
	 *
	 * @param userHome User Data目录
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public static Browser home(@NotNull File userHome) {
		return new ChromeBrowser(userHome);
	}

	public static abstract class LoginData {

		protected String name;
		protected byte[] value;
		protected Date created;
		protected String domain;
		protected File loginData;

		private LoginData(String name, byte[] value, Date created, String domain, File loginData) {
			this.name = name;
			this.value = value;
			this.created = created;
			this.domain = domain;
			this.loginData = loginData;
		}

		public String getName() {
			return name;
		}

		public Date getCreated() {
			return created;
		}

		public byte[] getValueBytes() {
			return value;
		}

		public String getDomain() {
			return domain;
		}

		public File getCookieStore() {
			return loginData;
		}

		public String getValue() {
			return new String(value);
		}

		public abstract boolean isDecrypted();

	}

	private static class DecryptedLoginData extends LoginData {

		private final String decryptedValue;

		private DecryptedLoginData(String name, byte[] encryptedValue, String decryptedValue, Date created, String domain, File loginData) {
			super(name, encryptedValue, created, domain, loginData);
			this.decryptedValue = decryptedValue;
		}

		@Override
		public boolean isDecrypted() {
			return true;
		}

		@Override
		public String toString() {
			return "LoginData [name=" + name + ", value=" + decryptedValue + Symbol.CLOSE_BRACKET;
		}

		@Override
		public String getValue() {
			return decryptedValue;
		}

	}

	private static class EncryptedLoginData extends LoginData {

		public EncryptedLoginData(String name, byte[] encryptedValue, Date created, String domain, File cookieStore) {
			super(name, encryptedValue, created, domain, cookieStore);
		}

		@Override
		public boolean isDecrypted() {
			return false;
		}

		@Override
		public String toString() {
			return "LoginData [name=" + name + " (encrypted)]";
		}

	}

	private static class ChromeBrowser extends Browser {

		private final String encryptedKey;
		private final File storageCopy = new File(".loginData.db");

		private ChromeBrowser(@NotNull File userHome) {
			encryptedKey = Decrypt.getEncryptedKey(this.userHome = userHome);
			storage = new File(new File(userHome, "Default"), "Login Data");
		}

		public Browser setProfile(@NotNull String name) {
			File folder = new File(userHome, JSONObject.parseObject(ReadWriteUtil.orgin(new File(userHome, "Local State")).read()).getJSONObject("profile").getJSONObject("info_cache").entrySet().stream().filter(l -> ((JSONObject) l.getValue()).getString("shortcut_name").equals(name)).findFirst().orElseThrow().getKey());
			storage = new File(folder, "Login Data");
			return this;
		}

		public Map<String, Map<String, String>> getForAll() {
			Map<String, Map<String, String>> result = new HashMap<>();
			Set<LoginData> loginDatas = processLoginData(null);
			for (LoginData loginData : loginDatas) {
				String domain = loginData.getDomain();
				Map<String, String> info = result.get(domain);
				if (info == null) {
					result.put(loginData.getDomain(), new HashMap<>(Map.of(loginData.getName(), loginData.getValue())));
				} else {
					info.put(loginData.getName(), loginData.getValue());
				}
			}
			return result;
		}

		public Map<String, String> getForDomain(@NotNull String domain) {
			return processLoginData(domain).parallelStream().filter(l -> !Judge.isEmpty(l.getValue())).collect(Collectors.toMap(LoginData::getName, LoginData::getValue, (e1, e2) -> e2));
		}

		/**
		 * Processes all loginData in the loginDataStore for a given domain or all domains if domainFilter is null
		 *
		 * @param domainFilter domain
		 * @return decrypted login data
		 */
		private Set<LoginData> processLoginData(String domainFilter) {
			Set<LoginData> loginDatas = new HashSet<>();
			try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + storageCopy.getAbsolutePath())) {
				Class.forName("org.sqlite.JDBC"); // load the sqlite-JDBC driver using the current class loader
				storageCopy.delete();
				FileUtil.copyFile(storage, storageCopy);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 seconds
				ResultSet result = Judge.isEmpty(domainFilter) ? statement.executeQuery("select * from logins") : statement.executeQuery("select * from logins where signon_realm like \"%" + domainFilter + "%\"");
				while (result.next()) {
					String name = result.getString("username_value");
					byte[] encryptedBytes = result.getBytes("password_value");
					String domain = result.getString("signon_realm");
					Date created = result.getDate("date_created");
					loginDatas.add(decrypt(new EncryptedLoginData(name, encryptedBytes, created, domain, storage)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally { // 删除备份
				storageCopy.delete();
			}
			return loginDatas;
		}

		/**
		 * Decrypts an encrypted login data
		 *
		 * @param encryptedLoginData encrypted login data
		 * @return decrypted login data
		 */
		private DecryptedLoginData decrypt(@NotNull EncryptedLoginData encryptedLoginData) {
			return new DecryptedLoginData(encryptedLoginData.getName(), encryptedLoginData.getValueBytes(), new String(Decrypt.DPAPIDecode(encryptedLoginData.value, encryptedKey)), encryptedLoginData.getCreated(), encryptedLoginData.getDomain(), encryptedLoginData.getCookieStore());
		}

	}

}