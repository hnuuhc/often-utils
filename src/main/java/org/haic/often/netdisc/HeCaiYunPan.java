package org.haic.often.netdisc;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.haic.often.Judge;
import org.haic.often.chrome.browser.LocalCookie;
import org.haic.often.exception.YunPanException;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.net.http.Response;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.Base64Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 和彩云(中国移动云盘)API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/22 9:50
 */
public class HeCaiYunPan {

	private static final String diskUrl = "https://yun.139.com/orchestration/personalCloud/catalog/v1.0/getDisk";
	private static final String individualContentUrl = "https://yun.139.com/orchestration/personalCloud/content/v1.0/getIndividualContent";
	private static final String createCatalogExtUrl = "https://yun.139.com/orchestration/personalCloud/catalog/v1.0/createCatalogExt";
	private static final String fileSearchUrl = "https://yun.139.com/orchestration/personalCloud/search/v1.0/fileSearch";
	private static final String createBatchOprTaskUrl = "https://yun.139.com/orchestration/personalCloud/batchOprTask/v1.0/createBatchOprTask";
	private static final String updateCatalogInfoUrl = "https://yun.139.com/orchestration/personalCloud/catalog/v1.0/updateCatalogInfo";
	private static final String qryUserExternInfoUrl = "https://yun.139.com/orchestration/personalCloud/user/v1.0/qryUserExternInfo";
	private static final String outLinkUrl = "https://yun.139.com/orchestration/personalCloud/outlink/v1.0/getOutLink";
	private static final String outLinkListUrl = "https://yun.139.com/orchestration/personalCloud/outlink/v1.0/getOutLinkList";
	private static final String delOutLinkUrl = "https://yun.139.com/orchestration/personalCloud/outlink/v1.0/delOutLink";
	private static final String virDirInfoUrl = "https://yun.139.com/orchestration/personalCloud/catalog/v1.0/getVirDirInfo";
	private static final String outlinkInfoUrl = " https://caiyun.139.com/stapi/outlink/info";
	private static final String downloadUrl = "https://caiyun.139.com/stapi/outlink/content/download";
	private static final String downloadRequestUrl = "https://yun.139.com/orchestration/personalCloud/uploadAndDownload/v1.0/downloadRequest";

	private final Connection conn = HttpsUtil.newSession();
	private final JSONObject user = new JSONObject();
	private final List<String> empty = new ArrayList<>();

