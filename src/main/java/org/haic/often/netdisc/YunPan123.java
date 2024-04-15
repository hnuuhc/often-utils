package org.haic.often.netdisc;

import org.jetbrains.annotations.NotNull;
import org.haic.often.chrome.browser.LocalCookie;
import org.haic.often.exception.YunPanException;
import org.haic.often.net.download.SionDownload;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.Base64Util;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 123云盘API,获取直链需要登陆
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/2/26 16:01
 */
public class YunPan123 {

	private static final String shareGetUrl = "https://www.123pan.com/b/api/share/get";
	private static final String downloadUrl = "https://www.123pan.com/a/api/share/download/info";
	private static final String batchDownloadUrl = "https://www.123pan.com/a/api/file/batch_download_share_info";
	private static final String filelistUrl = "https://www.123pan.com/a/api/file/list/new";
	private static final String fileDownloadInfoUrl = "https://www.123pan.com/a/api/file/download_info";
	private static final String uploadRequestUrl = "https://www.123pan.com/a/api/file/upload_request";
	private static final String modPidUrl = "https://www.123pan.com/a/api/file/mod_pid";
	private static final String trashUrl = "https://www.123pan.com/a/api/file/trash";
	private static final String renameUrl = "https://www.123pan.com/a/api/file/rename";
	private static final String createShareUrl = "https://www.123pan.com/a/api/share/create";
	private static final String shareListUrl = "https://www.123pan.com/a/api/share/list";
	private static final String shareDeleteUrl = "https://www.123pan.com/a/api/share/delete";
	private static final String fileDeleteUrl = "https://www.123pan.com/a/api/file/delete";
	private static final String trashDeleteAllUrl = "https://www.123pan.com/a/api/file/trash_delete_all";
	private static final String fileTrashUrl = "https://www.123pan.com/a/api/file/trash";
	private static final String userInfoUrl = "https://www.123pan.com/b/api/user/info";

	private final Connection conn = HttpsUtil.newSession();

	private YunPan123(@NotNull String auth) {
		conn.auth(auth);
		var status = conn.url(userInfoUrl).execute().json();
		if (status.getInteger("code") != 0) {
			throw new YunPanException(status.getString("message"));
		}
	}

	/**
	 * 获取分享页所有文件的信息
	 *
	 * @param shareUrl 分享URL
	 * @return 包含文件信息的JSON数据格式列表
	 */
	public static List<JSONObject> getInfosAsPage(@NotNull String shareUrl) {
		return getInfosAsPage(shareUrl, "");
	}

	/**
	 * 获取分享页所有文件的信息
	 *
	 * @param shareUrl 分享URL
	 * @param sharePwd 提取码
	 * @return 包含文件信息的JSON数据格式列表
	 */
	public static List<JSONObject> getInfosAsPage(@NotNull String shareUrl, @NotNull String sharePwd) {
		return getInfosAsPage(shareUrl.substring(shareUrl.lastIndexOf("/") + 1), sharePwd, "0", 1, "/");
	}

	private static List<JSONObject> getInfosAsPage(String key, String sharePwd, String parentId, int page, String path) {
		var data = new HashMap<String, String>();
		data.put("limit", "100");
		data.put("next", "1");
		data.put("orderBy", "share_id");
		data.put("orderDirection", "desc");
		data.put("shareKey", key);
		data.put("sharePwd", sharePwd);
		data.put("ParentFileId", parentId);
		data.put("Page", String.valueOf(page));
		var pageInfo = HttpsUtil.connect(shareGetUrl).data(data).get().json().getJSONObject("data");
		var filesInfo = new ArrayList<JSONObject>();
		for (var info : pageInfo.getList("InfoList", JSONObject.class)) {
			var fileId = info.getString("FileId");
			int type = info.getInteger("Type");
			if (type == 1) {
				filesInfo.addAll(getInfosAsPage(key, sharePwd, fileId, 1, path + info.getString("FileName") + "/"));
			} else {
				filesInfo.add(info.fluentPut("Path", path));
			}
		}
		int next = pageInfo.getInteger("Next");
		if (next != -1) {
			filesInfo.addAll(getInfosAsPage(key, sharePwd, "0", page + 1, path));
		}
		return filesInfo;
	}

