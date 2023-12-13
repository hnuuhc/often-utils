package org.haic.often.chrome.browser;

import org.haic.often.Judge;
import org.haic.often.annotations.NotNull;
import org.haic.often.parser.json.JSON;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.FileUtil;
import org.haic.often.util.RandomUtil;
import org.haic.often.util.ReadWriteUtil;
import org.haic.often.util.SystemUtil;

import java.io.File;
import java.nio.file.Files;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取本地浏览器cookie
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/12/24 23:15
 */
public class LocalCookie {

	static {
		try {
			Class.forName("org.sqlite.JDBC"); // load the sqlite-JDBC driver using the current class loader
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private LocalCookie() {
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
		private File storageCopy = new File(SystemUtil.DEFAULT_TEMP_DIR, RandomUtil.randomAlphanumeric(32) + ".cookies.db");

		private ChromeBrowser(@NotNull File home) {
			encryptedKey = Decrypt.getEncryptedKey(this.home = home);
			var folder = new File(home, "Default");
			storage = new File(folder, "Network\\Cookies"); // 新版本位置
			if (!storage.exists()) storage = new File(folder, "Cookies"); // 旧版本位置
			if (!storage.exists()) throw new RuntimeException("未找到 Cookies 文件");
		}

		public Browser setProfile(@NotNull String name) {
			var folder = new File(home, Judge.isEmpty(name) ? "Default" : ReadWriteUtil.orgin(new File(home, "Local State")).readJSON().getJSONObject("profile").getJSONObject("info_cache").entrySet().stream().filter(l -> ((JSONObject) l.getValue()).getString("shortcut_name").equals(name)).findFirst().orElseThrow().getKey());
			storage = new File(folder, "Network\\Cookies"); // 新版本位置
			storage = storage.exists() ? storage : new File(folder, "Cookies"); // 旧版本位置
			if (!storage.exists()) throw new RuntimeException("未找到 Cookies 文件");
			return this;
		}

		public Browser setTempDir(@NotNull String folder) {
			FileUtil.createFolder(folder);
			storageCopy = new File(folder, RandomUtil.randomAlphanumeric(32) + ".cookies.db");
			return this;
		}

		public JSONObject getForAll() {
			var result = new JSONObject();
			var cookies = processCookies(null);
			for (var cookie : cookies) {
				var domain = cookie.getDomain();
				var info = result.getJSONObject(domain);
				if (info == null) {
					result.put(cookie.getDomain(), JSON.of(cookie.getName(), cookie.getValue()));
				} else {
					info.put(cookie.getName(), cookie.getValue());
				}
			}
			return result;
		}

		public Map<String, String> getForDomain(@NotNull String domain) {
			return processCookies(domain).parallelStream().collect(Collectors.toMap(Data::getName, Data::getValue, (e1, e2) -> e1));
		}

		/**
		 * Processes all cookies in the cookie store for a given domain or all domains if domainFilter is null
		 *
		 * @param domainFilter domain
		 * @return decrypted cookie
		 */
		private Set<Data> processCookies(String domainFilter) {
			var cookies = new HashSet<Data>();
			try (var connection = DriverManager.getConnection("jdbc:sqlite:" + storageCopy.getAbsolutePath())) {
				storageCopy.delete();
				if (Files.isReadable(storage.toPath())) {
					FileUtil.copyFile(storage, storageCopy);
				} else { // 新版浏览器对文件占用锁定,必须将其解锁才能复制,目前没有解决办法
					throw new RuntimeException("文件被浏览器锁定,无法复制");
				}
				var statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 seconds
				var result = Judge.isEmpty(domainFilter) ? statement.executeQuery("select * from cookies") : statement.executeQuery("select * from cookies where host_key like \"%" + domainFilter + "%\"");
				while (result.next()) {
					var encryptedBytes = result.getBytes("encrypted_value");
					if (encryptedBytes.length == 0) continue;
					var name = result.getString("name");
					var domain = result.getString("host_key");
					cookies.add(new Data(name, new String(Decrypt.DPAPIDecode(encryptedBytes, encryptedKey)), domain));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally { // 删除备份
				storageCopy.delete();
			}
			return cookies;
		}

	}

}
