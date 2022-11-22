package org.haic.often.netdisc;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.haic.often.Symbol;
import org.haic.often.chrome.browser.LocalCookie;
import org.haic.often.exception.YunPanException;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.util.FileUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 夸克云盘API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/9/13 23:59
 */
public class KuaKeYunPan {

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
	private static final String preUrl = "https://drive.quark.cn/1/clouddrive/file/upload/pre?pr=ucpro&fr=pc";
	private static final String authUrl = "https://drive.quark.cn/1/clouddrive/file/upload/auth?pr=ucpro&fr=pc";
	private static final String hashUrl = "https://drive.quark.cn/1/clouddrive/file/update/hash?pr=ucpro&fr=pc";

	private final Connection conn = HttpsUtil.newSession();

	private KuaKeYunPan(Map<String, String> cookies) {
		conn.cookies(cookies);
		JSONObject loginInfo = JSONObject.parseObject(conn.url(flushUrl).get().text());
		if (!URIUtil.statusIsOK(loginInfo.getInteger("status"))) {
			throw new YunPanException(loginInfo.getString("message"));
		}
	}

	/**
	 * 使用本地谷歌浏览器(Edge)登陆,进行需要身份验证的API操作
	 *
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static KuaKeYunPan localLogin() {
		return login(LocalCookie.home().getForDomain("quark.cn"));
	}

	/**
	 * 使用本地谷歌浏览器登陆,进行需要身份验证的API操作
	 *
	 * @param userHome 本地谷歌浏览器用户数据目录(User Data)
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static KuaKeYunPan localLogin(@NotNull String userHome) {
		return login(LocalCookie.home(userHome).getForDomain("quark.cn"));
	}

	/**
	 * 登陆云盘,进行需要身份验证的API操作
	 *
	 * @param cookies cookies
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public List<JSONObject> getInfosOfExt(@NotNull String ext) {
		return JSONArray.parseArray(JSONObject.parseObject(conn.url(categoryUrl + ext).get().text()).getJSONObject("data").getString("list")).toList(JSONObject.class);
	}

	/**
	 * 获取分享文件列表
	 *
	 * @return 分享文件列表
	 */
	@Contract(pure = true)
	public List<JSONObject> listShares() {
		return JSONArray.parseArray(JSONObject.parseObject(conn.url(detailUrl).get().text()).getJSONObject("data").getString("list")).toList(JSONObject.class);
	}

	/**
	 * 删除指定的分享
	 *
	 * @param shareId 分享ID
	 * @return 删除状态
	 */
	@Contract(pure = true)
	public boolean unShare(@NotNull String... shareId) {
		return unShare(Arrays.asList(shareId));
	}

	/**
	 * 删除指定的分享
	 *
	 * @param shareIds 分享ID
	 * @return 删除状态
	 */
	@Contract(pure = true)
	public boolean unShare(@NotNull List<String> shareIds) {
		return URIUtil.statusIsOK(JSONObject.parseObject(conn.url(shareDeleteUrl).requestBody(new JSONObject().fluentPut("share_ids", shareIds).toString()).post().text()).getInteger("status"));
	}

	/**
	 * 分享指定文件
	 *
	 * @param shareCode 分享密码
	 * @param fid       文件ID
	 * @return 含有分享链接等JSON格式信息
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public JSONObject share(@NotNull String shareCode, @NotNull List<String> fids) {
		JSONObject data = new JSONObject();
		data.put("expired_type", 2);
		data.put("fid_list", fids);
		data.put("passcode", shareCode);
		data.put("url_type", shareCode.isEmpty() ? 1 : 2);
		JSONObject shareInfo = JSONObject.parseObject(conn.url(shareUrl).requestBody(data.toString()).post().text()).getJSONObject("data");
		String taskId = shareInfo.getString("task_id");
		if (shareInfo.getBoolean("task_sync")) {
			JSONObject taskResp = shareInfo.getJSONObject("task_resp");
			JSONObject result = new JSONObject();
			result.put("status", taskResp.getInteger("status"));
			result.put("message", taskResp.getString("message"));
			return result.fluentPut("task_id", taskId);
		}
		String shareId = null;
		while (shareId == null) {
			JSONObject taskInfo = JSONObject.parseObject(conn.url(taskUrl + taskId).get().text()).getJSONObject("data");
			shareId = taskInfo.getInteger("status") == 2 ? taskInfo.getString("share_id") : null;
		}
		return JSONObject.parseObject(conn.url(passwordUrl).requestBody(new JSONObject().fluentPut("share_id", shareId).toString()).post().text()).getJSONObject("data").fluentPut("share_id", shareId);
	}

	/**
	 * 删除指定文件
	 *
	 * @param fid 文件ID
	 * @return 删除状态
	 */
	@Contract(pure = true)
	public boolean delete(@NotNull String... fid) {
		return delete(Arrays.asList(fid));
	}

	/**
	 * 删除指定文件
	 *
	 * @param fids 文件ID
	 * @return 删除状态
	 */
	@Contract(pure = true)
	public boolean delete(@NotNull List<String> fids) {
		JSONObject data = new JSONObject();
		data.put("action_type", 2);
		data.put("exclude_fids", new ArrayList<>());
		data.put("filelist", fids);
		return URIUtil.statusIsOK(JSONObject.parseObject(conn.url(deleteUrl).requestBody(data.toString()).post().text()).getInteger("status"));
	}