	/**
	 * 获取分享页所有文件的批量下载链接
	 *
	 * @param shareUrl 分享URL
	 * @param sharePwd 提取码
	 * @return Map - 文件名, 文件直链
	 */
	public static String getStraightsAsPageOfBatch(@NotNull String shareUrl, @NotNull String sharePwd) {
		var data = new JSONObject().fluentPut("ShareKey", shareUrl.substring(shareUrl.lastIndexOf("/") + 1)).fluentPut("fileIdList", getInfosAsPage(shareUrl, sharePwd).stream().map(l -> new JSONObject().fluentPut("fileId", l.getString("FileId"))).toList());
		var url = HttpsUtil.connect(batchDownloadUrl).requestBody(data).post().json().getJSONObject("data").getString("DownloadUrl");
		return Base64Util.decode(url.substring(url.indexOf("=") + 1));
	}

	/**
	 * 获取分享页所有文件直链
	 *
	 * @param shareUrl 分享URL
	 * @param sharePwd 提取码
	 * @return Map - 文件名(含路径), 文件直链
	 */
	public static Map<String, String> getStraightsAsPage(@NotNull String shareUrl, @NotNull String sharePwd) {
		var result = new HashMap<String, String>();
		var shareKey = shareUrl.substring(shareUrl.lastIndexOf("/") + 1);
		for (var info : getInfosAsPage(shareUrl, sharePwd)) {
			var data = new HashMap<String, String>();
			data.put("Etag", info.getString("Etag"));
			data.put("FileID", info.getString("FileId"));
			data.put("S3keyFlag", info.getString("S3KeyFlag"));
			data.put("ShareKey", shareKey);
			data.put("Size", info.getString("Size"));
			var url = HttpsUtil.connect(downloadUrl).data(data).post().json().getJSONObject("data").getString("DownloadURL");
			result.put(info.getString("Path") + info.getString("FileName"), Base64Util.decode(url.substring(url.indexOf("=") + 1)));
		}
		return result;
	}

	/**
	 * 获取分享页所有文件直链
	 *
	 * @param shareUrl 分享URL
	 * @return Map - 文件名(含路径), 文件直链
	 */
	public static Map<String, String> getStraightsAsPage(@NotNull String shareUrl) {
		return getStraightsAsPage(shareUrl, "");
	}

	/**
	 * 使用本地谷歌浏览器(Edge)登陆,进行需要身份验证的API操作
	 *
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static YunPan123 localLogin() {
		return login(LocalCookie.home().getForDomain("www.123pan.com").getOrDefault("authorToken", null));
	}

	/**
	 * 使用本地谷歌浏览器登陆,进行需要身份验证的API操作
	 *
	 * @param userHome 本地谷歌浏览器用户数据目录(User Data)
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static YunPan123 localLogin(@NotNull String userHome) {
		return login(LocalCookie.home(userHome).getForDomain("www.123pan.com").getOrDefault("authorToken", null));
	}

	/**
	 * 通过账号密码登陆账号,进行需要身份验证的API操作
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static YunPan123 login(@NotNull String username, @NotNull String password) {
		return new YunPan123(YunPanLogin.login(username, password));
	}

	/**
	 * 通过身份识别标识登陆账号,进行需要是否验证的API操作
	 *
	 * @param auth 身份识别标识
	 * @return 此链接, 用于API操作
	 */
	public static YunPan123 login(@NotNull String auth) {
		return new YunPan123(auth);
	}

	/**
	 * 获取回收站的文件列表
	 *
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	public List<JSONObject> listRecycleBin() {
		return getInfoListAsHome("0", "", true);
	}

	/**
	 * 还原回收站的文件或文件夹
	 *
	 * @param fileId 文件或文件夹ID,可指定多个
	 * @return 操作返回的结果状态码, 一般情况下, 0为成功
	 */
	public int restore(@NotNull String... fileId) {
		return restore(Arrays.asList(fileId));
	}

