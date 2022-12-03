package org.haic.often.netdisc;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.haic.often.Judge;
import org.haic.often.chrome.browser.LocalCookie;
import org.haic.often.exception.YunPanException;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.net.http.Response;
import org.haic.often.parser.xml.Document;
import org.haic.often.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * 天翼云盘API,获取直链需要登陆
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/18 22:43
 */
public class TianYiYunPan {

	private static final String loginUrl = "https://cloud.189.cn/api/portal/loginUrl.action";
	private static final String listShareDirUrl = "https://cloud.189.cn/api/open/share/listShareDir.action";
	private static final String shareInfoByCodeUrl = "https://cloud.189.cn/api/open/share/getShareInfoByCode.action";
	private static final String fileDownloadUrl = "https://cloud.189.cn/api/open/file/getFileDownloadUrl.action";
	private static final String listFilesUrl = "https://cloud.189.cn/api/open/file/listFiles.action";
	private static final String createBatchTaskUrl = "https://cloud.189.cn/api/open/batch/createBatchTask.action";
	private static final String createShareLinkUrl = "https://cloud.189.cn/api/open/share/createShareLink.action";
	private static final String cancelShareUrl = "https://cloud.189.cn/api/portal/cancelShare.action";
	private static final String listSharesUrl = "https://cloud.189.cn/api/portal/listShares.action?";
	private static final String renameFileUrl = "https://cloud.189.cn/api/open/file/renameFile.action";
	private static final String renameFolderUrl = "https://cloud.189.cn/api/open/file/renameFolder.action";
	private static final String createFolderUrl = "https://cloud.189.cn/api/open/file/createFolder.action";
	private static final String listRecycleBinFilesUrl = "https://cloud.189.cn/api/open/file/listRecycleBinFiles.action";
	private static final String userInfoForPortalUrl = "https://cloud.189.cn/api/open/user/getUserInfoForPortal.action";

	private final Connection conn = HttpsUtil.newSession();

	private TianYiYunPan(@NotNull Map<String, String> cookies) {
		conn.cookies(cookies).header("accept", "application/json;charset=UTF-8").url(loginUrl).execute(); // 获取会话cookie
		Response res = conn.url(userInfoForPortalUrl).execute();
		if (!URIUtil.statusIsOK(res.statusCode())) {
			throw new YunPanException(JSONObject.parseObject(res.body()).getString("errorMsg"));
		}
	}

	/**
	 * 获得分享页面所有文件的信息
	 *
	 * @param url 天翼URL
	 * @return List - JSON数据类型,包含文件所有信息
	 */
	@Contract(pure = true)
	public static List<JSONObject> getInfoAsPage(@NotNull String url) {
		return getInfoAsPage(url, "");
	}

	/**
	 * 获得分享页面所有文件的信息
	 *
	 * @param url       天翼URL
	 * @param shareCode 提取码
	 * @return List - JSON数据类型,包含文件所有信息
	 */
	@Contract(pure = true)
	public static List<JSONObject> getInfoAsPage(@NotNull String url, @NotNull String shareCode) {
		JSONObject info = getshareUrlInfo(url);
		String shareId = info.getString("shareId");
		Map<String, String> data = new HashMap<>();
		data.put("fileId", info.getString("fileId"));
		data.put("shareId", shareId);
		data.put("isFolder", info.getString("isFolder"));
		data.put("shareMode", info.getString("shareMode"));
		data.put("accessCode", shareCode);
		List<JSONObject> result = getInfoAsPage(data);
		result.forEach(l -> l.put("shareId", shareId));
		return result;
	}

	/**
	 * 获取分享URL的ID等信息
	 *
	 * @param shareUrl 天翼URL
	 * @return JSON数据类型
	 */
	@Contract(pure = true)
	private static JSONObject getshareUrlInfo(@NotNull String shareUrl) {
		return JSONObject.parseObject(HttpsUtil.connect(shareInfoByCodeUrl).data("shareCode", shareUrl.contains("code") ? StringUtil.extract(shareUrl, "code=.*").substring(5) : shareUrl.substring(shareUrl.lastIndexOf("/") + 1)).header("accept", "application/json;charset=UTF-8").get().text());
	}

