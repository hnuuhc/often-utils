package org.haic.often.chrome.browser;

import com.protonail.leveldb.jna.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.haic.often.Judge;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.URIUtil;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.FileUtil;
import org.haic.often.util.ReadWriteUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.util.SystemUtil;

import java.io.File;
import java.util.*;
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

	public static class Storage {

		private final String domain;
		private final String key;
		private final String value;

		private Storage(String domain, String key, String value) {
			this.domain = domain;
			this.key = key;
			this.value = value;
		}

		public String getDomain() {
			return domain;
		}

		public String getName() {
			return key;
		}

		public String getValue() {
			return value;
		}

	}

	private static class ChromeBrowser extends Browser {

		private File storageCopy = new File(SystemUtil.DEFAULT_TEMP_DIR, RandomStringUtils.randomAlphanumeric(32) + ".leveldb");

		private ChromeBrowser(File home) {
			storage = new File(new File(this.home = home, "Default"), "Local Storage\\leveldb");
		}

		@Contract(pure = true)
		public Browser setProfile(@NotNull String name) {
			storage = new File(new File(home, Judge.isEmpty(name) ? "Default" : JSONObject.parseObject(ReadWriteUtil.orgin(new File(home, "Local State")).read()).getJSONObject("profile").getJSONObject("info_cache").entrySet().stream().filter(l -> ((JSONObject) l.getValue()).getString("shortcut_name").equals(name)).findFirst().orElseThrow().getKey()), "Local Storage\\leveldb");
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
			Set<Storage> storages = processLevelDB(null);
			for (Storage storage : storages) {
				String domain = storage.getDomain();
				Map<String, String> info = result.get(domain);
				if (info == null) {
					result.put(domain, new HashMap<>(Map.of(storage.getName(), storage.getValue())));
				} else {
					info.put(storage.getName(), storage.getValue());
				}
			}
			return result;
		}

		@Contract(pure = true)
		public Map<String, String> getForDomain(@NotNull String domain) {
			return processLevelDB(domain).parallelStream().filter(l -> !Judge.isEmpty(l.getValue())).collect(Collectors.toMap(Storage::getName, Storage::getValue, (e1, e2) -> e2));
		}

		/**
		 * Processes all local storage in the localStorageStore for a given domain or all domains if domainFilter is null
		 *
		 * @param domainFilter domain
		 * @return decrypted local storage
		 */
		@Contract(pure = true)
		private Set<Storage> processLevelDB(String domainFilter) {
			FileUtil.copyDirectory(storage, storageCopy);
			Set<Storage> result = new HashSet<>();
			try (LevelDB levelDB = new LevelDB(storageCopy.getPath(), new LevelDBOptions()); LevelDBKeyValueIterator iterator = new LevelDBKeyValueIterator(levelDB, new LevelDBReadOptions() {{
				setFillCache(false);// 遍历中swap出来的数据，不应该保存在memtable中
				setSnapshot(levelDB.createSnapshot());
			}})) {
				while (iterator.hasNext()) {
					KeyValuePair entry = iterator.next();
					byte[] keyBytes = entry.getKey();
					if (keyBytes[1] == 104) {
						int index = StringUtil.search(keyBytes, (byte) 0);
						String domain = URIUtil.getHost(Decrypt.levelDBDecode(Arrays.copyOfRange(keyBytes, 0, index)));
						if (!Judge.isEmpty(domainFilter) && !domain.contains(domainFilter)) {
							continue;
						}
						result.add(new Storage(domain, Decrypt.levelDBDecode(Arrays.copyOfRange(keyBytes, index + 1, keyBytes.length)), Decrypt.levelDBDecode(entry.getValue())));
					}
				}
			} finally {
				FileUtil.deleteDirectory(storageCopy);
			}
			return result;
		}

	}
}