	/**
	 * 还原回收站的文件或文件夹
	 *
	 * @param fileIdList 文件或文件夹ID列表
	 * @return 操作返回的结果状态码, 一般情况下, 0为成功
	 */
	public int restore(@NotNull List<String> fileIdList) {
		var data = new JSONObject();
		data.put("driveId", "0");
		data.put("operation", "false");
		data.put("fileTrashInfoList", fileIdList.stream().map(l -> new JSONObject().fluentPut("fileId", l)).toList());
		return conn.url(fileTrashUrl).requestBody(data.toString()).post().json().getInteger("code");
	}

	/**
	 * 清空回收站
	 *
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	public int clearRecycle() {
		return conn.url(trashDeleteAllUrl).requestBody(new JSONObject().toString()).post().json().getInteger("code");
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileId 指定的文件或文件夹,可指定多个
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	public int clearRecycle(@NotNull String... fileId) {
		return clearRecycle(Arrays.asList(fileId));
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileIdList 指定的文件或文件夹ID列表
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	public int clearRecycle(@NotNull List<String> fileIdList) {
		return conn.url(fileDeleteUrl).requestBody(new JSONObject().fluentPut("fileIdList", fileIdList.stream().map(l -> new JSONObject().fluentPut("fileId", l)).toList()).toString()).post().json().getInteger("code");
	}

	/**
	 * 取消已分享的文件
	 *
	 * @param shareId 分享ID,可指定多个
	 * @return 返回执行结果代码, 一般情况下, 0为成功
	 */
	public int unShare(@NotNull String... shareId) {
		return unShare(Arrays.asList(shareId));
	}

	/**
	 * 取消已分享的文件
	 *
	 * @param shareIdList 分享ID列表
	 * @return 返回执行结果代码, 一般情况下, 0为成功
	 */
	public int unShare(@NotNull List<String> shareIdList) {
		return conn.url(shareDeleteUrl).requestBody(new JSONObject().fluentPut("driveId", "0").fluentPut("shareInfoList", shareIdList.stream().map(l -> new JSONObject().fluentPut("shareId", l)).toList()).toString()).post().json().getInteger("code");
	}

	/**
	 * 获取已分享列表
	 *
	 * @return 文件列表信息JSON数组
	 */
	public List<JSONObject> listShares() {
		return listShares("");
	}

	/**
	 * 获取匹配搜索项的已分享列表
	 *
	 * @param search 搜索数据
	 * @return 文件列表信息JSON数组
	 */
	public List<JSONObject> listShares(@NotNull String search) {
		return conn.url(shareListUrl).requestBody("driveId=0&limit=10000&next=0&orderBy=fileId&orderDirection=desc&SearchData=" + search).get().json().getJSONObject("data").getList("InfoList", JSONObject.class);
	}

	/**
	 * 分享指定文件
	 *
	 * @param shareName 分享显示的名称
	 * @param fileId    分享文件ID,如果存在多个,用','分割
	 * @return 返回的JSON数据
	 */
	public JSONObject share(@NotNull String shareName, @NotNull String fileId) {
		return share(shareName, fileId, "", 1);
	}

	/**
	 * 分享指定文件
	 *
	 * @param shareName 分享显示的名称
	 * @param fileId    分享文件ID,如果存在多个,用','分割
	 * @param day       分享时间
	 * @return 返回的JSON数据
	 */
	public JSONObject share(@NotNull String shareName, @NotNull String fileId, int day) {
		return share(shareName, fileId, "", day);
	}

	/**
	 * 分享指定文件
	 *
	 * @param shareName 分享显示的名称
	 * @param fileId    分享文件ID,如果存在多个,用','分割
	 * @param sharePwd  分享密码
	 * @return 返回的JSON数据
	 */
	public JSONObject share(@NotNull String shareName, @NotNull String fileId, @NotNull String sharePwd) {
		return share(shareName, fileId, sharePwd, 1);
	}

