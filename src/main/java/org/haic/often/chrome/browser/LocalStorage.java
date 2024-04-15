package org.haic.often.chrome.browser;

import com.protonail.leveldb.jna.LevelDB;
import com.protonail.leveldb.jna.LevelDBKeyValueIterator;
import com.protonail.leveldb.jna.LevelDBOptions;
import com.protonail.leveldb.jna.LevelDBReadOptions;
import org.haic.often.Judge;
import org.jetbrains.annotations.NotNull;
import org.haic.often.net.URIUtil;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取本地浏览器 Local Storage 数据
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 4:03
 */
public class LocalStorage {

	private LocalStorage() {
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

		private File storageCopy = new File(SystemUtil.DEFAULT_TEMP_DIR, RandomUtil.randomAlphanumeric(32) + ".leveldb");

		private ChromeBrowser(File home) {
			storage = new File(new File(this.home = home, "Default"), "Local Storage\\leveldb");
			if (!storage.exists()) throw new RuntimeException("未找到 Local Storage leveldb 目录");
		}

		public Browser setProfile(@NotNull String name) {
			storage = new File(new File(home, Judge.isEmpty(name) ? "Default" : ReadWriteUtil.orgin(new File(home, "Local State")).readJSON().getJSONObject("profile").getJSONObject("info_cache").entrySet().stream().filter(l -> ((JSONObject) l.getValue()).getString("shortcut_name").equals(name)).findFirst().orElseThrow().getKey()), "Local Storage\\leveldb");
			if (!storage.exists()) throw new RuntimeException("未找到 Local Storage leveldb 目录");
			return this;
		}

		public Browser setTempDir(@NotNull String folder) {
			FileUtil.createFolder(folder);
			storageCopy = new File(folder, RandomUtil.randomAlphanumeric(32) + ".cookies.db");
			return this;
		}

		public JSONObject getForAll() {
			var result = new JSONObject();
			var storages = processLevelDB(null);
			for (var storage : storages) {
				var domain = storage.getDomain();
				var info = result.getJSONObject(domain);
				if (info == null) {
					result.put(domain, JSONObject.of(storage.getName(), storage.getValue()));
				} else {
					info.put(storage.getName(), storage.getValue());
				}
			}
			return result;
		}

		public Map<String, String> getForDomain(@NotNull String domain) {
			return processLevelDB(domain).parallelStream().collect(Collectors.toMap(Data::getName, Data::getValue, (e1, e2) -> e2));
		}

		/**
		 * Processes all local storage in the localStorageStore for a given domain or all domains if domainFilter is null
		 *
		 * @param domainFilter domain
		 * @return decrypted local storage
		 */
		private Set<Data> processLevelDB(String domainFilter) {
			FileUtil.copyDirectory(storage, storageCopy);
			var result = new HashSet<Data>();
			try (var levelDB = new LevelDB(storageCopy.getPath(), new LevelDBOptions()); var iterator = new LevelDBKeyValueIterator(levelDB, new LevelDBReadOptions() {{
				setFillCache(false);// 遍历中swap出来的数据，不应该保存在memtable中
				setSnapshot(levelDB.createSnapshot());
			}})) {
				while (iterator.hasNext()) {
					var entry = iterator.next();
					var keyBytes = entry.getKey();
					if (keyBytes[1] == 104) {
						int index = StringUtil.search(keyBytes, (byte) 0);
						var decrypt = Decrypt.levelDBDecode(Arrays.copyOfRange(keyBytes, 0, index));
						if (decrypt.contains("^0")) continue;
						var domain = URIUtil.getHost(decrypt);
						if (!Judge.isEmpty(domainFilter) && !domain.contains(domainFilter)) {
							continue;
						}
						result.add(new Data(Decrypt.levelDBDecode(Arrays.copyOfRange(keyBytes, index + 1, keyBytes.length)), Decrypt.levelDBDecode(entry.getValue()), domain));
					}
				}
			} finally {
				FileUtil.deleteDirectory(storageCopy);
			}
			return result;
		}

	}
}
