package org.haic.often.netdisc;

import org.haic.often.chrome.browser.LocalCookie;
import org.haic.often.exception.YunPanException;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.parser.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 夸克云盘API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/9/13 23:59
 */
public class KuaKeYunPan {

	private static final String domain = "https://drive.quark.cn";

	private static final String sortUrl = "https://drive.quark.cn/1/clouddrive/file/sort?pr=ucpro&fr=pc&_size=2147483647&pdir_fid=";
	private static final String flushUrl = "https://drive.quark.cn/1/clouddrive/auth/pc/flush?pr=ucpro&fr=pc";
	private static final String fileUrl = "https://drive.quark.cn/1/clouddrive/file?pr=ucpro&fr=pc";
	private static final String renameUrl = "https://drive.quark.cn/1/clouddrive/file/rename?pr=ucpro&fr=pc";
	private static final String deleteUrl = "https://drive.quark.cn/1/clouddrive/file/delete?pr=ucpro&fr=pc";
	private static final String shareDeleteUrl = "https://drive.quark.cn/1/clouddrive/share/delete?pr=ucpro&fr=pc";
	private static final String moveUrl = "https://drive.quark.cn/1/clouddrive/file/move?pr=ucpro&fr=pc";
	private static final String shareUrl = "https://drive.quark.cn/1/clouddrive/share?pr=ucpro&fr=pc";
	private static final String taskUrl = "https://drive.quark.cn/1/clouddrive/task?pr=ucpro&fr=pc&retry_index=0&task_id=";
	private static final String passwordUrl = "https://drive.quark.cn/1/clouddrive/share/password?pr=ucpro&fr=pc";
	private static final String detailUrl = "https://drive.quark.cn/1/clouddrive/share/mypage/detail?pr=ucpro&fr=pc&_size=2147483647";
	private static final String categoryUrl = "https://drive.quark.cn/1/clouddrive/file/category?pr=ucpro&fr=pc&_size=10240&cat=";

	private final Connection conn = HttpsUtil.newSession();

	private KuaKeYunPan(Map<String, String> cookies) {
		conn.cookieStore().put(URIUtil.getHost(domain), cookies);
		var loginInfo = conn.url(flushUrl).get().json();
		if (!URIUtil.statusIsOK(loginInfo.getInteger("status"))) {
			throw new YunPanException(loginInfo.getString("message"));
		}
	}

	/**
	 * 使用本地谷歌浏览器(Edge)登陆,进行需要身份验证的API操作
	 *
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static KuaKeYunPan localLogin() {
		return login(LocalCookie.home().getForDomain("quark.cn"));
	}

	/**
	 * 使用本地谷歌浏览器登陆,进行需要身份验证的API操作
	 *
	 * @param userHome 本地谷歌浏览器用户数据目录(User Data)
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static KuaKeYunPan localLogin(@NotNull String userHome) {
		return login(LocalCookie.home(userHome).getForDomain("quark.cn"));
	}

	/**
	 * 登陆云盘,进行需要身份验证的API操作
	 *
	 * @param cookies cookies
	 * @return 此链接, 用于身份验证的API操作
	 */
	public static KuaKeYunPan login(@NotNull Map<String, String> cookies) {
		return new KuaKeYunPan(cookies);
	}

	/**
	 * 获取指定类型的文件信息,限制最多显示前10240个文件
	 *
	 * @param ext 文件类型<br/>
	 *            <blockquote>
	 *            <pre>	bt - BT种子</pre>
	 *            <pre>	document - 文档</pre>
	 *            <pre>	video - 视频</pre>
	 *            <pre>	audio - 音频</pre>
	 *            <pre>	image - 图片</pre>
	 *            </blockquote>
	 * @return 文件信息列表
	 */
	public List<JSONObject> getInfosOfExt(@NotNull String ext) {
		return conn.url(categoryUrl + ext).get().json().getJSONObject("data").getList("list", JSONObject.class);
	}

	/**
	 * 获取分享文件列表
	 *
	 * @return 分享文件列表
	 */
	public List<JSONObject> listShares() {
		return conn.url(detailUrl).get().json().getJSONObject("data").getList("list", JSONObject.class);
	}

	/**
	 * 删除指定的分享
	 *
	 * @param shareId 分享ID
	 * @return 删除状态
	 */
	public boolean unShare(@NotNull String... shareId) {
		return unShare(Arrays.asList(shareId));
	}

	/**
	 * 删除指定的分享
	 *
	 * @param shareIds 分享ID
	 * @return 删除状态
	 */
	public boolean unShare(@NotNull List<String> shareIds) {
		return URIUtil.statusIsOK(conn.url(shareDeleteUrl).requestBody(new JSONObject().fluentPut("share_ids", shareIds).toString()).post().json().getInteger("status"));
	}