	/**
	 * 分享指定文件
	 *
	 * @param shareName 分享显示的名称
	 * @param fileId    分享文件ID,如果存在多个,用','分割
	 * @param sharePwd  分享密码
	 * @param day       分享时间
	 * @return 返回的JSON数据
	 */
	public JSONObject share(@NotNull String shareName, @NotNull String fileId, @NotNull String sharePwd, int day) {
		var time = Calendar.getInstance();
		time.add(Calendar.DAY_OF_MONTH, day);
		var format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssXXX");
		var data = new JSONObject();
		data.put("driveId", "0");
		data.put("expiration", format.format(time.getTime()));
		data.put("fileIdList", fileId);
		data.put("shareName", shareName);
		data.put("sharePwd", sharePwd);
		return conn.url(createShareUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 创建文件夹
	 *
	 * @param parentId 父文件夹ID,0为根目录
	 * @param fileName 文件夹名称
	 * @return 返回的JSON数据
	 */
	public JSONObject createFolder(@NotNull String parentId, @NotNull String fileName) {
		var data = new JSONObject();
		data.put("driveId", "0");
		data.put("etag", "");
		data.put("fileName", fileName);
		data.put("parentFileId", parentId);
		data.put("size", "0");
		data.put("type", "1");
		data.put("duplicate", "1");
		data.put("NotReuse", "true");
		return conn.url(uploadRequestUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 重命名文件或文件夹
	 *
	 * @param fileId   文件ID
	 * @param fileName 重命名后的文件名
	 * @return 返回执行结果代码, 一般情况下, 0为成功
	 */
	public int rename(@NotNull String fileId, @NotNull String fileName) {
		var data = new JSONObject();
		data.put("driveId", "0");
		data.put("fileId", fileId);
		data.put("fileName", fileName);
		return conn.url(renameUrl).requestBody(data.toString()).post().json().getInteger("code");
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param fileId 待删除的文件ID,可指定多个
	 * @return 返回执行结果代码, 一般情况下, 0为成功
	 */
	public int delete(@NotNull String... fileId) {
		return delete(List.of(fileId));
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param fileIdList 待删除的文件ID列表
	 * @return 返回执行结果代码, 一般情况下, 0为成功
	 */
	public int delete(@NotNull List<String> fileIdList) {
		var data = new JSONObject();
		data.put("driveId", "0");
		data.put("operation", "true");
		data.put("fileTrashInfoList", new JSONArray().fluentAddAll(fileIdList.stream().map(l -> new JSONObject().fluentPut("fileId", l)).toList()));
		return conn.url(trashUrl).requestBody(data.toString()).post().json().getInteger("code");
	}

	/**
	 * 移动文件到指定文件夹下
	 *
	 * @param parentId 移动后的文件夹ID
	 * @param fileId   需要移动的文件ID,可指定多个
	 * @return 返回执行结果代码, 一般情况下, 0为成功
	 */
	public int move(@NotNull String parentId, @NotNull String... fileId) {
		return move(parentId, List.of(fileId));
	}

	/**
	 * 移动文件到指定文件夹下
	 *
	 * @param parentId   移动后的文件夹ID
	 * @param fileIdList 需要移动的文件ID列表
	 * @return 返回执行结果代码, 一般情况下, 0为成功
	 */
	public int move(@NotNull String parentId, @NotNull List<String> fileIdList) {
		var data = new JSONObject();
		data.put("parentFileId", parentId);
		data.put("fileIdList", new JSONArray().fluentAddAll(fileIdList.stream().map(l -> new JSONObject().fluentPut("fileId", l)).toList()));
		return conn.url(modPidUrl).requestBody(data.toString()).post().json().getInteger("code");
	}

	/**
	 * 获取用户主页的所有文件信息
	 *
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHome() {
		return getInfosAsHome("");
	}

	/**
	 * 获取用户主页的匹配搜索项的文件信息
	 *
	 * @param search 待搜索数据
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHome(@NotNull String search) {
		return getInfosAsHomeOfFolder("0", search);
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息
	 *
	 * @param folderId 文件夹ID
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId) {
		return getInfosAsHomeOfFolder(folderId, "");
	}

	/**
	 * 获取用户主页的指定文件夹下的匹配搜索项的文件信息
	 *
	 * @param folderId 文件夹ID
	 * @param search   待搜索数据
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId, @NotNull String search) {
		return getInfoListAsHome(folderId, search, false);
	}

	/**
	 * 获取文件夹内文件信息
	 *
	 * @param fileId  文件夹ID
	 * @param search  搜索数据
	 * @param trashed 是否为垃圾站
	 * @return 文件列表
	 */
	private List<JSONObject> getInfoListAsHome(@NotNull String fileId, @NotNull String search, boolean trashed) {
		var data = new HashMap<String, String>();
		data.put("driveId", "0");
		data.put("limit", "1000");
		data.put("next", "0");
		data.put("orderBy", "fileId");
		data.put("orderDirection", "desc");
		data.put("parentFileId", fileId);
		data.put("trashed", String.valueOf(trashed));
		data.put("SearchData", search);
		data.put("Page", "1");
		var filesInfo = new JSONArray();
		var info = conn.url(filelistUrl).data(data).get().json().getJSONObject("data");
		filesInfo.addAll(info.getJSONArray("InfoList"));
		for (int i = 2; !info.getString("Next").equals("-1"); i++) {
			data.put("Page", String.valueOf(i));
			info = conn.url(filelistUrl).data(data).get().json().getJSONObject("data");
			filesInfo.addAll(info.getJSONArray("InfoList"));
		}
		return filesInfo.toList(JSONObject.class);
	}

	/**
	 * 通过文件信息配置获取文件直链
	 *
	 * @param fileInfo 文件信息
	 * @return 文件直链
	 */
	public String getStraight(@NotNull JSONObject fileInfo) {
		JSONObject data = new JSONObject();
		data.put("type", fileInfo.getString("Type"));
		if (data.getInteger("type") == 1) {
			return null;
		}
		data.put("driveId", "0");
		data.put("fileName", fileInfo.getString("FileName"));
		data.put("fileId", fileInfo.getString("FileId"));
		data.put("size", fileInfo.getString("Size"));
		data.put("etag", fileInfo.getString("Etag"));
		data.put("s3KeyFlag", fileInfo.get("S3KeyFlag"));
		return conn.url(fileDownloadInfoUrl).requestBody(data.toString()).post().json().getJSONObject("data").getString("DownloadUrl");
	}

	/**
	 * 通过指定的文件信息下载文件
	 *
	 * @param fileInfo   JSON文件信息
	 * @param folderPath 存放的文件夹路径
	 */
	public void download(@NotNull JSONObject fileInfo, @NotNull String folderPath) {
		SionDownload.connect(getStraight(fileInfo)).fileName(fileInfo.getString("FileName")).folder(folderPath + fileInfo.getString("Path")).execute();
	}

	/**
	 * 通过指定的文件信息列表下载文件
	 *
	 * @param fileInfos  JSON文件信息
	 * @param folderPath 存放的文件夹路径
	 */
	public void download(@NotNull List<JSONObject> fileInfos, @NotNull String folderPath) {
		fileInfos.forEach(l -> download(l, folderPath));
	}

	public static class YunPanLogin {

		private static final String signinUrl = "https://www.123pan.com/a/api/user/sign_in";

		/**
		 * 通过账号密码登录获得用户身份识别标识,可在请求头中使用
		 *
		 * @param username 用户名
		 * @param password 密码
		 * @return 此链接, 用于API操作
		 */
		public static String login(@NotNull String username, @NotNull String password) {
			return HttpsUtil.connect(signinUrl).requestBody(new JSONObject().fluentPut("passport", username).fluentPut("password", password).toString()).post().json().getJSONObject("data").getString("token");
		}

	}

}