	/**
	 * 通过配置获得分享页面所有文件的信息,如果文件非常多的话,可能要花较长时间
	 *
	 * @param data 配置信息,必须包含key:
	 *             <blockquote>
	 *             <pre>		"fileId"</pre>
	 *             <pre>		"shareId"</pre>
	 *             <pre>		"isFolder"</pre>
	 *             <pre>		"shareMode"</pre>
	 *             <pre>		"accessCode"</pre>
	 *             </blockquote>
	 * @return List - JSON数据类型,包含文件所有信息
	 */
	@Contract(pure = true)
	private static List<JSONObject> getInfoAsPage(@NotNull Map<String, String> data) {
		Connection conn = HttpsUtil.connect(listShareDirUrl).header("accept", "application/json;charset=UTF-8");
		JSONObject infos = JSONObject.parseObject(conn.data(data).execute().body()).getJSONObject("fileListAO");
		if (infos == null || Judge.isEmpty(infos.getInteger("count"))) {
			return new ArrayList<>();
		}
		List<JSONObject> filesInfo = infos.getJSONArray("fileList").toList(JSONObject.class);
		Map<String, String> thisData = new HashMap<>(data);
		for (JSONObject folderInfo : infos.getJSONArray("folderList").toList(JSONObject.class)) {
			thisData.put("fileId", folderInfo.getString("id"));
			filesInfo.addAll(getInfoAsPage(thisData));
		}
		return filesInfo;
	}

	/**
	 * 使用本地谷歌浏览器(Edge)登陆,进行需要身份验证的API操作
	 *
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static TianYiYunPan localLogin() {
		return login(LocalCookie.home().getForDomain("e.189.cn"));
	}

	/**
	 * 使用本地谷歌浏览器登陆,进行需要身份验证的API操作
	 *
	 * @param userHome 本地谷歌浏览器用户数据目录(User Data)
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static TianYiYunPan localLogin(@NotNull String userHome) {
		return login(LocalCookie.home(userHome).getForDomain("e.189.cn"));
	}

	/**
	 * 登陆账户,进行需要身份验证的API操作
	 *
	 * @param userName 用户名 (不含@189.cn后缀)
	 * @param password 密码
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static TianYiYunPan login(@NotNull String userName, @NotNull String password) {
		return login(YunPanLogin.login(userName, password));
	}

	/**
	 * 登陆账户,进行需要是否验证的API操作
	 *
	 * @param cookies cookies
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static TianYiYunPan login(@NotNull Map<String, String> cookies) {
		return new TianYiYunPan(cookies);
	}

	/**
	 * 获取分享页面所有文件信息
	 *
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	@Contract(pure = true)
	public List<JSONObject> listShares() {
		Map<String, String> data = new HashMap<>();
		data.put("pageNum", "1");
		data.put("pageSize", "1");
		data.put("shareType", "1");
		data.put("pageSize", JSONObject.parseObject(conn.url(listSharesUrl).data(data).execute().body()).getString("recordCount"));
		return JSONObject.parseObject(conn.url(listSharesUrl).data(data).execute().body()).getJSONArray("data").toList(JSONObject.class);
	}

	/**
	 * 取消分享文件
	 *
	 * @param shareId 分享ID,可指定多个
	 * @return 返回的响应结果状态码
	 */
	@Contract(pure = true)
	public int unShare(@NotNull String... shareId) {
		return unShare(Arrays.asList(shareId));
	}

	/**
	 * 取消分享文件
	 *
	 * @param shareIdList 分享ID列表
	 * @return 返回的响应结果状态码
	 */
	@Contract(pure = true)
	public int unShare(@NotNull List<String> shareIdList) {
		return JSONObject.parseObject(conn.url(cancelShareUrl).requestBody("shareIdList=" + String.join(",", shareIdList) + "&cancelType=" + 1).execute().body()).getInteger("res_code");
	}

	/**
	 * 自定义分享文件
	 *
	 * @param fileId 待分享的文件ID
	 * @param time   分享的时间(例: 1为1天), 永久为2099
	 * @param type   分享类型: 2-公开,3 - 私密,other - 社交
	 * @return 响应结果
	 */
	@Contract(pure = true)
	public JSONObject share(@NotNull String fileId, int time, int type) {
		return JSONObject.parseObject(conn.url(createShareLinkUrl).requestBody("fileId=" + fileId + "&expireTime=" + time + "&shareType=" + type).execute().body());
	}