	/**
	 * 分享指定文件
	 *
	 * @param shareCode 分享密码
	 * @param fid       文件ID
	 * @return 含有分享链接等JSON格式信息
	 */
	public JSONObject share(@NotNull String shareCode, @NotNull String... fid) {
		return share(shareCode, Arrays.asList(fid));
	}

	/**
	 * 分享指定文件
	 *
	 * @param shareCode 分享密码
	 * @param fids      文件ID
	 * @return 含有分享链接等JSON格式信息
	 */
	public JSONObject share(@NotNull String shareCode, @NotNull List<String> fids) {
		var data = new JSONObject();
		data.put("expired_type", 2);
		data.put("fid_list", fids);
		data.put("passcode", shareCode);
		data.put("url_type", shareCode.isEmpty() ? 1 : 2);
		var shareInfo = conn.url(shareUrl).requestBody(data.toString()).post().json().getJSONObject("data");
		var taskId = shareInfo.getString("task_id");
		if (shareInfo.getBoolean("task_sync")) {
			var taskResp = shareInfo.getJSONObject("task_resp");
			var result = new JSONObject();
			result.put("status", taskResp.getInteger("status"));
			result.put("message", taskResp.getString("message"));
			return result.fluentPut("task_id", taskId);
		}
		String shareId = null;
		while (shareId == null) {
			var taskInfo = conn.url(taskUrl + taskId).get().json().getJSONObject("data");
			shareId = taskInfo.getInteger("status") == 2 ? taskInfo.getString("share_id") : null;
		}
		return conn.url(passwordUrl).requestBody(new JSONObject().fluentPut("share_id", shareId).toString()).post().json().getJSONObject("data").fluentPut("share_id", shareId);
	}

	/**
	 * 删除指定文件
	 *
	 * @param fid 文件ID
	 * @return 删除状态
	 */
	public boolean delete(@NotNull String... fid) {
		return delete(Arrays.asList(fid));
	}

	/**
	 * 删除指定文件
	 *
	 * @param fids 文件ID
	 * @return 删除状态
	 */
	public boolean delete(@NotNull List<String> fids) {
		var data = new JSONObject();
		data.put("action_type", 2);
		data.put("exclude_fids", new ArrayList<>());
		data.put("filelist", fids);
		return URIUtil.statusIsOK(conn.url(deleteUrl).requestBody(data.toString()).post().json().getInteger("status"));
	}

	/**
	 * 重命名文件或文件夹
	 *
	 * @param fid  文件ID
	 * @param name 新的名称
	 * @return 重命名状态
	 */
	public boolean rename(@NotNull String fid, @NotNull String name) {
		var data = new JSONObject();
		data.put("fid", fid);
		data.put("file_name", name);
		return URIUtil.statusIsOK(conn.url(renameUrl).requestBody(data.toString()).post().json().getInteger("status"));
	}

	/**
	 * 移动文件
	 *
	 * @param tofid 移动至指定目录
	 * @param fid   待移动的文件ID
	 * @return 移动状态
	 */
	public boolean move(@NotNull String tofid, @NotNull String... fid) {
		return move(tofid, Arrays.asList(fid));
	}

	/**
	 * 移动文件
	 *
	 * @param tofid 移动至指定目录
	 * @param fids  待移动的文件ID
	 * @return 移动状态
	 */
	public boolean move(@NotNull String tofid, @NotNull List<String> fids) {
		var data = new JSONObject();
		data.put("action_type", 1);
		data.put("exclude_fids", new ArrayList<>());
		data.put("filelist", fids);
		data.put("to_pdir_fid", tofid);
		return URIUtil.statusIsOK(conn.url(moveUrl).requestBody(data.toString()).post().json().getInteger("status"));
	}

	/**
	 * 创建文件夹
	 *
	 * @param parentId 父文件夹ID,根目录为"0"
	 * @param name     文件夹名称
	 * @return 包含文件夹ID等JSON格式信息
	 */
	public JSONObject createFolder(@NotNull String parentId, @NotNull String name) {
		var data = new JSONObject();
		data.put("dir_init_lock", false);
		data.put("dir_path", "");
		data.put("file_name", name);
		data.put("pdir_fid", parentId);
		return conn.url(fileUrl).requestBody(data.toString()).post().json();
	}

	/**
	 * 获取用户主页的文件信息
	 *
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHome() {
		return getInfosAsHomeOfFolder("0");
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息
	 *
	 * @param folderId 文件夹ID,根目录为0
	 * @return 文件信息JSON数组
	 */
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId) {
		return conn.url(sortUrl + folderId).get().json().getJSONObject("data").getList("list", JSONObject.class);
	}

}