	/**
	 * 重命名文件或文件夹
	 *
	 * @param fid  文件ID
	 * @param name 新的名称
	 * @return 重命名状态
	 */
	@Contract(pure = true)
	public boolean rename(@NotNull String fid, @NotNull String name) {
		JSONObject data = new JSONObject();
		data.put("fid", fid);
		data.put("file_name", name);
		return URIUtil.statusIsOK(JSONObject.parseObject(conn.url(renameUrl).requestBody(data.toString()).post().text()).getInteger("status"));
	}

	/**
	 * 移动文件
	 *
	 * @param tofid 移动至指定目录
	 * @param fid   待移动的文件ID
	 * @return 移动状态
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public boolean move(@NotNull String tofid, @NotNull List<String> fids) {
		JSONObject data = new JSONObject();
		data.put("action_type", 1);
		data.put("exclude_fids", new ArrayList<>());
		data.put("filelist", fids);
		data.put("to_pdir_fid", tofid);
		return URIUtil.statusIsOK(JSONObject.parseObject(conn.url(moveUrl).requestBody(data.toString()).post().text()).getInteger("status"));
	}

	/**
	 * 创建文件夹
	 *
	 * @param parentId 父文件夹ID,根目录为"0"
	 * @param name     文件夹名称
	 * @return 包含文件夹ID等JSON格式信息
	 */
	@Contract(pure = true)
	public JSONObject createFolder(@NotNull String parentId, @NotNull String name) {
		JSONObject data = new JSONObject();
		data.put("dir_init_lock", false);
		data.put("dir_path", "");
		data.put("file_name", name);
		data.put("pdir_fid", "0");
		return JSONObject.parseObject(conn.url(fileUrl).requestBody(data.toString()).post().text());
	}

	/**
	 * 获取用户主页的文件信息
	 *
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHome() {
		return getInfosAsHomeOfFolder("0");
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息
	 *
	 * @param folderId 文件夹ID,根目录为0
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId) {
		return JSONArray.parseArray(JSONObject.parseObject(conn.url(sortUrl + folderId).get().text()).getJSONObject("data").getString("list")).toList(JSONObject.class);
	}

	/**
	 * 上传文件(大文件可能不会成功)
	 *
	 * @param src 待上传的文件路径
	 * @param fid 存放目录ID
	 * @return 上传状态
	 */
	@Contract(pure = true)
	public boolean upload(@NotNull String src, @NotNull String fid) {
		return upload(new File(src), fid);
	}

	/**
	 * 上传文件,方法暂时无效等待修复,请勿使用
	 *
	 * @param file 待上传的文件
	 * @param fid  存放目录ID
	 * @return 上传状态
	 */
	@Contract(pure = true)
	public boolean upload(@NotNull File file, @NotNull String fid) {
		try (FileInputStream in = new FileInputStream(file)) {
			String mimiType = URLConnection.guessContentTypeFromName(file.getName());
			SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			String date = format.format(new Date());
			JSONObject preData = new JSONObject();
			preData.put("ccp_hash_update", true);
			preData.put("parallel_upload", true);
			preData.put("pdir_fid", fid);
			preData.put("dir_name", "");
			preData.put("size", file.length());
			preData.put("file_name", file.getName());
			preData.put("format_type", mimiType);
			JSONObject preInfo = JSONObject.parseObject(conn.url(preUrl).requestBody(preData.toString()).post().text()).getJSONObject("data");
			String authInfo = preInfo.getString("auth_info");
			String taskId = preInfo.getString("task_id");
			String bucket = preInfo.getString("bucket");
			String key = preInfo.getString("obj_key");
			String uploadId = preInfo.getString("upload_id");
			String userAgent = "aliyun-sdk-js/1.0.0 Microsoft Edge 107.0.1418.52 on Windows 10 64-bit";
			JSONObject authData = new JSONObject();
			authData.put("auth_info", authInfo);
			authData.put("task_id", taskId);
			authData.put("auth_meta", "PUT\n\n" + mimiType + "\n" + date + "\nx-oss-date:" + date + "\nx-oss-user-agent:" + userAgent + "\n" + bucket + Symbol.SLASH + key + "?partNumber=1&uploadId=" + uploadId);
			String auth = JSONObject.parseObject(conn.url(authUrl).requestBody(authData.toString()).post().text()).getJSONObject("data").getString("auth_key");
			String uploadUrl = "https://" + bucket + ".oss-cn-zhangjiakou.aliyuncs.com/" + key + "?uploadId=" + uploadId + "&partNumber=1";
			// 上传文件
			Map<String, String> headers = new HashMap<>();
			headers.put("x-oss-date", date);
			headers.put("x-oss-user-agent", userAgent);
			conn.url(uploadUrl).data(in, mimiType).headers(headers).auth(auth).method(Method.PUT).execute();
			// 获取上传结果
			JSONObject hashData = new JSONObject();
			hashData.put("md5", FileUtil.getMD5(file));
			hashData.put("sha1", FileUtil.getSHA1(file));
			hashData.put("task_id", taskId);
			return JSONObject.parseObject(conn.newRequest().url(hashUrl).requestBody(hashData.toString()).post().text()).getJSONObject("data").getBoolean("finish");
		} catch (IOException e) {
			return false;
		}
	}

}
