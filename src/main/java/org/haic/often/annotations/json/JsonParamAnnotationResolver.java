package org.haic.often.annotations.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.haic.often.exception.MissJSONParametersException;
import org.haic.often.exception.TypeException;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.TypeUtil;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Type;

/**
 * @author haicdust
 * @version 1.0
 * @since 2023/5/7 16:54
 */
public class JsonParamAnnotationResolver implements HandlerMethodArgumentResolver {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(JsonParam.class);
	}

	@Override
	public Object resolveArgument(@Nullable MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, @Nullable NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
		assert parameter != null;
		var param = parameter.getParameterAnnotation(JsonParam.class);
		assert param != null;
		var value = TypeUtil.convert(RequestBodyScope.get(param.value()), parameter.getParameterType());
		var exist = param.exist();
		if (exist.length > 0) {
			if (value instanceof JSONObject json) {
				for (var key : exist) {
					if (json.get(key) == null) throw new MissJSONParametersException("缺少参数");
				}
			} else {
				throw new TypeException("参数不为JSON类型");
			}
		}
		return value;
	}

	/**
	 * 将{@link Type} 转化为Jackson需要的{com.fasterxml.jackson.databind.JavaType}
	 */
	public static JavaType getJavaType(Type type, Class<?> contextClass) {
		//MAPPER这个可以使用ObjectMapperUtils中ObjectMapper
		var typeFactory = objectMapper.getTypeFactory();
		//这种是处理public <T extends User> T testEnvV3(@JsonParam("users") List<T> user) 这种类型。
		return typeFactory.constructType(GenericTypeResolver.resolveType(type, contextClass));
	}

	/**
	 * 将Object对象转换为具体的对象类型（支持泛型）
	 */
	public static <T> T value(Object rawValue, JavaType javaType) {
		return objectMapper.convertValue(rawValue, javaType);
	}

}
