package org.haic.often.netdisc;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.haic.often.Symbol;
import org.haic.often.chrome.browser.LocalCookie;
import org.haic.often.exception.YunPanException;
import org.haic.often.net.Method;
import org.haic.often.net.analyze.nodes.Document;
import org.haic.often.net.analyze.nodes.Element;
import org.haic.often.net.analyze.select.Elements;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 蓝奏云盘API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/18 23:59
 */
public class LanZouYunPan {

	private static final String domain = "https://www.lanzoui.com/";
	private static final String ajaxmUrl = "https://www.lanzoui.com/ajaxm.php";
	private static final String douploadUrl = "https://pc.woozooo.com/doupload.php";
	private static final String mydiskUrl = "https://pc.woozooo.com/mydisk.php";

	private final Connection conn = HttpsUtil.newSession();

	private LanZouYunPan(Map<String, String> cookies) {
		conn.cookies(cookies);
		if (conn.url(mydiskUrl).get().selectFirst("div[class='mydisk_bar']") == null) {
			throw new YunPanException("登陆信息无效,请检查cookies是否正确");
		}
	}

	/**
	 * 获取分享页面的文件列表的所有文件信息<br/>
	 * 文件列表页面
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @param passwd    访问密码
	 * @return Map - 文件名, 文件ID链接
	 */
	@Contract(pure = true)
	public static Map<String, String> getInfosAsPage(@NotNull String lanzouUrl, @NotNull String passwd) {
		String infos = Objects.requireNonNull(HttpsUtil.connect(lanzouUrl).get().selectFirst("body script")).toString();
		infos = infos.substring(32, infos.indexOf("json") - 20).replaceAll("\t*　* *'*;*", "");

		// 获取post参数
		Map<String, String> params = new HashMap<>();
		for (String data : StringUtil.extract(infos.replaceAll("\n", ""), "data.*").substring(6).split(",")) {
			String[] entry = data.split(Symbol.COLON);
			params.put(entry[0], entry[1]);
		}

		// 获取修正后的参数
		String pgs = StringUtil.extract(infos, "pgs=.*");
		pgs = pgs.substring(pgs.indexOf("=") + 1);
		String t = StringUtil.extract(infos, params.get("t") + "=.*");
		t = t.substring(t.indexOf("=") + 1);
		String k = StringUtil.extract(infos, params.get("k") + "=.*");
		k = k.substring(k.indexOf("=") + 1);

		// 修正post参数
		params.put("pg", pgs);
		params.put("t", t);
		params.put("k", k);
		params.put("pwd", passwd);

		Map<String, String> result = new HashMap<>();
		JSONObject jsonInfos = JSONObject.parseObject(HttpsUtil.connect(domain + "filemoreajax.php").data(params).post().text());
		if (jsonInfos.getInteger("zt") == 1) { // 处理json数据
			JSONArray jsonArray = jsonInfos.getJSONArray("text");
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject info = jsonArray.getJSONObject(i);
				result.put(info.getString("name_all"), domain + info.getString("id"));
			}
		}

