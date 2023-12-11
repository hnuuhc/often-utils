package org.haic.often.parser.json;

import org.haic.often.annotations.NotNull;
import org.haic.often.function.FourFunction;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;
import org.haic.often.util.Validate;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * JSON快捷解析方案
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/11 16:31
 */
public class JSONPath {

	private final Object json;

	public JSONPath(@NotNull String json) {
		this.json = new ParserStringBuilder(json).stripLeading().charAt() == '[' ? JSONArray.parseArray(json) : JSONObject.parseObject(json);
	}

	public JSONPath(JSONObject json) {
		this.json = json;
	}

	public JSONPath(JSONArray json) {
		this.json = json;
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link #select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	public Object select(@NotNull String cssQuery) {
		return select(cssQuery, Object.class);
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link #select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @param type     指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, TypeReference<T> type) {
		return TypeUtil.convert(select(cssQuery, Object.class), type);
	}

	/**
	 * 使用规则对JSON进行快捷解析
	 * <p>
	 * 解析规则:
	 * <pre>	'.' 		- 获取MAP对象的对应值</pre>
	 * <pre>	'['和']' 	- 和'.'等价,在内部KEY值必须使用'\''环绕</pre>
	 * <pre>	'@' 		- 当前对象,在方括号内部必须存在</pre>
	 * <pre>	'!' 		- 非(反义),在'@'之前为相反结果,之后为操作符</pre>
	 * <pre>	'<' '>' '='	- 操作符,用于判断</pre>
	 * <pre>	"=="		- 字符串相等判断和{@link  String#equals}等价</pre>
	 * <pre>	"=~" "~="	- 筛选包含指定数据的值和{@link  String#contains}等价</pre>
	 * <pre>	'|'		- 管道符,用于合并多个结果,返回值为数组</pre>
	 * <pre>测试JSON: {"123":{"zzz":"ccc","cc2":["123","321"],"cc3":"654","cc4":["888","117"]},"c43":{"zzz":"113","zzz2":"223"}}</pre>
	 * <pre>规则: ".123.cc2" 			结果: [123, 321]</pre>
	 * <pre>规则: ".123.cc2[@<200]" 			结果: [123]</pre>
	 * <pre>规则: ".123.cc2[0]" 			结果: 123</pre>
	 * <pre>规则: ".c43[@'zzz'>100][0].zzz"		结果: 113</pre>
	 *
	 * @param cssQuery 查询规则
	 * @param clazz    指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, Class<T> clazz) {
		Object result = this.json;
		for (int index = 0; index < cssQuery.length(); index++) {
			if (result == null) return null;
			switch (cssQuery.charAt(index)) {
				case '.' -> {
					int off = ++index;
					//noinspection StatementWithEmptyBody
					while (cssQuery.charAt(index) != '.' && cssQuery.charAt(index) != '[' && ++index < cssQuery.length()) {}
					var key = cssQuery.substring(off, index--);
					result = TypeUtil.convertMap(result, Object.class, Object.class).get(key);
				}
				case '[' -> {
					FourFunction<Object, Integer, Integer, Boolean, Object> filter = (obj, i, end, non) -> {
						var as = obj instanceof Collection<?> c ? c.toArray() : obj instanceof Object[] os ? os : JSONArray.parseArray(String.valueOf(obj)).toArray();
						Function<Predicate<Map<String, Integer>>, Object> mapIntCompare = predicate -> TypeUtil.convertList(as, Map.class).stream().map(l -> TypeUtil.convert(l, new TypeReference<Map<String, Integer>>() {})).filter(predicate).collect(Collectors.toList());
						Function<Predicate<Map<String, Boolean>>, Object> mapBoolCompare = predicate -> TypeUtil.convertList(as, Map.class).stream().map(l -> TypeUtil.convert(l, new TypeReference<Map<String, Boolean>>() {})).filter(predicate).collect(Collectors.toList());
						Function<Predicate<Map<String, String>>, Object> mapStringCompare = predicate -> TypeUtil.convertList(as, Map.class).stream().map(l -> TypeUtil.convert(l, new TypeReference<Map<String, String>>() {})).filter(predicate).collect(Collectors.toList());
						switch (cssQuery.charAt(i)) {
							case '<' -> {
								int a = cssQuery.charAt(++i) == '=' ? Integer.parseInt(cssQuery.substring(++i, end)) + 1 : Integer.parseInt(cssQuery.substring(i, end));
								return TypeUtil.convertList(as, Integer.class).stream().filter(non ? l -> l > a : l -> l < a).collect(Collectors.toList());
							}
							case '>' -> {
								int a = cssQuery.charAt(++i) == '=' ? Integer.parseInt(cssQuery.substring(++i, end)) - 1 : Integer.parseInt(cssQuery.substring(i, end));
								return TypeUtil.convertList(as, Integer.class).stream().filter(non ? l -> l < a : l -> l > a).collect(Collectors.toList());
							}
							case '=' -> {
								switch (cssQuery.charAt(++i)) {
									case '=' -> {
										if (cssQuery.charAt(++i) == '\'') {
											var value = cssQuery.substring(++i, cssQuery.indexOf("'", i));
											return TypeUtil.convertList(as, String.class).stream().filter(non ? l -> !l.equals(value) : l -> l.equals(value)).collect(Collectors.toList());
										}
										var value = cssQuery.substring(i, end);
										switch (value) {
											case "null" -> {
												return TypeUtil.convertList(as, Object.class).stream().filter(non ? Objects::nonNull : Objects::isNull).collect(Collectors.toList());
											}
											case "true" -> {
												return TypeUtil.convertList(as, Boolean.class).stream().filter(non ? l -> !l : l -> l).collect(Collectors.toList());
											}
											case "false" -> {
												return TypeUtil.convertList(as, Boolean.class).stream().filter(non ? l -> l : l -> !l).collect(Collectors.toList());
											}
											default -> {
												int a = Integer.parseInt(value);
												return TypeUtil.convertList(as, Integer.class).stream().filter(non ? l -> l == a : l -> l != a).collect(Collectors.toList());
											}
										}
									}
									case '~' -> {
										Validate.isTrue(cssQuery.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
										var value = cssQuery.substring(++i, cssQuery.indexOf("'", i));
										return TypeUtil.convertList(as, String.class).stream().filter(non ? l -> !l.contains(value) : l -> l.contains(value)).collect(Collectors.toList());
									}
									default -> throw new IllegalArgumentException("查询参数在索引 " + i + " 处未知的判断符");
								}
							}
							case '~' -> {
								Validate.isTrue(cssQuery.charAt(++i) == '=', "查询参数在索引 " + i + " 处未知的判断符");
								Validate.isTrue(cssQuery.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
								var value = cssQuery.substring(++i, cssQuery.indexOf("'", i));
								return TypeUtil.convertList(as, String.class).stream().filter(non ? l -> !value.contains(l) : value::contains).collect(Collectors.toList());
							}
							case '!' -> {
								Validate.isTrue(cssQuery.charAt(++i) == '=', "查询参数在索引 " + i + " 处期待值不为'='");
								int a = Integer.parseInt(cssQuery.substring(++i, end));
								return TypeUtil.convertList(as, Integer.class).stream().filter(non ? l -> l == a : l -> l != a).collect(Collectors.toList());
							}
							case '\'' -> {
								var key = cssQuery.substring(++i, i = cssQuery.indexOf("'", i));
								switch (cssQuery.charAt(++i)) {
									case '<' -> {
										if (cssQuery.charAt(++i) == '=') {
											int a = Integer.parseInt(cssQuery.substring(++i, end));
											return mapIntCompare.apply(non ? l -> l.get(key) > a : l -> l.get(key) <= a);
										}
										int a = Integer.parseInt(cssQuery.substring(i, end));
										return mapIntCompare.apply(non ? l -> l.get(key) >= a : l -> l.get(key) < a);
									}
									case '>' -> {
										if (cssQuery.charAt(++i) == '=') {
											int a = Integer.parseInt(cssQuery.substring(++i, end));
											return mapIntCompare.apply(non ? l -> l.get(key) < a : l -> l.get(key) >= a);
										}
										int a = cssQuery.charAt(++i) == '=' ? Integer.parseInt(cssQuery.substring(++i, end)) - 1 : Integer.parseInt(cssQuery.substring(i, end));
										return mapIntCompare.apply(non ? l -> l.get(key) <= a : l -> l.get(key) > a);
									}
									case '=' -> {
										switch (cssQuery.charAt(++i)) {
											case '=' -> {
												if (cssQuery.charAt(++i) == '\'') {
													var value = cssQuery.substring(++i, cssQuery.indexOf("'", i));
													return mapStringCompare.apply(non ? l -> !l.get(key).equals(value) : l -> l.get(key).equals(value));
												}
												var value = cssQuery.substring(i, end);
												switch (value) {
													case "null" -> {
														return TypeUtil.convertList(as, Map.class).stream().filter(non ? l -> l.get(key) != null : l -> l.get(key) == null).collect(Collectors.toList());
													}
													case "true" -> {
														return mapBoolCompare.apply(non ? l -> !l.get(key) : l -> l.get(key));
													}
													case "false" -> {
														return mapBoolCompare.apply(non ? l -> l.get(key) : l -> !l.get(key));
													}
													default -> {
														int a = Integer.parseInt(value);
														return mapIntCompare.apply(non ? l -> l.get(key) == a : l -> l.get(key) != a);
													}
												}
											}
											case '~' -> {
												Validate.isTrue(cssQuery.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
												var value = cssQuery.substring(++i, cssQuery.indexOf("'", i));
												return mapStringCompare.apply(non ? l -> !value.contains(l.get(key)) : l -> value.contains(l.get(key)));
											}
											default -> throw new IllegalArgumentException("查询参数在索引 " + i + " 处未知的判断符");
										}
									}
									case '~' -> {
										Validate.isTrue(cssQuery.charAt(++i) == '=', "查询参数在索引 " + i + " 处未知的判断符");
										Validate.isTrue(cssQuery.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
										var value = cssQuery.substring(++i, cssQuery.indexOf("'", i));
										return mapStringCompare.apply(non ? l -> !l.get(key).contains(value) : l -> l.get(key).contains(value));
									}
									case '!' -> {
										Validate.isTrue(cssQuery.charAt(++i) == '=', "查询参数在索引 " + i + " 处期待值不为'='");
										if (cssQuery.charAt(++i) == '\'') {
											var value = cssQuery.substring(++i, cssQuery.indexOf("'", i));
											return mapStringCompare.apply(non ? l -> l.get(key).equals(value) : l -> !l.get(key).equals(value));
										}
										var value = cssQuery.substring(i, end);
										switch (value) {
											case "null" -> {
												return TypeUtil.convertList(as, Map.class).stream().filter(non ? l -> l.get(key) == null : l -> l.get(key) != null).collect(Collectors.toList());
											}
											case "true" -> {
												return mapBoolCompare.apply(non ? l -> l.get(key) : l -> !l.get(key));
											}
											case "false" -> {
												return mapBoolCompare.apply(non ? l -> !l.get(key) : l -> l.get(key));
											}
											default -> {
												int a = Integer.parseInt(value);
												return mapIntCompare.apply(non ? l -> l.get(key) != a : l -> l.get(key) == a);
											}
										}
									}
									case ']' -> {
										return mapIntCompare.apply(non ? l -> !l.containsKey(key) : l -> l.containsKey(key));
									}
								}
								throw new IllegalArgumentException("查询参数在索引 " + i + " 之前未找到判断符");
							}
							default -> throw new IllegalArgumentException("查询参数在索引 " + i + " 处不正确");
						}
					};
					if (cssQuery.charAt(++index) == '!') {
						Validate.isTrue(cssQuery.charAt(++index) == '@', "查询参数在索引 " + index + " 处期待值不为'@'");
						result = filter.apply(result, ++index, index = cssQuery.indexOf("]", index), true);
					} else if (cssQuery.charAt(index) == '@') {
						result = filter.apply(result, ++index, index = cssQuery.indexOf("]", index), false);
					} else if (cssQuery.charAt(index) == '\'') {
						var key = cssQuery.substring(++index, index = cssQuery.indexOf("'", index));
						index++;
						result = ((Map<?, ?>) result).get(key);
					} else {
						var key = Integer.parseInt(cssQuery.substring(index, index = cssQuery.indexOf("]", index)));
						if (result instanceof Collection<?> c) {
							result = c.toArray()[key];
						} else if (result instanceof Object[] c) {
							result = c[key];
						} else {
							result = TypeUtil.convert(result, JSONArray.class).get(key);
						}
					}
				}
				case '|' -> {
					Collection<Object> list = result instanceof Collection<?> c ? new ArrayList<>(c) : result instanceof Object[] objs ? Arrays.asList(objs) : new JSONArray().fluentAdd(result);
					Object pipeline = select(cssQuery.substring(++index), clazz);
					if (pipeline instanceof Collection<?> c) list.addAll(c);
					else if (result instanceof Object[] objs) list.addAll(Arrays.asList(objs));
					else list.add(pipeline);
					return TypeUtil.convert(list, clazz);
				}
				default -> throw new IllegalArgumentException("查询参数在索引 " + index + " 处不正确");
			}
		}
		return TypeUtil.convert(result, clazz);
	}

}
