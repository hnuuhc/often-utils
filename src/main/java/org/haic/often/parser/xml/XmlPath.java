package org.haic.often.parser.xml;

import org.haic.often.parser.ParserStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * XML快捷解析方案
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/3/6 11:19
 */
public class XmlPath {

    private final Elements es;

    public XmlPath(@NotNull String body) {
        this(Document.parse(body));
    }

    public XmlPath(Element e) {
        this(new Elements().fluentAdd(e));
    }

    public XmlPath(Elements es) {
        this.es = es;
    }

    /**
     * 按照指定规则查询标签,支持使用空格分割,以确保更精确的查询
     * <p>
     * 例:
     * <blockquote>
     * <pre> 	@ - @head - 查询当前节点名称为head的子节点</pre>
     * <pre> 	# - #stop - 查询属性名id值为stop的标签节点</pre>
     * <pre> 	. - .stop - 查询属性名class值为stop的标签节点</pre>
     * <pre>	! - !name - 查询标签名不匹配相等的标签节点</pre>
     * <pre>	name - 查询标签名匹配相等的标签节点</pre>
     * <pre>	name[key] - 查询标签名并且属性名都匹配相等的标签节点</pre>
     * <pre>	name[key=value] - 查询标签名并且属性名和值都匹配相等的标签节点</pre>
     * <pre>	name[key!=value] - 查询标签名并且属性名和值都匹配且不包含匹配属性值的标签节点</pre>
     * <pre>	name[!key] - 查询标签名匹配且不包含匹配属性名标签节点</pre>
     * <pre>	name[!class|!src] - 管道符'|'连接多个属性筛选</pre>
     * </blockquote>
     *
     * @param cssQuery 查询规则
     * @return 查询结果
     */
    @NotNull
    public Elements select(String cssQuery) {
        var es = new Elements(this.es);
        for (var sb = new ParserStringBuilder(cssQuery); sb.stripLeading().site() < sb.length(); sb.offset(1)) {
            switch (sb.charAt()) {
                case '.' -> {
                    var value = sb.interceptOrEof(' ');
                    es = es.select(e -> e.containsAttr("class") && value.equals(e.attr("class")));
                }
                case '#' -> {
                    var value = sb.interceptOrEof(' ');
                    es = es.select(e -> e.containsAttr("id") && value.equals(e.attr("id")));
                }
                case '@' -> {
                    var value = sb.interceptOrEof(' ');
                    if (es.size() != 1) throw new IllegalStateException("参数 " + value + " 错误,查询对象不为Element类型");
                    es = es.get(0).childElements().stream().filter(l -> l.name().equals(value)).collect(Collectors.toCollection(Elements::new));
                }
                default -> {
                    var css = new StringBuilder();
                    var attrs = "";
                    while (sb.isNoOutBounds()) {
                        var c = sb.charAt();
                        if (c == '[') {
                            attrs = sb.intercept(']');
                            break;
                        } else if (c == ' ') {
                            break;
                        }
                        css.append(c);
                        sb.offset(1);
                    }
                    if (css.charAt(0) == '!') {
                        var name = css.substring(1);
                        if (attrs.isEmpty()) {
                            es = es.select(e -> !e.name().equals(name));
                        } else {
                            for (var attr : attrs.split("\\|")) {
                                int indexAttr = attr.indexOf("=");
                                if (indexAttr == -1) { // 不存在等号
                                    if (attr.startsWith("!")) {
                                        var thisAttr = attr.substring(1);
                                        es = thisAttr.isEmpty() ? es.select(e -> !e.name().equals(name) && e.attrIsEmpty()) : es.select(e -> !e.name().equals(name) && !e.containsAttr(thisAttr));
                                    } else {
                                        es = es.select(e -> !e.name().equals(name) && e.containsAttr(attr));
                                    }
                                } else {
                                    @SuppressWarnings("DuplicatedCode") var key = attr.substring(0, indexAttr);
                                    var value = attr.charAt(attr.length() - 1) == '\'' || attr.charAt(attr.length() - 1) == '"' ? attr.substring(indexAttr + 2, attr.length() - 1) : attr.substring(indexAttr + 1);
                                    if (key.endsWith("!")) {
                                        var thisKey = key.substring(0, key.length() - 1);
                                        es = es.select(e -> !e.name().equals(name) && e.containsAttr(thisKey) && !value.equals(e.attr(thisKey)));
                                    } else {
                                        es = es.select(e -> !e.name().equals(name) && e.containsAttr(key) && value.equals(e.attr(key)));
                                    }
                                }
                            }
                        }
                    } else {
                        var name = css.toString();
                        if (attrs.isEmpty()) {
                            es = es.select(e -> e.name().equals(name));
                        } else {
                            for (var attr : attrs.split("\\|")) {
                                int indexAttr = attr.indexOf("=");
                                if (indexAttr == -1) { // 不存在等号
                                    if (attr.startsWith("!")) {
                                        var thisAttr = attr.substring(1);
                                        es = thisAttr.isEmpty() ? es.select(e -> e.name().equals(name) && e.attrIsEmpty()) : es.select(e -> e.name().equals(name) && !e.containsAttr(thisAttr));
                                    } else {
                                        es = es.select(e -> e.name().equals(name) && e.containsAttr(attr));
                                    }
                                } else {
                                    @SuppressWarnings("DuplicatedCode") var key = attr.substring(0, indexAttr);
                                    var value = attr.charAt(attr.length() - 1) == '\'' || attr.charAt(attr.length() - 1) == '"' ? attr.substring(indexAttr + 2, attr.length() - 1) : attr.substring(indexAttr + 1);
                                    if (key.endsWith("!")) {
                                        var thisKey = key.substring(0, key.length() - 1);
                                        es = es.select(e -> e.name().equals(name) && e.containsAttr(thisKey) && !value.equals(e.attr(thisKey)));
                                    } else {
                                        es = es.select(e -> e.name().equals(name) && e.containsAttr(key) && value.equals(e.attr(key)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return es;
    }

}
