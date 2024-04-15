package org.haic.often.netdisc;

import org.haic.often.Judge;
import org.jetbrains.annotations.NotNull;
import org.haic.often.chrome.browser.LocalStorage;
import org.haic.often.exception.YunPanException;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 阿里云盘API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/22 9:50
 */
public class ALiYunPan {

	private static final String fileListUrl = "https://api.aliyundrive.com/adrive/v3/file/list";
	private static final String shareTokenUrl = "https://api.aliyundrive.com/v2/share_link/get_share_token";
	private static final String shareLinkDownloadUrl = "https://api.aliyundrive.com/v2/file/get_share_link_download_url";
	private static final String downloadUrl = "https://api.aliyundrive.com/v2/file/get_download_url";
	private static final String createWithFoldersUrl = "https://api.aliyundrive.com/adrive/v2/file/createWithFolders";
	private static final String userInfoUrl = "https://api.aliyundrive.com/v2/user/get";
	private static final String fileSearchUrl = "https://api.aliyundrive.com/adrive/v3/file/search";
	private static final String batchUrl = "https://api.aliyundrive.com/v3/batch";
	private static final String createShareLinkUrl = "https://api.aliyundrive.com/adrive/v2/share_link/create";
	private static final String recyclebinListUrl = "https://api.aliyundrive.com/v2/recyclebin/list";

	private final JSONObject userInfo;
	private final Connection conn = HttpsUtil.newSession();

	private ALiYunPan(@NotNull String auth) {
		conn.auth(auth);
		var res = conn.url(userInfoUrl).requestBody("{}").post();
		userInfo = res.json();
		if (!URIUtil.statusIsOK(res.statusCode())) {
			throw new YunPanException(userInfo.getString("message"));
		}
	}

	/**
	 * 获取分享页面的文件信息
	 *
	 * @param shareUrl 分享链接
	 * @return 文件信息列表
	 */
	public static List<JSONObject> getInfosAsPage(String shareUrl) {
		return shareUrl.contains("#") ? getInfosAsPage(shareUrl.substring(0, shareUrl.indexOf("#")), shareUrl.substring(shareUrl.indexOf('#') + 1)) : getInfosAsPage(shareUrl, "");
	}

	/**
	 * 获取分享页面的文件信息
	 *
	 * @param shareUrl 分享链接
	 * @param sharePwd 提取码
	 * @return 文件信息列表
	 */
	public static List<JSONObject> getInfosAsPage(@NotNull String shareUrl, @NotNull String sharePwd) {
		var shareId = shareUrl.substring(shareUrl.lastIndexOf("/") + 1);
		return getInfosAsPage(shareId, getShareToken(shareId, sharePwd), "root", "/");
	}

	private static List<JSONObject> getInfosAsPage(@NotNull String shareId, @NotNull String shareToken, @NotNull String parentId, @NotNull String path) {
		var filesInfo = new ArrayList<JSONObject>();
		var data = new JSONObject();
		data.put("limit", 100);
		data.put("order_by", "name");
		data.put("fields", "*");
		data.put("parent_file_id", parentId);
		data.put("share_id", shareId);
		var items = HttpsUtil.connect(fileListUrl).requestBody(data.toString()).header("x-share-token", shareToken).post().json().getList("items", JSONObject.class);
		for (var item : items) {
			if (item.getString("type").equals("folder")) {
				filesInfo.addAll(getInfosAsPage(shareId, shareToken, item.getString("file_id"), path + item.getString("name") + "/"));
			} else {
				filesInfo.add(item.fluentPut("path", path));
			}
		}
		return filesInfo;
	}

	/**
	 * 获得分享链接的ShareToken
	 *
	 * @param shareId  分享链接ID
	 * @param sharePwd 提取码
	 * @return ShareToken
	 */
	private static String getShareToken(String shareId, String sharePwd) {
		var apiJson = new JSONObject();
		apiJson.put("share_id", shareId);
		apiJson.put("share_pwd", sharePwd);
		return HttpsUtil.connect(shareTokenUrl).requestBody(apiJson.toString()).post().json().getString("share_token");
	}