	/**
	 * 获取回收站的文件列表
	 *
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	@Contract(pure = true)
	public List<JSONObject> listRecycleBin() {
		Map<String, String> data = new HashMap<>();
		data.put("pageNum", "1");
		data.put("pageSize", "30");
		data.put("iconOption", "1");
		data.put("family", "false");
		Function<JSONObject, List<JSONObject>> list = l -> l.getJSONArray("fileList").fluentAddAll(l.getJSONArray("folderList")).toList(JSONObject.class);
		JSONObject info = JSONObject.parseObject(conn.url(listRecycleBinFilesUrl).data(data).execute().body());
		List<JSONObject> result = list.apply(info);
		int page = (int) Math.ceil(info.getDouble("count") / 30);
		for (int i = 2; i <= page; i++) {
			info = JSONObject.parseObject(conn.data("pageNum", String.valueOf(i)).execute().body());
			result.addAll(list.apply(info));
		}
		return result;
	}

	/**
	 * 还原多个回收站的文件或文件夹
	 *
	 * @param fileInfo 指定的文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @return 操作返回的结果状态码, 一般情况下, 0为成功
	 */
	@Contract(pure = true)
	public int restore(@NotNull JSONObject fileInfo) {
		return batchTask("RESTORE", fileInfo, "");
	}

	/**
	 * 还原回收站的文件或文件夹
	 *
	 * @param filesInfo 指定的多个文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @return 操作返回的结果状态码, 一般情况下, 0为成功
	 */
	@Contract(pure = true)
	public int restore(@NotNull List<JSONObject> filesInfo) {
		return batchTask("RESTORE", filesInfo, "");
	}

	/**
	 * 清空回收站
	 *
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int clearRecycle() {
		return batchTask("EMPTY_RECYCLE", "[]", "");
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileInfo 指定的文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int clearRecycle(@NotNull JSONObject fileInfo) {
		return batchTask("CLEAR_RECYCLE", fileInfo, "");
	}

	/**
	 * 删除回收站的文件或文件夹
	 *
	 * @param filesInfo 指定的多个文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int clearRecycle(@NotNull List<JSONObject> filesInfo) {
		return batchTask("CLEAR_RECYCLE", filesInfo, "");
	}

	/**
	 * 重命名文件夹名称
	 *
	 * @param folderId   文件夹ID
	 * @param folderName 重命名后的文件夹名称
	 * @return 返回的响应结果状态码
	 */
	@Contract(pure = true)
	public int renameFolder(@NotNull String folderId, String folderName) {
		return JSONObject.parseObject(conn.url(renameFolderUrl).requestBody("folderId=" + folderId + "&destFolderName=" + folderName).execute().body()).getInteger("res_code");
	}

	/**
	 * 重命名文件名称
	 *
	 * @param fileId   文件ID
	 * @param fileName 重命名后的文件名称
	 * @return 返回的响应结果状态码
	 */
	@Contract(pure = true)
	public int renameFile(@NotNull String fileId, String fileName) {
		return JSONObject.parseObject(conn.url(renameFileUrl).requestBody("fileId=" + fileId + "&destFileName=" + fileName).execute().body()).getInteger("res_code");
	}

