package org.haic.often.chrome.browser;

import org.haic.often.Judge;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.URIUtil;
import org.haic.often.parser.json.JSON;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.FileUtil;
import org.haic.often.util.RandomUtil;
import org.haic.often.util.ReadWriteUtil;
import org.haic.often.util.SystemUtil;

import java.io.File;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	public static Browser home() {
		return home(new File(SystemUtil.DEFAULT_USER_HOME, "AppData\\Local\\Microsoft\\Edge\\User Data"));
	}

	/**
	 * 本地谷歌浏览器用户数据目录(User Data)
	 *
	 * @param home User Data目录路径
	 * @return 此连接, 用于链接
	 */
	public static Browser home(@NotNull String home) {
		return home(new File(home));
	}

	/**
	 * 本地谷歌浏览器用户数据目录(User Data)
	 *
	 * @param home User Data目录
	 * @return 此连接, 用于链接
	 */
	public static Browser home(@NotNull File home) {
		return new ChromeBrowser(home);
	}

	private static class ChromeBrowser extends Browser {

		private final byte[] encryptedKey;
		private File storageCopy = new File(SystemUtil.DEFAULT_TEMP_DIR, RandomUtil.randomAlphanumeric(32) + ".loginData.db");

		private ChromeBrowser(@NotNull File home) {
			encryptedKey = Decrypt.getEncryptedKey(this.home = home);
			storage = new File(new File(home, "Default"), "Login Data");
			if (!storage.exists()) throw new RuntimeException("未找到 Login Data 文件");
		}

		public Browser setProfile(@NotNull String name) {
			storage = new File(new File(home, Judge.isEmpty(name) ? "Default" : ReadWriteUtil.orgin(new File(home, "Local State")).readJSON().getJSONObject("profile").getJSONObject("info_cache").entrySet().stream().filter(l -> ((JSONObject) l.getValue()).getString("shortcut_name").equals(name)).findFirst().orElseThrow().getKey()), "Login Data");
			if (!storage.exists()) throw new RuntimeException("未找到 Login Data 文件");
			return this;
		}

		public Browser setTempDir(@NotNull String folder) {
			FileUtil.createFolder(folder);
			storageCopy = new File(folder, RandomUtil.randomAlphanumeric(32) + ".cookies.db");
			return this;
		}

		public JSONObject getForAll() {
			var result = new JSONObject();
			var loginDatas = processLoginData(null);
			for (var loginData : loginDatas) {
				var domain = loginData.getDomain();
				var info = result.getJSONObject(domain);
				if (info == null) {
					result.put(loginData.getDomain(), JSON.of(loginData.getName(), loginData.getValue()));
				} else {
					info.put(loginData.getName(), loginData.getValue());
				}
			}
			return result;
		}

		public Map<String, String> getForDomain(@NotNull String domain) {
			return processLoginData(domain).parallelStream().collect(Collectors.toMap(Data::getName, Data::getValue, (e1, e2) -> e2));
		}

		/**
		 * Processes all loginData in the loginDataStore for a given domain or all domains if domainFilter is null
		 *
		 * @param domainFilter domain
		 * @return decrypted login data
		 */
		private Set<Data> processLoginData(String domainFilter) {
			var loginDataList = new HashSet<Data>();
			try (var connection = DriverManager.getConnection("jdbc:sqlite:" + storageCopy.getAbsolutePath())) {
				Class.forName("org.sqlite.JDBC"); // load the sqlite-JDBC driver using the current class loader
				storageCopy.delete();
				FileUtil.copyFile(storage, storageCopy);
				var statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 seconds
				var result = Judge.isEmpty(domainFilter) ? statement.executeQuery("select * from logins") : statement.executeQuery("select * from logins where signon_realm like \"%" + domainFilter + "%\"");
				while (result.next()) {
					var encryptedBytes = result.getBytes("password_value");
					if (encryptedBytes.length == 0) continue;
					var name = result.getString("username_value");
					var domain = result.getString("signon_realm");
					loginDataList.add(new Data(name, new String(Decrypt.DPAPIDecode(encryptedBytes, encryptedKey)), URIUtil.getHost(domain)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally { // 删除备份
				storageCopy.delete();
			}
			return loginDataList;
		}

	}

}
