package org.haic.often.parser.json;

import org.haic.often.annotations.NotNull;
import org.haic.often.exception.JSONException;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.parser.xml.Element;
import org.haic.often.parser.xml.XmlChilds;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON数组类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/6 0:30
 */
public class JSONArray extends ArrayList<Object> {

	public JSONArray() {super();}

	public JSONArray(Collection<?> c) {super(c);}

	/**
	 * 这是解析用构建,切勿使用
	 *
	 * @param body 字符串
	 */
	public JSONArray(@NotNull ParserStringBuilder body) {
		if (body.charAt() == '[') {
			if (body.offset(1).stripLeading().charAt() == ']') return;
			while (body.isNoOutBounds()) {
				switch (body.charAt()) {
					case '"', '\'' -> this.add(body.intercept());
					case '{' -> this.add(new JSONObject(body));
					case '[' -> this.add(new JSONArray(body));
					case 'n' -> {
						if (body.startsWith("null")) this.add(null);
						else throw new JSONException("位置 " + body.pos() + " 处期望值不为'null'");
						body.offset(3);
					}
					case 't' -> {
						if (body.startsWith("true")) this.add(true);
						else throw new JSONException("位置 " + body.pos() + " 处期望值不为'true'");
						body.offset(3);
					}
					case 'f' -> {
						if (body.startsWith("false")) this.add(false);
						else throw new JSONException("位置 " + body.pos() + " 处期望值不为'false'");
						body.offset(4);
					}
					case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
						var value = new StringBuilder();
						do {
							value.append(body.charAt());
						} while (Character.isDigit(body.offset(1).charAt()) || body.charAt() == '.');
						if (body.charAt() == 'e' || body.charAt() == 'E') { // 自然数
							value.append('e');
							if (body.offset(1).charAt() == '+') {
								value.append('+');
								body.offset(1);
							}
							do value.append(body.charAt()); while (Character.isDigit(body.offset(1).charAt()));
						}
						this.add(new JSONNumber(value.toString()));
						body.offset(-1); // 修正索引
					}
					default -> throw new JSONException("位置 " + body.pos() + " 处期望值不为'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '['");
				}
				if (body.offset(1).stripLeading().charAt() == ']') return;
				if (body.charAt() != ',') throw new JSONException("位置 " + body.pos() + " 处期望值不为分隔符','");
				body.offset(1).stripLeading();
			}
			throw new JSONException("数据未封闭");
		} else if (body.charAt(body.pos()) == '{') {
			this.add(new JSONObject(body));
		} else {
			throw new JSONException("位置 " + body.pos() + " 处格式错误期望值不为'['或'{'");
		}
	}

	/**
	 * 解析并获取JSON数组
	 *
	 * @param body JSON字符串
	 * @return JSON数组
	 */
	public static JSONArray parseArray(@NotNull String body) {
		var builder = new ParserStringBuilder(body).strip();
		var object = new JSONArray(builder);
		if (builder.pos() + 1 != builder.length()) throw new JSONException("格式错误,在封闭符号之后仍然存在数据");
		return object;
	}

	/**
	 * 解析并获取JSON数组
	 *
	 * @param list 数组
	 * @return JSON数组
	 */
	public static JSONArray parseArray(@NotNull Collection<?> list) {
		return new JSONArray().fluentAddAll(list);
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	public Object select(@NotNull String cssQuery) {
		return new JSONPath(this).select(cssQuery);
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @param type     指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, TypeReference<T> type) {
		return new JSONPath(this).select(cssQuery, type);
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @param clazz    指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, Class<T> clazz) {
		return new JSONPath(this).select(cssQuery, clazz);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public Object get(int i) {
		return JSONFormat.format(super.get(i));
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param i      要返回的元素的索引
	 * @param mapper 函数式接口,用于指定转换类型
	 * @param <T>    返回泛型
	 * @return 值
	 */
	public <T> T get(int i, @NotNull Function<Object, ? extends T> mapper) {
		return mapper.apply(this.get(i));
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param i    要返回的元素的索引
	 * @param type TypeReference接口类型
	 * @param <T>  返回泛型
	 * @return 值
	 */
	public <T> T get(int i, @NotNull TypeReference<T> type) {
		return TypeUtil.convert(this.get(i), type);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i         要返回的元素的索引
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 值
	 */
	public <T> T get(int i, @NotNull Class<T> itemClass) {
		return TypeUtil.convert(this.get(i), itemClass);
	}

	/**
	 * 获取名称对应键的数组
	 *
	 * @param i         要返回的元素的索引
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 数组
	 */
	public <T> List<T> getList(int i, @NotNull Class<T> itemClass) {
		return TypeUtil.convertList(this.get(i), itemClass);
	}

	/**
	 * @param i          要返回的元素的索引
	 * @param keyClass   指定键类型
	 * @param valueClass 指定值类型
	 * @param <K>        键泛型
	 * @param <V>        值泛型
	 * @return Map对象
	 */
	public <K, V> Map<K, V> getMap(int i, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		return TypeUtil.convertMap(this.get(i), keyClass, valueClass);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public String getString(int i) {
		return this.get(i, String.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public boolean getBoolean(int i) {
		return this.get(i, Boolean.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public byte getByte(int i) {
		return this.get(i, Byte.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public short getShort(int i) {
		return this.get(i, Short.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public int getInteger(int i) {
		return this.get(i, Integer.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public long getLong(int i) {
		return this.get(i, Long.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public float getFloat(int i) {
		return this.get(i, Float.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public double getDouble(int i) {
		return this.get(i, Double.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public JSONObject getJSONObject(int i) {
		return this.get(i, JSONObject.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public JSONArray getJSONArray(int i) {
		return this.get(i, JSONArray.class);
	}

	/**
	 * 转换为指定类型数组
	 *
	 * @param itemClass 指定类型
	 * @param <T>       数组泛型
	 * @return 指定类型的数组
	 */
	public <T> List<T> toList(@NotNull Class<T> itemClass) {
		return TypeUtil.convertList(this, itemClass);
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param obj 待添加的元素
	 * @return 自身
	 */
	public JSONArray fluentAdd(Object obj) {
		super.add(obj);
		return this;
	}

	/**
	 * 添加数组所有元素并返回自身
	 *
	 * @param c 数组
	 * @return 自身
	 */
	public JSONArray fluentAddAll(Collection<?> c) {
		super.addAll(c);
		return this;
	}

	/**
	 * 按照所给比较函数获取排序后的数组
	 *
	 * @param comparator 一个比较函数，它对某些对象集合施加总排序
	 * @return 排序后的数组
	 */
	public JSONArray sorted(Comparator<Object> comparator) {
		return JSONArray.parseArray(this.stream().sorted(comparator).toList());
	}

	/**
	 * 转化为 {@link XmlChilds} 类型
	 *
	 * @return XmlChilds对象
	 */
	public XmlChilds toXmlChilds() {
		return toXmlChilds(null);
	}

	/**
	 * 转化为 {@link XmlChilds} 类型
	 *
	 * @param parent 父节点
	 * @return XmlChilds对象
	 */
	public XmlChilds toXmlChilds(Element parent) {
		return this.stream().map(m -> m instanceof JSONObject e ? e.toXmlTree(parent) : m).collect(Collectors.toCollection(XmlChilds::new));
	}

	/**
	 * 输出当前JSON数据,并对中文进行转义,使其符合JSON传输要求
	 *
	 * @return JSON字符串
	 */
	@NotNull
	public String toJSONString() {
		return '[' + this.stream().map(JSONFormat::toNetOutFormat).collect(Collectors.joining(",")) + ']';
	}

	@Override
	public String toString() {
		return '[' + this.stream().map(JSONFormat::toOutFormat).collect(Collectors.joining(",")) + ']';
	}

	/**
	 * 以指定的深度格式化当前标签
	 *
	 * @param depth 深度
	 * @return 格式化的标签
	 */
	@NotNull
	public String toString(int depth) {
		return this.isEmpty() ? "[]" : '[' + this.stream().map(token -> '\n' + "    ".repeat(depth + 1) + JSONFormat.toOutFormat(token, depth)).collect(Collectors.joining(",")) + "\n" + "    ".repeat(depth) + ']';
	}

}