	/**
	 * 创建文件夹
	 *
	 * @param parentId   父文件夹ID
	 * @param folderName 文件夹名称
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject createFolder(@NotNull String parentId, String folderName) {
		return JSONObject.parseObject(conn.url(createFolderUrl).requestBody("parentFolderId=" + parentId + "&folderName=" + folderName).execute().body());
	}

	/**
	 * 根据配置删除多个文件或文件夹到指定文件夹
	 *
	 * @param filesInfo 指定的多个文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int delete(@NotNull List<JSONObject> filesInfo) {
		return batchTask("DELETE", filesInfo, "");
	}

	/**
	 * 删除单个个文件或文件夹到指定文件夹
	 *
	 * @param fileInfo 文指定的文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int delete(@NotNull JSONObject fileInfo) {
		return batchTask("DELETE", fileInfo, "");
	}

	/**
	 * 根据配置复制多个文件或文件夹到指定文件夹
	 *
	 * @param filesInfo 指定的多个文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @param folderId  目标文件夹ID
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int copy(@NotNull List<JSONObject> filesInfo, @NotNull String folderId) {
		return batchTask("COPY", filesInfo, folderId);
	}

	/**
	 * 复制单个文件或文件夹到指定文件夹
	 *
	 * @param fileInfo 指定的文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @param folderId 目标文件夹ID
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int copy(@NotNull JSONObject fileInfo, @NotNull String folderId) {
		return batchTask("COPY", fileInfo, folderId);
	}

	/**
	 * 根据配置移动多个文件或文件夹到指定文件夹
	 *
	 * @param filesInfo 指定的多个文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @param folderId  目标文件夹ID
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int move(@NotNull List<JSONObject> filesInfo, @NotNull String folderId) {
		return batchTask("MOVE", filesInfo, folderId);
	}

	/**
	 * 移动单个文件或文件夹到指定文件夹
	 *
	 * @param fileInfo 指定的文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @param folderId 目标文件夹ID
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int move(@NotNull JSONObject fileInfo, @NotNull String folderId) {
		return batchTask("MOVE", fileInfo, folderId);
	}

	/**
	 * 对多个文件或文件夹执行批处理脚本操作,用于文件移动,删除,复制等
	 *
	 * @param type      操作类型
	 * @param filesInfo 指定的多个文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int batchTask(@NotNull String type, @NotNull List<JSONObject> filesInfo, @NotNull String folderId) {
		JSONArray taskInfos = new JSONArray();
		for (JSONObject fileInfo : filesInfo) {
			JSONObject taskInfo = new JSONObject();
			taskInfo.put("fileName", fileInfo.getString("name"));
			taskInfo.put("fileId", fileInfo.getString("id"));
			taskInfos.add(taskInfo);
		}
		return batchTask(type, taskInfos.toString(), folderId);
	}

	/**
	 * 对单个文件或文件夹执行脚本操作,用于文件移动,删除,复制等
	 *
	 * @param type     操作类型
	 * @param fileInfo 指定的文件或文件夹,JSON类型数据,需包含"name"和"id"选项
	 * @param folderId 目标文件夹ID,注意如果当前操作(如删除)没有关联文件夹,指定空字符串
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int batchTask(@NotNull String type, @NotNull JSONObject fileInfo, @NotNull String folderId) {
		JSONObject taskInfo = new JSONObject();
		taskInfo.put("fileName", fileInfo.getString("name"));
		taskInfo.put("fileId", fileInfo.getString("id"));
		return batchTask(type, new JSONArray().fluentAdd(taskInfo).toString(), folderId);
	}

	/**
	 * 执行脚本操作,用于文件移动,删除,复制等
	 *
	 * @param type      操作类型
	 * @param taskInfos 自定义待执行的Json数据(json数组,每个元素应包含fileName和fileId选项)
	 * @param folderId  目标文件夹ID,注意如果当前操作(如删除)没有关联文件夹,指定空字符串
	 * @return 操作返回的结果状态码, 一般情况下, 0表示成功
	 */
	@Contract(pure = true)
	public int batchTask(@NotNull String type, @NotNull String taskInfos, @NotNull String folderId) {
		Map<String, String> data = new HashMap<>();
		data.put("type", type);
		data.put("taskInfos", taskInfos);
		data.put("targetFolderId", folderId);
		return JSONObject.parseObject(conn.url(createBatchTaskUrl).data(data).execute().body()).getInteger("res_code");
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息
	 *
	 * @param folderId 文件夹ID
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId) {
		Map<String, String> data = new HashMap<>();
		data.put("pageSize", "1");
		data.put("folderId", folderId);
		String urlInfo = conn.url(listFilesUrl).data(data).get().text();
		data.put("pageSize", JSONObject.parseObject(urlInfo).getJSONObject("fileListAO").getString("count"));
		urlInfo = conn.url(listFilesUrl).data(data).get().text();
		JSONObject fileListAO = JSONObject.parseObject(urlInfo).getJSONObject("fileListAO");
		JSONArray filesList = new JSONArray();
		filesList.addAll(fileListAO.getJSONArray("fileList"));
		filesList.addAll(fileListAO.getJSONArray("folderList"));
		return filesList.toList(JSONObject.class);
	}