	private HeCaiYunPan(@NotNull Map<String, String> cookies) {
		conn.cookies(cookies).header("mcloud-channel", "1000101").header("mcloud-client", "10701").header("mcloud-route", "001");
		String userInfo = cookies.get("userInfo");
		if (userInfo == null) {
			throw new YunPanException("登陆信息无效");
		}
		String protectRecordId = JSONObject.parseObject(userInfo).getJSONObject("extInfo").getString("protectRecordId");
		user.fluentPut("account", protectRecordId.substring(protectRecordId.length() - 25, protectRecordId.length() - 14)).put("accountType", 1);
		JSONObject data = new JSONObject().fluentPut("qryUserExternInfoReq", new JSONObject() {{
			put("commonAccountInfo", user);
		}});
		String requestBody = data.toString();
		Response res = conn.url(qryUserExternInfoUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute();
		JSONObject body = JSONObject.parseObject(res.body());
		if (!body.getBoolean("success")) {
			throw new YunPanException(body.getString("message"));
		}
	}

	/**
	 * 获取分享页面的文件信息
	 *
	 * @param shareUrl 分享链接
	 * @return 文件信息列表
	 */
	@Contract(pure = true)
	public static List<JSONObject> getInfosAsPage(String shareUrl) {
		return getInfosAsPage(shareUrl, "");
	}

	/**
	 * 获取分享页面的文件信息
	 *
	 * @param shareUrl 分享链接
	 * @param sharePwd 提取码
	 * @return 文件信息列表
	 */
	@Contract(pure = true)
	public static List<JSONObject> getInfosAsPage(@NotNull String shareUrl, @NotNull String sharePwd) {
		String linkid = shareUrl.contains("?") ? shareUrl.substring(shareUrl.lastIndexOf("?") + 1) : shareUrl.substring(shareUrl.lastIndexOf("/") + 1);
		return getInfosAsPage(linkid, sharePwd, "root", "");
	}

	public static List<JSONObject> getInfosAsPage(@NotNull String shareId, @NotNull String sharePwd, @NotNull String path, @NotNull String folderPath) {
		List<JSONObject> result = new ArrayList<>();
		Map<String, String> data = new HashMap<>();
		data.put("linkId", shareId);
		data.put("path", path);
		data.put("start", "1");
		data.put("end", String.valueOf(Integer.MAX_VALUE));
		data.put("sortType", "0");
		data.put("sortDr", "1");
		data.put("pass", sharePwd);
		JSONObject outlinkInfo = JSONObject.parseObject(HttpsUtil.connect(outlinkInfoUrl).data(data).method(Method.POST).execute().body()).getJSONObject("data");
		JSONObject caLst = outlinkInfo.getJSONObject("caLst");
		if (caLst.getJSONObject("$").getInteger("length") > 0) {
			caLst.getJSONArray("outLinkCaInfo").toList(JSONObject.class).forEach(ca -> result.addAll(getInfosAsPage(shareId, sharePwd, ca.getString("path"), folderPath + "/" + ca.getString("caName"))));
		}
		JSONObject coLst = outlinkInfo.getJSONObject("coLst");
		if (coLst.getJSONObject("$").getInteger("length") > 0) {
			result.addAll(coLst.getJSONArray("outLinkCoInfo").toList(JSONObject.class).stream().map(l -> l.fluentPut("folderPath", folderPath)).toList());
		}
		return result;
	}

	/**
	 * 使用本地谷歌浏览器(Edge)登陆,进行需要身份验证的API操作
	 *
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static HeCaiYunPan localLogin() {
		return login(LocalCookie.home().getForDomain("yun.139.com"));
	}

	/**
	 * 使用本地谷歌浏览器登陆,进行需要身份验证的API操作
	 *
	 * @param userHome 本地谷歌浏览器用户数据目录(User Data)
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static HeCaiYunPan localLogin(@NotNull String userHome) {
		return login(LocalCookie.home(userHome).getForDomain("yun.139.com"));
	}

	/**
	 * 登陆账户,进行需要身份验证的API操作
	 *
	 * @param cookies cookies
	 * @return 此链接, 用于身份验证的API操作
	 */
	@Contract(pure = true)
	public static HeCaiYunPan login(@NotNull Map<String, String> cookies) {
		return new HeCaiYunPan(cookies);
	}

	@Contract(pure = true)
	private static String mcloudSign(String body) {
		String key = RandomStringUtils.randomAlphanumeric(16);
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String temstr = "";
		if (!Judge.isEmpty(body)) {
			char[] chars = URIUtil.encodeValue(body).replaceAll("\\+", "%20").replaceAll("%5F", "_").toCharArray();
			Arrays.sort(chars);
			temstr = new String(chars);
		}
		return date + "," + key + "," + DigestUtils.md5Hex(DigestUtils.md5Hex(Base64Util.encode(temstr)) + DigestUtils.md5Hex(date + ":" + key)).toUpperCase();
	}

	/**
	 * 清空回收站
	 *
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject clearRecycle() {
		return batchOprTask(203, 2, empty, "");
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileId 指定的文件或文件夹,可指定多个
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject clearRecycle(@NotNull String... fileId) {
		return clearRecycle(Arrays.asList(fileId));
	}

	/**
	 * 删除多个回收站的文件或文件夹
	 *
	 * @param fileIdList 指定的文件或文件夹ID列表
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject clearRecycle(@NotNull List<String> fileIdList) {
		return batchOprTask(0, 2, fileIdList, "");
	}

	/**
	 * 还原回收站的文件或文件夹
	 *
	 * @param fileId 文件或文件夹ID,可指定多个
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject restore(@NotNull String... fileId) {
		return restore(Arrays.asList(fileId));
	}

	/**
	 * 还原回收站的文件或文件夹
	 *
	 * @param fileIdList 文件或文件夹ID列表
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject restore(@NotNull List<String> fileIdList) {
		return batchOprTask(0, 4, fileIdList, "");
	}

	/**
	 * 获取回收站文件列表
	 *
	 * @return 返回的JSON格式文件列表
	 */
	@Contract(pure = true)
	public List<JSONObject> listRecycleBin() {
		JSONObject data = new JSONObject().fluentPut("getVirDirInfoReq", new JSONObject() {{
			put("commonAccountInfo", user);
			put("catalogIDList", new ArrayList<>() {{add("00019700101000000054");}});
			put("endRange", Integer.MAX_VALUE);
			put("sortDirection", 0);
			put("sortType", 0);
			put("startRange", 1);
		}});
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(virDirInfoUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body()).getJSONObject("data").getJSONObject("getVirDirInfoRes").getJSONArray("virDirInfoList").toList(JSONObject.class);
	}

	/**
	 * 获取分享文件列表
	 *
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject listShares() {
		JSONObject data = new JSONObject().fluentPut("getOutLinkLstReq", new JSONObject() {{
			put("commonAccountInfo", user);
			put("bNum", 1);
			put("eNum", Integer.MAX_VALUE);
			put("needSetAccount", true);
			put("qryType", 1);
			put("srt", 0);
			put("srtDr", 0);
		}});
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(outLinkListUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body());
	}

	/**
	 * 取消分享链接
	 *
	 * @param linkId 文件ID,可指定多个
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject unshare(@NotNull String... linkId) {
		return unshare(Arrays.asList(linkId));
	}

	/**
	 * 取消分享链接
	 *
	 * @param linkIdList 文件ID列表
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject unshare(@NotNull List<String> linkIdList) {
		JSONObject data = new JSONObject().fluentPut("delOutLinkReq", new JSONObject() {{
			put("commonAccountInfo", user);
			put("linkIDs", linkIdList);
		}});
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(delOutLinkUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body());
	}

	/**
	 * 创建分享链接
	 *
	 * @param day    分享天数,0为永久
	 * @param fileId 文件ID,可指定多个
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject share(int day, @NotNull String... fileId) {
		return share(day, Arrays.asList(fileId));
	}

	/**
	 * 创建分享链接
	 *
	 * @param day        分享天数,0为永久
	 * @param fileIdList 文件ID列表
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject share(int day, @NotNull List<String> fileIdList) {
		JSONObject data = new JSONObject().fluentPut("getOutLinkReq", new JSONObject() {{
			put("commonAccountInfo", user);
			put("caIDLst", fileIdList);
			put("coIDLst", empty);
			put("encrypt", 1);
			put("period", day);
			put("periodUnit", 1);
			put("pubType", 1);
			put("subLinkType", 0);
			put("viewerLst", empty);
		}});
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(outLinkUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body());
	}

	/**
	 * 重命名
	 *
	 * @param fileId 文件ID
	 * @param name   重命名后的名称
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject rename(@NotNull String fileId, @NotNull String name) {
		JSONObject data = new JSONObject();
		data.put("commonAccountInfo", user);
		data.put("catalogID", fileId);
		data.put("catalogName", name);
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(updateCatalogInfoUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body());
	}

	@Contract(pure = true)
	public JSONObject copy(@NotNull String parentId, @NotNull String... fileId) {
		return copy(parentId, Arrays.asList(fileId));
	}

	@Contract(pure = true)
	public JSONObject copy(@NotNull String parentId, @NotNull List<String> fileIdList) {
		return batchOprTask(0, 1, fileIdList, parentId);
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param fileId 文件或文件夹ID,可指定多个
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject delete(@NotNull String... fileId) {
		return delete(Arrays.asList(fileId));
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param fileIdList 文件或文件夹ID列表
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject delete(@NotNull List<String> fileIdList) {
		return batchOprTask(201, 2, fileIdList, "");
	}

	/**
	 * 移动文件或文件夹到指定目录
	 *
	 * @param parentId 指定目录ID
	 * @param fileId   文件或文件夹ID,可指定多个
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public JSONObject move(@NotNull String parentId, @NotNull List<String> fileIdList) {
		return batchOprTask(304, 3, fileIdList, parentId);
	}

	@Contract(pure = true)
	private JSONObject batchOprTask(int actionType, int taskType, @NotNull List<String> fileIdList, @NotNull String newCatalogID) {
		JSONObject data = new JSONObject().fluentPut("createBatchOprTaskReq", new JSONObject() {{
			put("actionType", actionType);
			put("commonAccountInfo", user);
			put("taskInfo", new JSONObject() {{
				put("catalogInfoList", fileIdList);
				put("contentInfoList", empty);
				put("newCatalogID", newCatalogID);
			}});
			put("taskType", taskType);
		}});
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(createBatchOprTaskUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body());
	}

	/**
	 * 搜索匹配名称的文件
	 *
	 * @param search 搜索数据
	 * @return 返回的JSON格式文件列表
	 */
	@Contract(pure = true)
	public List<JSONObject> search(@NotNull String search) {
		return search(search, "00019700101000000001");
	}

	/**
	 * 搜索匹配名称的文件
	 *
	 * @param search 搜索数据
	 * @param id     指定目录ID
	 * @return 返回的JSON格式文件列表
	 */
	@Contract(pure = true)
	public List<JSONObject> search(@NotNull String search, @NotNull String id) {
		JSONObject data = new JSONObject();
		data.put("commonAccountInfo", user);
		data.put("conditions", "search_name:\"" + search + "\" and path:\"" + id + "\"");
		data.put("showInfo", new JSONObject().fluentPut("sortInfos", "[]").fluentPut("startNum", 1).fluentPut("stopNum", 100));
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(fileSearchUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body()).getJSONObject("data").getJSONArray("rows").toList(JSONObject.class);
	}

	/**
	 * 在指定目录下创建文件夹
	 *
	 * @param id   父文件夹ID
	 * @param name 文件夹名称
	 * @return 返回的JSON数据
	 */
	@Contract(pure = true)
	public JSONObject createFolder(@NotNull String id, @NotNull String name) {
		JSONObject data = new JSONObject().fluentPut("createCatalogExtReq", new JSONObject() {{
			put("commonAccountInfo", user);
			put("parentCatalogID", id);
			put("newCatalogName", name);
		}});
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(createCatalogExtUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body());
	}

	/**
	 * 获取用户主页的所有文件信息
	 *
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHome() {
		return getInfosAsHomeOfFolder("root");
	}

	/**
	 * 获取用户主页的指定文件夹下的文件信息
	 *
	 * @param folderId 文件夹ID(JSON数据"catalogID"参数),"root"为根目录
	 * @return 文件信息JSON数组
	 */
	@Contract(pure = true)
	public List<JSONObject> getInfosAsHomeOfFolder(@NotNull String folderId) {
		JSONObject data = new JSONObject();
		data.put("catalogID", folderId);
		data.put("catalogSortType", 0);
		data.put("commonAccountInfo", user);
		data.put("contentSortType", 0);
		data.put("endNumber", Integer.MAX_VALUE);
		data.put("filterType", 0);
		data.put("sortDirection", 0);
		data.put("startNumber", 1);
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(diskUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body()).getJSONObject("data").getJSONObject("getDiskResult").getJSONArray("catalogList").toList(JSONObject.class);
	}

	/**
	 * 获取账号内文档文件信息
	 *
	 * @return 文件信息列表
	 */
	public List<JSONObject> getDocumentInfosAsHome() {
		return getSortInfosAsHome(4);
	}

	/**
	 * 获取账号内图片文件信息
	 *
	 * @return 文件信息列表
	 */
	public List<JSONObject> getPictureInfosAsHome() {
		return getSortInfosAsHome(1);
	}

	/**
	 * 获取账号内视频文件信息
	 *
	 * @return 文件信息列表
	 */
	public List<JSONObject> getVideoInfosAsHome() {
		return getSortInfosAsHome(3);
	}

	private List<JSONObject> getSortInfosAsHome(int type) {
		JSONObject data = new JSONObject().fluentPut("getIndividualContentReq", new JSONObject() {{
			put("commonAccountInfo", user);
			put("contentType", type);
			put("endNumber", Integer.MAX_VALUE);
			put("isSumnum", 1);
			put("sortDirection", 1);
			put("startNumber", 1);
		}});
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(individualContentUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body()).getJSONObject("data").getJSONObject("getIndividualContentRsp").getJSONArray("contentList").toList(JSONObject.class);
	}

	/**
	 * 获得分享页面所有文件直链
	 *
	 * @param shareUrl 分享链接
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	@Contract(pure = true)
	public Map<String, String> getStraightsAsPage(@NotNull String shareUrl) {
		return getStraightsAsPage(shareUrl, "");
	}

	/**
	 * 获得分享页面所有文件直链
	 *
	 * @param shareUrl 分享链接
	 * @param sharePwd 提取码
	 * @return 列表 ( 文件路径 - 文件直链 )
	 */
	@Contract(pure = true)
	public Map<String, String> getStraightsAsPage(@NotNull String shareUrl, @NotNull String sharePwd) {
		String linkid = shareUrl.contains("?") ? shareUrl.substring(shareUrl.lastIndexOf("?") + 1) : shareUrl.substring(shareUrl.lastIndexOf("/") + 1);
		Map<String, String> result = new HashMap<>();
		List<JSONObject> infos = getInfosAsPage(linkid, sharePwd, "root", "");
		for (JSONObject info : infos) {
			String path = info.getString("path");
			Map<String, String> data = new HashMap<>();
			data.put("linkId", linkid);
			data.put("contentIds", path);
			data.put("catalogIds", "");
			data.put("isReturnCdnDownloadUrl", "1");
			result.put(info.getString("folderPath") + "/" + info.getString("coName"), JSONObject.parseObject(HttpsUtil.connect(downloadUrl).data(data).method(Method.POST).execute().body()).getJSONObject("data").getString("redrUrl"));
		}
		return result;
	}

	/**
	 * 获取用户的文件直链
	 *
	 * @param fileid 文件ID
	 * @return 文件直链
	 */
	@Contract(pure = true)
	public String getStraight(@NotNull String fileid) {
		JSONObject data = new JSONObject();
		data.put("commonAccountInfo", user);
		data.put("contentID", fileid);
		data.put("extInfo", new JSONObject().fluentPut("isReturnCdnDownloadUrl", "1"));
		data.put("inline", 0);
		data.put("operation", 0);
		String requestBody = data.toString();
		return JSONObject.parseObject(conn.url(downloadRequestUrl).header("mcloud-sign", mcloudSign(requestBody)).requestBody(requestBody).method(Method.POST).execute().body()).getJSONObject("data").getString("downloadURL");
	}

}
