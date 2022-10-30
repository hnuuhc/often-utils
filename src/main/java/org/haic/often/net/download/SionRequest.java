package org.haic.often.net.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Download Request
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/9/27 11:03
 */
public class SionRequest {

	private String url; // 请求URL
	private String hash; // hash值,md5算法

	private long fileSize; // 文件大小
	private int statusCode;

	private File storage;

	private Map<String, String> headers = new HashMap<>(); // headers
	private Map<String, String> cookies = new HashMap<>(); // cookies

	public int statusCode() {
		return statusCode;
	}

	public SionRequest statusCode(int statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public SionRequest setUrl(String url) {
		this.url = url;
		return this;
	}

	public File getStorage() {
		return storage;
	}

	public SionRequest setStorage(File storage) {
		this.storage = storage;
		return this;
	}

	public String getHash() {
		return hash;
	}

	public SionRequest setHash(String hash) {
		this.hash = hash;
		return this;
	}

	public long getFileSize() {
		return fileSize;
	}

	public SionRequest setFileSize(long fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	public Map<String, String> headers() {
		return headers;
	}

	public SionRequest headers(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public Map<String, String> cookies() {
		return cookies;
	}

	public SionRequest cookies(Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

}