	/**
	 * 获取用户主页的指定文件夹下的文件(仅文件夹)信息
	 *
	 * @param folderId 文件夹ID
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	@Contract(pure = true)
	public List<JSONObject> getFileInfosAsHomeOfFolder(@NotNull String folderId) {
		return getInfosAsHomeOfFolder(folderId).stream().filter(l -> !l.containsKey("fileCount")).collect(Collectors.toList());
	}

	/**
	 * 获取用户主页的指定文件夹下的文件(仅文件)信息
	 *
	 * @param folderId 文件夹ID
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	@Contract(pure = true)
	public List<JSONObject> getFolderInfosAsHomeOfFolder(@NotNull String folderId) {
		return getInfosAsHomeOfFolder(folderId).stream().filter(l -> l.containsKey("fileCount")).collect(Collectors.toList());
	}

	/**
	 * 获取用户主页的所有文件信息
	 *
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHome() {
		return getInfosAsHomeOfFolder("-11"); // -11 主页
	}

	/**
	 * 获取指定文件夹下(包含子文件夹)所有文件信息,[-11]为根目录
	 *
	 * @param folderId 文件夹ID
	 * @return List - JSON类型数据,包含了文件的所有信息
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHomeOfAll(@NotNull String folderId) {
		List<JSONObject> filesInfo = new ArrayList<>();
		for (JSONObject fileInfo : getInfosAsHomeOfFolder(folderId)) {
			if (fileInfo.containsKey("fileCount")) {
				filesInfo.addAll(Judge.isEmpty(fileInfo.getInteger("fileCount")) ? new ArrayList<>() : getInfosAsHomeOfFolder(fileInfo.getString("id")));
			} else {
				filesInfo.add(fileInfo);
			}
		}
		return filesInfo;
	}

	/**
	 * 在需要提取码但自己没有时,可直接获得文件直链<br/>
	 * 如果有多个文件,返回第一个文件直链
	 *
	 * @param url 天翼URL
	 * @return 文件直链
	 */
	@Contract(pure = true)
	public String getStraightAsNotCode(@NotNull String url) {
		JSONObject info = getshareUrlInfo(url);
		String straight = JSONObject.parseObject(conn.url(fileDownloadUrl + "?dt=1&fileId=" + info.getString("fileId") + "&shareId=" + info.getString("shareId")).get().text()).getString("fileDownloadUrl");
		return straight == null ? "" : straight;
	}

	/**
	 * 获得分享页面所有文件的直链(无密码)
	 *
	 * @param url 天翼URL
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	@Contract(pure = true)
	public Map<String, String> getStraightsAsPage(@NotNull String url) {
		return getStraightsAsPage(url, "");
	}

	/**
	 * 获得分享页面所有文件的直链
	 *
	 * @param url        天翼URL
	 * @param accessCode 提取码
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	@Contract(pure = true)
	public Map<String, String> getStraightsAsPage(@NotNull String url, @NotNull String accessCode) {
		Map<String, String> fileUrls = new HashMap<>();
		for (JSONObject fileInfo : getInfoAsPage(url, accessCode)) {
			Map<String, String> params = new HashMap<>();
			params.put("dt", "1");
			params.put("fileId", fileInfo.getString("id"));
			params.put("shareId", fileInfo.getString("shareId"));
			String straight = JSONObject.parseObject(conn.url(fileDownloadUrl).data(params).get().text()).getString("fileDownloadUrl");
			fileUrls.put(fileInfo.getString("name"), straight == null ? "" : straight);
		}
		return fileUrls;
	}

	/**
	 * 通过文件ID获取文件的直链,不能用于分享页面获取
	 *
	 * @param fileId 文件ID
	 * @return 用于下载的直链
	 */
	@Contract(pure = true)
	public String getStraight(@NotNull String fileId) {
		return JSONObject.parseObject(conn.url(fileDownloadUrl).data("fileId", fileId).get().text()).getString("fileDownloadUrl");
	}

	/**
	 * 天翼云盘的登陆操作
	 */
	public static class YunPanLogin {

		private static final String loginSubmitUrl = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";
		private static final String encryptConfUrl = "https://open.e.189.cn/api/logbox/config/encryptConf.do";

