package org.haic.often.parser.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.haic.often.annotations.JsonParam;
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
		try {
			assert parameter != null;
			var jsonParam = parameter.getParameterAnnotation(JsonParam.class);
			assert jsonParam != null;
			return TypeUtil.convert(RequestBodyScope.get(jsonParam.value()), parameter.getParameterType());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 将{@link Type} 转化为Jackson需要的{com.fasterxml.jackson.databind.JavaType}
	 */
	public static JavaType getJavaType(Type type, Class<?> contextClass) {
		//MAPPER这个可以使用ObjectMapperUtils中ObjectMapper
		TypeFactory typeFactory = objectMapper.getTypeFactory();
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