	/**
	 * 使用本地谷歌浏览器(Edge)登陆,进行需要身份验证的API操作
	 *
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static ALiYunPan localLogin() {
		return login(new JSONObject(LocalStorage.home().getForDomain("www.aliyundrive.com")).getOrDefault("token", "{}", JSONObject.class).getString("access_token"));
	}

	/**
	 * 使用本地谷歌浏览器登陆,进行需要身份验证的API操作
	 *
	 * @param userHome 本地谷歌浏览器用户数据目录(User Data)
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static ALiYunPan localLogin(@NotNull String userHome) {
		return login(new JSONObject(LocalStorage.home(userHome).getForDomain("www.aliyundrive.com")).getOrDefault("token", "{}", JSONObject.class).getString("access_token"));
	}

	/**
	 * 登陆账户,进行需要身份验证的API操作
	 *
	 * @param auth 身份识别信息,登录后,可在开发者本地存储(Local Storage)获取token项access_token值,或者在网络请求头中查找
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static ALiYunPan login(@NotNull String auth) {
		return new ALiYunPan(auth);
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileId 指定的文件或文件夹,可指定多个
	 * @return 返回的JSON数据
	 */
	public JSONObject clearRecycle(@NotNull String... fileId) {
		return clearRecycle(Arrays.asList(fileId));
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileIdList 指定的文件或文件夹ID列表
	 * @return 返回的JSON数据
	 */
	public JSONObject clearRecycle(@NotNull List<String> fileIdList) {
		var data = new JSONObject().fluentPut("requests", fileIdList.stream().map(l -> new JSONObject() {{
			put("body", new JSONObject() {{
				put("drive_id", userInfo.getString("default_drive_id"));
				put("file_id", l);
			}});
			put("headers", new JSONObject().fluentPut("Content-Type", "application/json"));
			put("id", l);
			put("method", "POST");
			put("url", "/file/delete");
		}}).toList()).fluentPut("resource", "file");
		return conn.url(batchUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 还原回收站的文件或文件夹
	 *
	 * @param fileId 文件或文件夹ID,可指定多个
	 * @return 返回的JSON数据
	 */
	public JSONObject restore(@NotNull String... fileId) {
		return restore(Arrays.asList(fileId));
	}

	/**
	 * 还原回收站的文件或文件夹
	 *
	 * @param fileIdList 文件或文件夹ID列表
	 * @return 返回的JSON数据
	 */
	public JSONObject restore(@NotNull List<String> fileIdList) {
		var data = new JSONObject().fluentPut("requests", fileIdList.stream().map(l -> new JSONObject() {{
			put("body", new JSONObject() {{
				put("drive_id", userInfo.getString("default_drive_id"));
				put("file_id", l);
			}});
			put("headers", new JSONObject().fluentPut("Content-Type", "application/json"));
			put("id", l);
			put("method", "POST");
			put("url", "/recyclebin/restore");
		}}).toList()).fluentPut("resource", "file");
		return conn.url(batchUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 获取回收站文件列表
	 *
	 * @return 返回的JSON格式文件列表
	 */
	public List<JSONObject> listRecycleBin() {
		var data = new JSONObject();
		data.put("drive_id", userInfo.getString("default_drive_id"));
		data.put("limit", 100);
		data.put("order_by", "name");
		data.put("order_direction", "DESC");
		return inquire(recyclebinListUrl, data);
	}

	/**
	 * 取消收藏文件
	 *
	 * @param fileId 文件ID,可指定多个
	 * @return 返回的JSON数据
	 */
	public JSONObject cancelCollect(@NotNull String... fileId) {
		return cancelCollect(Arrays.asList(fileId));
	}

	/**
	 * 取消收藏文件
	 *
	 * @param fileIdList 文件ID列表
	 * @return 返回的JSON数据
	 */
	public JSONObject cancelCollect(@NotNull List<String> fileIdList) {
		var data = new JSONObject().fluentPut("requests", fileIdList.stream().map(l -> new JSONObject() {{
			put("body", new JSONObject() {{
				put("drive_id", userInfo.getString("default_drive_id"));
				put("file_id", l);
				put("starred", false);
				put("custom_index_key", "");
			}});
			put("headers", new JSONObject().fluentPut("Content-Type", "application/json"));
			put("id", l);
			put("method", "PUT");
			put("url", "/file/update");
		}}).toList()).fluentPut("resource", "file");
		return conn.url(batchUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 收藏文件
	 *
	 * @param fileId 文件ID,可指定多个
	 * @return 返回的JSON数据
	 */
	public JSONObject collect(@NotNull String... fileId) {
		return collect(Arrays.asList(fileId));
	}

	/**
	 * 收藏文件
	 *
	 * @param fileIdList 文件ID列表
	 * @return 返回的JSON数据
	 */
	public JSONObject collect(@NotNull List<String> fileIdList) {
		var data = new JSONObject().fluentPut("requests", fileIdList.stream().map(l -> new JSONObject() {{
			put("body", new JSONObject() {{
				put("drive_id", userInfo.getString("default_drive_id"));
				put("file_id", l);
				put("starred", true);
				put("custom_index_key", "starred_yes");
			}});
			put("headers", new JSONObject().fluentPut("Content-Type", "application/json"));
			put("id", l);
			put("method", "PUT");
			put("url", "/file/update");
		}}).toList()).fluentPut("resource", "file");
		return conn.url(batchUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 创建分享链接
	 *
	 * @param day       分享天数,0为永久
	 * @param shareCode 分享码,空字符串为公开链接
	 * @param fileId    文件ID,可指定多个
	 * @return 返回的JSON数据
	 */
	public JSONObject share(int day, @NotNull String shareCode, @NotNull String... fileId) {
		return share(day, shareCode, Arrays.asList(fileId));
	}

	/**
	 * 创建分享链接
	 *
	 * @param day        分享天数,0为永久
	 * @param shareCode  分享码,空字符串为公开链接
	 * @param fileIdList 文件ID列表
	 * @return 返回的JSON数据
	 */
	public JSONObject share(int day, @NotNull String shareCode, @NotNull List<String> fileIdList) {
		var time = Calendar.getInstance();
		time.add(Calendar.DAY_OF_MONTH, day);
		var format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'.094Z'");
		var data = new JSONObject() {{
			put("drive_id", userInfo.getString("default_drive_id"));
			put("expiration", day == 0 ? "" : format.format(time.getTime()));
			put("file_id_list", new JSONArray().fluentAddAll(fileIdList));
			put("share_pwd", shareCode);
			put("sync_to_homepage", false);
		}};
		return conn.url(createShareLinkUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param fileId 文件或文件夹ID,可指定多个
	 * @return 返回的JSON数据
	 */
	public JSONObject delete(@NotNull String... fileId) {
		return delete(Arrays.asList(fileId));
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param fileIdList 文件或文件夹ID列表
	 * @return 返回的JSON数据
	 */
	public JSONObject delete(@NotNull List<String> fileIdList) {
		var data = new JSONObject().fluentPut("requests", fileIdList.stream().map(l -> new JSONObject() {{
			put("body", new JSONObject() {{
				put("drive_id", userInfo.getString("default_drive_id"));
				put("file_id", l);
			}});
			put("id", l);
			put("method", "POST");
			put("url", "/recyclebin/trash");
		}}).toList()).fluentPut("resource", "file");
		return conn.url(batchUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 移动文件或文件夹到指定目录
	 *
	 * @param parentId 指定目录ID
	 * @param fileId   文件或文件夹ID,可指定多个
	 * @return 返回的JSON数据
	 */
	public JSONObject move(@NotNull String parentId, @NotNull String... fileId) {
		return move(parentId, Arrays.asList(fileId));
	}

	/**
	 * 移动文件或文件夹到指定目录
	 *
	 * @param parentId   指定目录ID
	 * @param fileIdList 文件或文件夹ID列表
	 * @return 返回的JSON数据
	 */
	public JSONObject move(@NotNull String parentId, @NotNull List<String> fileIdList) {
		var data = new JSONObject().fluentPut("requests", fileIdList.stream().map(l -> new JSONObject() {{
			put("body", new JSONObject() {{
				String driveId = userInfo.getString("default_drive_id");
				put("drive_id", driveId);
				put("to_drive_id", driveId);
				put("to_parent_file_id", parentId);
				put("file_id", l);
			}});
			put("id", l);
			put("method", "POST");
			put("url", "/file/move");
		}}).toList()).fluentPut("resource", "file");
		return conn.url(batchUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 搜索匹配的文件
	 *
	 * @param search 搜索数据
	 * @return 返回的JSON格式文件列表
	 */
	public List<JSONObject> search(@NotNull String search) {
		var data = new JSONObject();
		data.put("drive_id", userInfo.getString("default_drive_id"));
		data.put("limit", 100);
		data.put("order_by", "updated_at DESC");
		data.put("query", "name match \"" + search + "\"");
		return inquire(fileSearchUrl, data);
	}

	/**
	 * 创建文件夹
	 *
	 * @param parentFileId 父文件夹ID,"root"为根目录
	 * @param fileName     文件夹名称
	 * @return 返回的JSON数据
	 */
	public JSONObject createFolder(@NotNull String parentFileId, @NotNull String fileName) {
		var data = new JSONObject();
		data.put("drive_id", userInfo.getString("default_drive_id"));
		data.put("parent_file_id", parentFileId);
		data.put("name", fileName);
		data.put("check_name_mode", "refuse");
		data.put("type", "folder");
		return conn.url(createWithFoldersUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 获取用户主页的所有文件信息
	 *
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHome() {
		return getInfosAsHomeOfFolder("root");
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息
	 *
	 * @param folderId 文件夹ID,"root"为根目录
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId) {
		var data = new JSONObject();
		data.put("drive_id", userInfo.getString("default_drive_id"));
		data.put("parent_file_id", folderId);
		data.put("limit", 100);
		data.put("all", false);
		data.put("fields", "*");
		data.put("order_by", "updated_at");
		data.put("order_direction", "DESC");
		data.put("url_expire_sec", 1600);
		return inquire(fileListUrl, data);
	}

	private List<JSONObject> inquire(@NotNull String url, @NotNull JSONObject data) {
		var infos = new JSONArray();
		var info = conn.url(url).requestBody(data.toString()).post().json();
		infos.addAll(info.getJSONArray("items"));
		var marker = info.getString("next_marker");
		while (!Judge.isEmpty(marker)) {
			info = conn.requestBody(data.fluentPut("marker", marker).toString()).post().json();
			infos.addAll(info.getJSONArray("items"));
			marker = info.getString("next_marker");
		}
		return infos.toList(JSONObject.class);
	}

	/**
	 * 获得分享页面所有文件直链(注意下载链接需要添加referer请求头参数 <a href="https://www.aliyundrive.com/">https://www.aliyundrive.com/</a>)
	 * <p>
	 * 警告: referer链接最后的 "/" 符号也要加上
	 *
	 * @param shareUrl 分享链接
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	public List<JSONObject> getStraightsAsPage(@NotNull String shareUrl) {
		return shareUrl.contains("#") ? getStraightsAsPage(shareUrl.substring(0, shareUrl.indexOf("#")), shareUrl.substring(shareUrl.indexOf('#') + 1)) : getStraightsAsPage(shareUrl, "");
	}

	/**
	 * 获得分享页面所有文件直链(注意下载链接需要添加referer请求头参数 <a href="https://www.aliyundrive.com/">https://www.aliyundrive.com/</a>)
	 * <p>
	 * 警告: referer链接最后的 "/" 符号也要加上
	 *
	 * @param shareUrl 分享链接
	 * @param sharePwd 提取码
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	public List<JSONObject> getStraightsAsPage(@NotNull String shareUrl, @NotNull String sharePwd) {
		var shareId = shareUrl.substring(shareUrl.lastIndexOf("/") + 1);
		var shareToken = getShareToken(shareId, sharePwd);
		var filesInfo = getInfosAsPage(shareId, sharePwd);
		for (var fileInfo : filesInfo) {
			var data = new JSONObject().fluentPut("share_id", shareId).fluentPut("file_id", fileInfo.getString("file_id"));
			var connection = conn.url(shareLinkDownloadUrl).header("x-share-token", shareToken).requestBody(data.toString());
			fileInfo.put("url", connection.post().json().getString("download_url"));
			connection.newRequest();
		}
		return filesInfo;
	}

	/**
	 * 获取用户主页的文件直链(注意下载链接需要添加referer请求头参数 <a href="https://www.aliyundrive.com/">https://www.aliyundrive.com/</a>)
	 * <p>
	 * 警告: referer链接最后的 "/" 符号也要加上
	 *
	 * @param fileid 文件ID
	 * @return 文件直链
	 */
	public String getStraight(@NotNull String fileid) {
		var data = new JSONObject().fluentPut("drive_id", userInfo.getString("default_drive_id")).fluentPut("file_id", fileid);
		return conn.url(downloadUrl).requestBody(data.toString()).post().json().getString("url");
	}

}