		return result;
	}

	/**
	 * 获取分享页面的文件列表的所有文件信息<br/>
	 * 文件列表页面
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @return Map - 文件名, 文件ID链接
	 */
	@Contract(pure = true)
	public static Map<String, String> getInfosAsPage(@NotNull String lanzouUrl) {
		return getInfosAsPage(lanzouUrl, "");
	}

	/**
	 * 获取分享页面的文件列表的所有文件的直链<br/>
	 * 文件列表页面
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @param passwd    访问密码
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	@Contract(pure = true)
	public static Map<String, String> getStraightsAsPage(@NotNull String lanzouUrl, @NotNull String passwd) {
		return getInfosAsPage(lanzouUrl, passwd).entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, l -> getStraight(l.getValue())));
	}

	/**
	 * 获取分享页面的文件列表的所有文件的直链<br/>
	 * 文件列表页面
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	@Contract(pure = true)
	public static Map<String, String> getStraightsAsPage(@NotNull String lanzouUrl) {
		return getInfosAsPage(lanzouUrl).entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, l -> getStraight(l.getValue())));
	}

	/**
	 * 获取单个文件的直链<br/>
	 * 单个文件分享页面
	 *
	 * @param lanzouUrl 蓝奏云文件链接
	 * @return 蓝奏云URL直链
	 */
	@Contract(pure = true)
	public static String getStraight(@NotNull String lanzouUrl) {
		Document doc = HttpsUtil.connect(lanzouUrl).get();
		String downUrl = domain;
		Element ifr2 = doc.selectFirst("iframe[class='ifr2']");
		if (ifr2 == null) {
			Element downlink = doc.selectFirst("iframe[class='n_downlink']");
			if (downlink == null) {
				return "";
			}
			downUrl += downlink.attr("src");
		} else {
			downUrl += ifr2.attr("src");
		}

		// 提取POST参数信息段
		String infos = Objects.requireNonNull(HttpsUtil.connect(downUrl).get().selectFirst("body script")).toString();
		infos = infos.substring(32, infos.indexOf("json") - 17).replaceAll("\t*　* *'*;*", "");
		infos = infos.lines().filter(l -> !l.startsWith("//")).collect(Collectors.joining("\n")); // 去除注释

		// 获取post参数
		String dataInfo = infos.lines().filter(l -> l.startsWith("data")).findFirst().orElse("");
		Map<String, String> params = new HashMap<>();
		for (String data : dataInfo.substring(6, dataInfo.length() - 1).split(",")) {
			String[] entry = data.split(Symbol.COLON);
			params.put(entry[0], entry[1]);
		}

		// 获取修正后的参数
		String signs = StringUtil.extract(infos, params.get("signs") + "=.*");
		signs = signs.substring(signs.indexOf("=") + 1);
		String websign = StringUtil.extract(infos, params.get("websign") + "=.*");
		websign = websign.substring(websign.indexOf("=") + 1);
		String websignkey = StringUtil.extract(infos, params.get("websignkey") + "=.*");
		websignkey = websignkey.substring(websignkey.indexOf("=") + 1);

		// 修正post参数
		params.put("signs", signs);
		params.put("websign", websign);
		params.put("websignkey", websignkey);

		// 处理json数据
		JSONObject fileInfo = JSONObject.parseObject(HttpsUtil.connect(ajaxmUrl).referrer(downUrl).data(params).post().text());
		return HttpsUtil.connect(fileInfo.getString("dom") + "/file/" + fileInfo.getString("url")).execute().url();
	}

	/**
	 * 获取单个文件的直链<br/>
	 * 单个文件分享页面
	 *
	 * @param lanzouUrl 蓝奏云文件链接
	 * @param password  提取码
	 * @return 蓝奏云URL直链
	 */
	@Contract(pure = true)
	public static String getStraight(@NotNull String lanzouUrl, @NotNull String password) {
		JSONObject fileInfo = JSONObject.parseObject(HttpsUtil.connect(ajaxmUrl).requestBody(StringUtil.extract(HttpsUtil.connect(lanzouUrl).execute().body(), "action=.*&p=") + password).referrer(lanzouUrl).post().text());
		String suffix = fileInfo.getString("url");
		return suffix.equals("0") ? "" : HttpsUtil.connect(fileInfo.getString("dom") + "/file/" + suffix).execute().url();
	}

	/**
	 * 使用本地谷歌浏览器(Edge)登陆,进行需要身份验证的API操作
	 *
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static LanZouYunPan localLogin() {
		return login(LocalCookie.home().getForDomain("pc.woozooo.com"));
	}

	/**
	 * 使用本地谷歌浏览器登陆,进行需要身份验证的API操作
	 *
	 * @param userHome 本地谷歌浏览器用户数据目录(User Data)
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static LanZouYunPan localLogin(@NotNull String userHome) {
		return login(LocalCookie.home(userHome).getForDomain("pc.woozooo.com"));
	}

	/**
	 * 登陆云盘,进行需要身份验证的API操作
	 *
	 * @param cookies cookies
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static LanZouYunPan login(@NotNull Map<String, String> cookies) {
		return new LanZouYunPan(cookies);
	}

	/**
	 * 获取回收站的文件列表
	 *
	 * @return JSON类型数据, 包含了所有文件的信息
	 */
	@Contract(pure = true)
	public List<JSONObject> listRecycleBin() {
		Elements lists = conn.url(mydiskUrl).requestBody("item=recycle&action=files").get().select("tbody tr[class]");
		return lists.size() == 1 && lists.text().contains("回收站为空") ? new ArrayList<>() : lists.stream().map(l -> {
			Element input = l.selectFirst("input");
			assert input != null;
			JSONObject info = new JSONObject();
			info.put("fileName", Objects.requireNonNull(l.selectFirst("a")).text());
			info.put("inputName", input.attr("name"));
			info.put("inputValue", input.attr("value"));
			return info;
		}).collect(Collectors.toList());
	}

	/**
	 * 还原多个回收站的文件或文件夹
	 *
	 * @param fileInfo JSON格式配置参数,包含fileName,inputName,inputValue,可指定多个
	 * @return 操作返回的结果状态码, 200为成功
	 */
	@Contract(pure = true)
	public int restore(@NotNull JSONObject... fileInfo) {
		return restore(Arrays.asList(fileInfo));
	}

	/**
	 * 还原多个回收站的文件或文件夹
	 *
	 * @param fileInfoList JSON格式配置参数数组,包含fileName,inputName,inputValue
	 * @return 操作返回的结果状态码, 200为成功
	 */
	@Contract(pure = true)
	public int restore(@NotNull List<JSONObject> fileInfoList) {
		return conn.url(mydiskUrl).requestBody("item=recycle&task=restore_recycle&action=files&formhash=a1c01e43&" + fileInfoList.stream().map(l -> l.getString("inputName") + "=" + l.getString("inputValue")).collect(Collectors.joining("&"))).method(Method.POST).execute().statusCode();
	}

	/**
	 * 还原回收站的所有文件
	 *
	 * @return 操作返回的结果状态码, 200为成功
	 */
	@Contract(pure = true)
	public int restoreAll() {
		return conn.url(mydiskUrl).requestBody("item=recycle&task=restore_all&action=restore_all&formhash=a1c01e43&").method(Method.POST).execute().statusCode();
	}

	/**
	 * 还原回收站的所有文件
	 *
	 * @return 操作返回的结果状态码, 200为成功
	 */
	@Contract(pure = true)
	public int clearRecycle() {
		return conn.url(mydiskUrl).requestBody("item=recycle&task=delete_all&action=delete_all&formhash=a1c01e43&").method(Method.POST).execute().statusCode();
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileInfo JSON格式配置参数,包含fileName,inputName,inputValue,可指定多个
	 * @return 操作返回的结果状态码, 200为成功
	 */
	@Contract(pure = true)
	public int clearRecycle(@NotNull JSONObject... fileInfo) {
		return clearRecycle(Arrays.asList(fileInfo));
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileInfoList JSON格式配置参数数组,包含fileName,inputName,inputValue
	 * @return 操作返回的结果状态码, 200为成功
	 */
	@Contract(pure = true)
	public int clearRecycle(@NotNull List<JSONObject> fileInfoList) {
		return conn.url(mydiskUrl).requestBody("item=recycle&task=delete_complete_recycle&action=files&formhash=a1c01e43&" + fileInfoList.stream().map(l -> l.getString("inputName") + "=" + l.getString("inputValue")).collect(Collectors.joining("&"))).method(Method.POST).execute().statusCode();
	}

	/**
	 * 更改文件夹的分享密码
	 *
	 * @param folderId  文件夹ID
	 * @param shareCode 修改后的分享密码
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject alterFolderOfShareCode(@NotNull String folderId, @NotNull String shareCode) {
		return doupload("task=16&folder_id=" + folderId + "&shows=1&shownames=" + shareCode);
	}

	/**
	 * 关闭文件夹的分享密码,注意该功能仅限会员使用
	 *
	 * @param folderId 文件夹ID
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject closeFolderOfShareCode(@NotNull String folderId) {
		return doupload("task=23&folder_id=" + folderId + "&shows=0&shownames=");
	}

	/**
	 * 更改文件夹的资料,名称和说明
	 *
	 * @param folderId    文件夹ID
	 * @param folderName  修改后的文件夹名称
	 * @param description 修改后的说明
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject alterFolderOfDescription(@NotNull String folderId, @NotNull String folderName, @NotNull String description) {
		return doupload("task=4&folder_id=" + folderId + "&folder_name=" + folderName + "&folder_description=" + description);
	}

	/**
	 * 更改文件的分享密码,也可用于打开文件的分享密码
	 *
	 * @param fileId    文件ID
	 * @param shareCode 分享密码
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject alterFileOfShareCode(@NotNull String fileId, @NotNull String shareCode) {
		return doupload("task=23&file_id=" + fileId + "&shows=1&shownames=" + shareCode);
	}

	/**
	 * 关闭文件的分享密码,注意该功能仅限会员使用
	 *
	 * @param fileId 文件ID
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject closeFileOfShareCode(@NotNull String fileId) {
		return doupload("task=23&file_id=" + fileId + "&shows=0&shownames=");
	}

	/**
	 * 更改文件的名称(重命名),注意该功能仅限会员使用
	 *
	 * @param fileId   文件ID
	 * @param fileName 修改后的文件名
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject rename(@NotNull String fileId, @NotNull String fileName) {
		return doupload("task=46&file_id=" + fileId + "&typr=2&file_name=" + fileName);
	}

	/**
	 * 删除文件夹
	 *
	 * @param folderId 待删除的文件夹ID
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject deleteFolder(@NotNull String folderId) {
		return doupload("task=3&folder_id=" + folderId);
	}

	/**
	 * 删除文件
	 *
	 * @param fileId 待删除的文件ID
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject deleteFile(@NotNull String fileId) {
		return doupload("task=6&file_id=" + fileId);
	}

	/**
	 * 移动文件
	 *
	 * @param parentId 父目录ID,根目录为-1
	 * @param fileId   待移动的文件ID
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject move(@NotNull String parentId, @NotNull String fileId) {
		return doupload("task=20&folder_id=" + parentId + "&file_id=" + fileId);
	}

	/**
	 * 在指定目录下创建文件夹
	 *
	 * @param parentId   父目录ID,根目录为0
	 * @param folderName 文件夹名称
	 * @return 返回的JSON数据, text项为创建的文件夹ID
	 */
	@Contract(pure = true)
	public JSONObject createFolder(@NotNull String parentId, @NotNull String folderName) {
		return createFolder(parentId, folderName, "");
	}

	/**
	 * 在指定目录下创建文件夹
	 *
	 * @param parentId    父目录ID,根目录为0
	 * @param folderName  文件夹名称
	 * @param description 文件夹说明
	 * @return 返回的JSON数据, text项为创建的文件夹ID
	 */
	@Contract(pure = true)
	public JSONObject createFolder(@NotNull String parentId, @NotNull String folderName, @NotNull String description) {
		return doupload("task=2&parent_id=" + parentId + "&folder_name=" + folderName + "&folder_description" + description);
	}

	/**
	 * 获取指定文件夹的信息
	 *
	 * @param folderId 文件夹ID
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject getFolderInfo(@NotNull String folderId) {
		return doupload("task=18&folder_id=" + folderId);
	}

	/**
	 * 获取指定文件的信息
	 *
	 * @param fileId 文件ID
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject getFileInfo(@NotNull String fileId) {
		return doupload("task=22&file_id=" + fileId);
	}

	/**
	 * 获取用户主页的文件信息
	 *
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHome() {
		return getInfosAsHomeOfFolder("-1");
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息
	 *
	 * @param folderId 文件夹ID,根目录为-1
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId) {
		List<JSONObject> infos = getFolderInfosAsHomeOfFolder(folderId);
		infos.addAll(getFileInfosAsHomeOfFolder(folderId));
		return infos;
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息(仅文件)
	 *
	 * @param folderId 文件夹ID,根目录为-1
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getFileInfosAsHomeOfFolder(@NotNull String folderId) {
		return doupload("task=5&pg=1&folder_id=" + folderId).getJSONArray("text").toList(JSONObject.class);
	}

	/**
	 * 获取用户主页的指定文件夹下的文件夹信息(仅文件夹)
	 *
	 * @param folderId 文件夹ID,根目录为-1
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getFolderInfosAsHomeOfFolder(@NotNull String folderId) {
		return doupload("task=47/&folder_id=" + folderId).getJSONArray("text").toList(JSONObject.class);
	}

	/**
	 * 通过设定的请求参数对公共API进行各类操作
	 *
	 * @param body 请求数据
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	private JSONObject doupload(@NotNull String body) {
		return JSONObject.parseObject(conn.url(douploadUrl).requestBody(body).post().text());
	}

}
