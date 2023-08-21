package org.haic.often.parser.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.haic.often.Judge;
import org.haic.often.util.IOUtil;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;

import static com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance;
import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;

/**
 * @author haicdust
 * @version 1.0
 * @since 2023/5/7 16:57
 */
public class RequestBodyScope {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * 获取RequestBody里面的值
	 */
	public static Object get(String name) {
		return name.isEmpty() ? resolveRequestBody() : resolveRequestBody().get(name);
	}

	/**
	 * 解析requestBody对象为Map对象
	 */
	public static Map<String, Object> resolveRequestBody() {
		var request = ((ServletRequestAttributes) currentRequestAttributes()).getRequest();
		if (!isJsonRequest(request)) throw new IllegalStateException("请求类型不为JSON");
		try (var input = request.getInputStream()) {
			return fromJson(IOUtil.stream(input).read());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json串转化为Map对象
	 */
	private static Map<String, Object> fromJson(String json) {
		if (Judge.isEmpty(json)) json = "{}";
		try {
			//json串转化为特定格式的Map对象
			return MAPPER.readValue(json, defaultInstance().constructMapType(Map.class, String.class, Object.class));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 判断是否为JSON格式？
	 */
	private static boolean isJsonRequest(HttpServletRequest request) {
		var contentType = request.getHeader("Content-Type");
		return contentType != null && contentType.toLowerCase().startsWith("application/json");
	}
}