		/**
		 * 通过账号密码登录获得用户cookies
		 * <p>
		 * js逆向方法,直接通过api获取登陆cookie
		 *
		 * @param userName 用户名
		 * @param password 密码
		 * @return 此链接, 用于API操作
		 */
		@Contract(pure = true)
		public static Map<String, String> login(@NotNull String userName, @NotNull String password) {
			Connection conn = HttpsUtil.newSession();
			JSONObject encryptConfData = JSONObject.parseObject(conn.url(encryptConfUrl).get().text()).getJSONObject("data");
			String pre = encryptConfData.getString("pre");
			String pubKey = encryptConfData.getString("pubKey");
			userName = pre + encrypt(userName, pubKey);
			password = pre + encrypt(password, pubKey);
			Document doc = conn.url(loginUrl).get();
			String loginUrlText = doc.select("script[type='text/javascript']").toString();
			String captcha_token = doc.selectFirst("input[name='captchaToken']").attr("value");
			String appKey = StringUtil.extract(loginUrlText, "appKey =.*,");
			appKey = appKey.substring(appKey.indexOf("'") + 1, appKey.lastIndexOf("'"));
			String accountType = StringUtil.extract(loginUrlText, "accountType =.*,");
			accountType = accountType.substring(accountType.indexOf("'") + 1, accountType.lastIndexOf("'"));
			String clientType = StringUtil.extract(loginUrlText, "clientType =.*,");
			clientType = clientType.substring(clientType.indexOf("'") + 1, clientType.lastIndexOf("'"));
			String returnUrl = StringUtil.extract(loginUrlText, "returnUrl =.*,");
			returnUrl = returnUrl.substring(returnUrl.indexOf("'") + 1, returnUrl.lastIndexOf("'"));
			String mailSuffix = StringUtil.extract(loginUrlText, "mailSuffix =.*;");
			mailSuffix = mailSuffix.substring(mailSuffix.indexOf("'") + 1, mailSuffix.lastIndexOf("'"));
			String isOauth2 = StringUtil.extract(loginUrlText, "isOauth2 =.*;");
			isOauth2 = isOauth2.substring(isOauth2.indexOf("\"") + 1, isOauth2.lastIndexOf("\""));
			String lt = StringUtil.extract(loginUrlText, "lt =.*;");
			lt = lt.substring(lt.indexOf("\"") + 1, lt.lastIndexOf("\""));
			String reqId = StringUtil.extract(loginUrlText, "reqId =.*;");
			reqId = reqId.substring(reqId.indexOf("\"") + 1, reqId.lastIndexOf("\""));
			String paramId = StringUtil.extract(loginUrlText, "paramId =.*;");
			paramId = paramId.substring(paramId.indexOf("\"") + 1, paramId.lastIndexOf("\""));

			Map<String, String> data = new HashMap<>();
			data.put("appKey", appKey);
			data.put("accountType", accountType);
			data.put("userName", userName);
			data.put("password", password);
			data.put("captchaToken", captcha_token);
			data.put("returnUrl", returnUrl);
			data.put("mailSuffix", mailSuffix);
			data.put("dynamicCheck", paramId);
			data.put("clientType", clientType);
			data.put("isOauth2", isOauth2);
			data.put("paramId", paramId);
			Response res = conn.url(loginSubmitUrl).header("lt", lt).header("reqid", reqId).header("referer", encryptConfUrl).data(data).method(Method.POST).execute();
			JSONObject info = JSONObject.parseObject(res.body());
			if (!info.getString("result").equals("0")) {
				throw new YunPanException(info.getString("msg"));
			}
			return HttpsUtil.connect(info.getString("toUrl")).execute().cookies();
		}

		private static String encrypt(String data, String pubKey) {
			try {
				return b64tohex(publicKeyEncrypt(data, pubKey));
			} catch (Exception e) {
				return "";
			}
		}

		private static String publicKeyEncrypt(String str, String publicKey) throws Exception {
			byte[] decoded = Base64.decodeBase64(publicKey); //base64编码的公钥
			RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
			Cipher cipher = Cipher.getInstance("RSA"); //RSA加密
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			return Base64.encodeBase64String(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
		}

		private static String b64tohex(String data) {
			char[] a = data.toCharArray();
			IntFunction<Character> intTochar = i -> "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray()[i];
			String b64map = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
			StringBuilder d = new StringBuilder();
			int e = 0, c = 0;
			for (char value : a) {
				if (value != '=') {
					int v = b64map.indexOf(value);
					switch (e) {
						case 0 -> {
							e = 1;
							d.append(intTochar.apply(v >> 2));
							c = 3 & v;
						}
						case 1 -> {
							e = 2;
							d.append(intTochar.apply(c << 2 | v >> 4));
							c = 15 & v;
						}
						case 2 -> {
							e = 3;
							d.append(intTochar.apply(c));
							d.append(intTochar.apply(v >> 2));
							c = 3 & v;
						}
						default -> {
							e = 0;
							d.append(intTochar.apply(c << 2 | v >> 4));
							d.append(intTochar.apply(15 & v));
						}
					}
				}
			}
			return e == 1 ? String.valueOf(d) + intTochar.apply(c << 2) : String.valueOf(d);
		}

	}

}
