package org.haic.often.parser.json;

import org.haic.often.annotations.NotNull;
import org.haic.often.function.FourFunction;
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

	public JSONPath(JSONObject json) {
		this.json = json;
	}

	public JSONPath(JSONArray json) {
		this.json = json;
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link #select(String, Class)}
	 *
	 * @param regex 查询规则
	 * @param type  指定返回类型
	 * @param <T>   返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String regex, TypeReference<T> type) {
		return TypeUtil.convert(select(regex, Object.class), type);
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
	 * @param regex 查询规则
	 * @param clazz 指定返回类型
	 * @param <T>   返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String regex, Class<T> clazz) {
		Object result = this.json;
		for (int index = 0; index < regex.length(); index++) {
			switch (regex.charAt(index)) {
				case '.' -> {
					int off = ++index;
					//noinspection StatementWithEmptyBody
					while (regex.charAt(index) != '.' && regex.charAt(index) != '[' && ++index < regex.length()) {}
					var key = regex.substring(off, index--);
					result = ((Map<?, ?>) result).get(key);
				}
				case '[' -> {
					FourFunction<Object, Integer, Integer, Boolean, Object> filter = (obj, i, end, non) -> {
						var as = obj instanceof Collection<?> c ? c.toArray() : obj instanceof Object[] os ? os : JSONArray.parseArray(String.valueOf(obj)).toArray();
						Function<Predicate<Map<String, Integer>>, Object> mapIntCompare = predicate -> TypeUtil.convertList(as, Map.class).stream().map(l -> TypeUtil.convert(l, new TypeReference<Map<String, Integer>>() {})).filter(predicate).collect(Collectors.toList());
						Function<Predicate<Map<String, Boolean>>, Object> mapBoolCompare = predicate -> TypeUtil.convertList(as, Map.class).stream().map(l -> TypeUtil.convert(l, new TypeReference<Map<String, Boolean>>() {})).filter(predicate).collect(Collectors.toList());
						Function<Predicate<Map<String, String>>, Object> mapStringCompare = predicate -> TypeUtil.convertList(as, Map.class).stream().map(l -> TypeUtil.convert(l, new TypeReference<Map<String, String>>() {})).filter(predicate).collect(Collectors.toList());

						switch (regex.charAt(i)) {
							case '<' -> {
								int a = regex.charAt(++i) == '=' ? Integer.parseInt(regex.substring(++i, end)) + 1 : Integer.parseInt(regex.substring(i, end));
								return TypeUtil.convertList(as, Integer.class).stream().filter(non ? l -> l > a : l -> l < a).collect(Collectors.toList());
							}
							case '>' -> {
								int a = regex.charAt(++i) == '=' ? Integer.parseInt(regex.substring(++i, end)) - 1 : Integer.parseInt(regex.substring(i, end));
								return TypeUtil.convertList(as, Integer.class).stream().filter(non ? l -> l < a : l -> l > a).collect(Collectors.toList());
							}
							case '=' -> {
								switch (regex.charAt(++i)) {
									case '=' -> {
										if (regex.charAt(++i) == '\'') {
											var value = regex.substring(++i, regex.indexOf("'", i));
											return TypeUtil.convertList(as, String.class).stream().filter(non ? l -> !l.equals(value) : l -> l.equals(value)).collect(Collectors.toList());
										}
										var value = regex.substring(i, end);
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
										Validate.isTrue(regex.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
										var value = regex.substring(++i, regex.indexOf("'", i));
										return TypeUtil.convertList(as, String.class).stream().filter(non ? l -> !l.contains(value) : l -> l.contains(value)).collect(Collectors.toList());
									}
									default -> throw new IllegalArgumentException("查询参数在索引 " + i + " 处未知的判断符");
								}
							}
							case '~' -> {
								Validate.isTrue(regex.charAt(++i) == '=', "查询参数在索引 " + i + " 处未知的判断符");
								Validate.isTrue(regex.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
								var value = regex.substring(++i, regex.indexOf("'", i));
								return TypeUtil.convertList(as, String.class).stream().filter(non ? l -> !value.contains(l) : value::contains).collect(Collectors.toList());
							}
							case '!' -> {
								Validate.isTrue(regex.charAt(++i) == '=', "查询参数在索引 " + i + " 处期待值不为'='");
								int a = Integer.parseInt(regex.substring(++i, end));
								return TypeUtil.convertList(as, Integer.class).stream().filter(non ? l -> l == a : l -> l != a).collect(Collectors.toList());
							}
							case '\'' -> {
								var key = regex.substring(++i, i = regex.indexOf("'", i));
								switch (regex.charAt(++i)) {
									case '<' -> {
										if (regex.charAt(++i) == '=') {
											int a = Integer.parseInt(regex.substring(++i, end));
											return mapIntCompare.apply(non ? l -> l.get(key) > a : l -> l.get(key) <= a);
										}
										int a = Integer.parseInt(regex.substring(i, end));
										return mapIntCompare.apply(non ? l -> l.get(key) >= a : l -> l.get(key) < a);
									}
									case '>' -> {
										if (regex.charAt(++i) == '=') {
											int a = Integer.parseInt(regex.substring(++i, end));
											return mapIntCompare.apply(non ? l -> l.get(key) < a : l -> l.get(key) >= a);
										}
										int a = regex.charAt(++i) == '=' ? Integer.parseInt(regex.substring(++i, end)) - 1 : Integer.parseInt(regex.substring(i, end));
										return mapIntCompare.apply(non ? l -> l.get(key) <= a : l -> l.get(key) > a);
									}
									case '=' -> {
										switch (regex.charAt(++i)) {
											case '=' -> {
												if (regex.charAt(++i) == '\'') {
													var value = regex.substring(++i, regex.indexOf("'", i));
													return mapStringCompare.apply(non ? l -> !l.get(key).equals(value) : l -> l.get(key).equals(value));
												}
												var value = regex.substring(i, end);
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
												Validate.isTrue(regex.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
												var value = regex.substring(++i, regex.indexOf("'", i));
												return mapStringCompare.apply(non ? l -> !value.contains(l.get(key)) : l -> value.contains(l.get(key)));
											}
											default -> throw new IllegalArgumentException("查询参数在索引 " + i + " 处未知的判断符");
										}
									}
									case '~' -> {
										Validate.isTrue(regex.charAt(++i) == '=', "查询参数在索引 " + i + " 处未知的判断符");
										Validate.isTrue(regex.charAt(++i) == '\'', "查询参数在索引 " + i + " 处期待值不为''',包含判断符必须为字符串,且使用单引号环绕");
										var value = regex.substring(++i, regex.indexOf("'", i));
										return mapStringCompare.apply(non ? l -> !l.get(key).contains(value) : l -> l.get(key).contains(value));
									}
									case '!' -> {
										Validate.isTrue(regex.charAt(++i) == '=', "查询参数在索引 " + i + " 处期待值不为'='");
										if (regex.charAt(++i) == '\'') {
											var value = regex.substring(++i, regex.indexOf("'", i));
											return mapStringCompare.apply(non ? l -> l.get(key).equals(value) : l -> !l.get(key).equals(value));
										}
										var value = regex.substring(i, end);
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
					if (regex.charAt(++index) == '!') {
						Validate.isTrue(regex.charAt(++index) == '@', "查询参数在索引 " + index + " 处期待值不为'@'");
						result = filter.apply(result, ++index, index = regex.indexOf("]", index), true);
					} else if (regex.charAt(index) == '@') {
						result = filter.apply(result, ++index, index = regex.indexOf("]", index), false);
					} else if (regex.charAt(index) == '\'') {
						var key = regex.substring(++index, index = regex.indexOf("'", index) + 1);
						result = ((Map<?, ?>) result).get(key);
					} else {
						Validate.isTrue(result instanceof Collection, "上次查询结果不为数组");
						var key = Integer.parseInt(regex.substring(index, index = regex.indexOf("]", index)));
						result = ((List<?>) result).get(key);
					}
				}
				case '|' -> {
					Collection<Object> list = result instanceof Collection<?> c ? new ArrayList<>(c) : result instanceof Object[] objs ? Arrays.asList(objs) : new JSONArray().fluentAdd(result);
					Object pipeline = select(regex.substring(++index), clazz);
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
