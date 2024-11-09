package org.haic.often.parser.xml;

import org.haic.often.parser.ParserStringBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * 这是一个html和xml解析器,使用方法为 Document doc = Document.parse(String)
 * <p>
 * 重要警告: 由于格式化时自动完成转义,已经格式化的文本不可再次被解析
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Document extends Element {

    private final String type;

    /**
     * 解析html文档
     *
     * @param body 数据
     * @return 文档
     */
    public static Document parse(String body) {
        return parse(body, true);
    }

    /**
     * 解析html或xml文档
     *
     * @param body   数据
     * @param isHtml 是否为html格式,如果文档顶部存在类型,将会自动判断覆盖此参数
     * @return 文档
     */
    public static Document parse(String body, boolean isHtml) {
        if (body == null) return null;
        var sb = new ParserStringBuilder(body).strip();
        if (sb.startsWith("\uFEFF")) sb.offset(1).stripLeading(); // 去除特殊符号
        while (sb.startsWith("<!--")) sb.site(sb.indexOf("-->", sb.site() + 4) + 3); // 去除注释
        if (sb.stripLeading().startsWith("<!")) {
            var typeTail = sb.indexOf(">", sb.site() + 2) + 1;
            var type = sb.substring(sb.site(), typeTail);
            sb.site(typeTail); // 更新位置
            while (sb.stripLeading().startsWith("<!--")) sb.site(sb.indexOf("-->", sb.site() + 4) + 3); // 去除注释
            return new Document(type, sb, true);
        } else if (sb.startsWith("<?")) {
            var typeTail = sb.indexOf(">", sb.site() + 2) + 1;
            var type = sb.substring(sb.site(), typeTail);
            sb.site(typeTail); // 更新位置
            while (sb.stripLeading().startsWith("<!--")) sb.site(sb.indexOf("-->", sb.site() + 4) + 3); // 去除注释
            return new Document(type, sb, false);
        } else {
            if (isHtml) {
                if (sb.startsWith("<html")) {
                    return new Document("", sb, true);
                } else if (sb.startsWith("<head")) {
                    return new Document("", new ParserStringBuilder("<html>" + sb + "<body></body></html>"), true);
                } else if (sb.startsWith("<body")) {
                    return new Document("", new ParserStringBuilder("<html><head></head>" + sb + "</html>"), true);
                } else {
                    return new Document("", new ParserStringBuilder("<html><head></head><body>" + sb + "</body></html>"), true);
                }
            } else {
                if (!sb.startsWith("<")) throw new IllegalStateException("在索引 " + sb.site() + " 处未找到到起始符'<'");
                return new Document("", sb, false);
            }
        }

    }

    private Document(@NotNull String type, @NotNull ParserStringBuilder node, boolean isHtml) {
        super(null, node);
        this.type = type;
        Element tree = this;
        for (node.offset(1); node.stripLeading().isNoOutBounds() && tree != null; node.offset(1)) {
            int start = node.stripLeading().site(); // 记录初始位置
            int tagHeadIndex = node.indexOf("<"); // 获取标签初始位置

            var child = new Element(tree, node.site(tagHeadIndex));
            while (node.charAt() == '<') { // 修正错误标签
                tagHeadIndex = node.site();
                child = new Element(tree, node);
            }

            var text = Document.unescape(node.substring(start, tagHeadIndex).stripTrailing()).strip();
            if (!text.isEmpty()) tree.addChild(text);  // 提前写入文本,防止结束返回

            if (node.charAt() == '/') { // 结束标签返回,允许多级返回
                var name = node.offset(1).substring(node.site(), node.site(node.indexOf(">")).site());
                for (var e = tree; e != null; e = e.parent()) {
                    if (name.equalsIgnoreCase(e.name())) {
                        tree = e.parent();
                        break;
                    }
                }
                continue;
            }

            if (node.charAt() == '!') { // 注释
                var notestart = tagHeadIndex + 4;
                var noteend = node.indexOf("-->", notestart);
                tree.addChild(new XmlNote(node.substring(notestart, noteend).strip()));
                node.site(noteend + 2);
                continue;
            }

            if (isHtml) {  // 可能不规范的标签,需要处理
                switch (child.name()) { // html特殊标签处理后返回
                    case "a", "p" -> {   // 可能不规范的标签,需要排序处理
                        if (child.name().equals(tree.name())) {
                            tree = tree.parent();
                            tree.addChild(child);
                            continue;
                        }
                    }
                    // 自闭合标签
                    case "hr", "br", "input", "meta", "link", "img", "area", "base", "col", "command", "embed", "keygen", "param", "source", "track", "wbr", "feflood", "feblend", "feoffset",
                         "fegaussianblur", "fecomposite", "fecolormatrix", "lineargradient", "radialgradient" -> {
                        tree.addChild(child.close(true));
                        continue;
                    }
                    // 文本标签
                    case "textarea", "script", "style" -> {
                        // if (tree.name().equals("div")) tree = tree.parent(); // 异常位置
                        int index = node.offset(1).indexOf("</" + child.name() + ">");
                        if (index == -1) index = node.indexOf("</" + child.name().toUpperCase() + ">");
                        var s = node.substring(node.site(), index).strip();
                        if (s.startsWith("\"") && s.endsWith("\"")) s = new ParserStringBuilder(s).intercept();
                        if (!s.isEmpty()) child.addChild(s);
                        tree.addChild(child);
                        node.site(index + child.name().length() + 2);
                        continue;
                    }
                }
            }

            tree.addChild(child);
            if (!child.isClose()) tree = child; // 非自闭合标签,进入下级
        }
        if (isHtml && node.stripLeading().site() < node.length()) { // 不规范的网页可能存在越界标签,即html结束标签后仍然存在标签
            var outbounds = new ParserStringBuilder("<body>" + node.substring(node.site(), node.length()) + "</body>");
            var body = this.selectFirst("@body"); // 修正为body标签子元素
            if (body == null) body = this; // 不规范网页可能不存在body
            for (var child : new Document("", outbounds, true).childs()) {
                if (child instanceof Element e) body.addChild(e);
            }
        }
    }

    /**
     * 返回html文档的html.head元素,如果不存在body,则会发生异常
     *
     * @return BODY元素
     */
    public Element head() {
        return (Element) this.childs().stream().filter(l -> l instanceof XmlTree e && e.name().equals("head")).findFirst().orElseThrow();
    }

    /**
     * 返回html文档的html.body元素,如果不存在body,则会发生异常
     *
     * @return BODY元素
     */
    public Element body() {
        return (Element) this.childs().stream().filter(l -> l instanceof XmlTree e && e.name().equals("body")).findFirst().orElseThrow();
    }

    /**
     * 反转义当前字符串,如果存在未知转义符,则不做转义处理
     *
     * @param s 待反转义字符串
     * @return 反转义后的字符串
     */
    public static String unescape(String s) {
        if (s == null) return null;
        var sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '&') {
                int index = s.indexOf(";", i + 1);
                if (index == -1) return sb.append(s.substring(i)).toString();
                var escape = s.substring(i, (i = index) + 1);
                try {
                    sb.append(HtmlEscape.unescape(escape));
                } catch (IllegalArgumentException e) {
                    sb.append(escape);
                }
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    /**
     * 转义当前字符串
     *
     * @param s 待转义的字符串
     * @return 转义后的字符串
     */
    public static String escape(@NotNull String s) {
        var sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) sb.append(Character.isLetterOrDigit(s.charAt(i)) ? s.charAt(i) : HtmlEscape.escape(s.charAt(i)));
        return sb.toString();
    }

    @Override
    public String toString() {
        return type.isEmpty() ? super.toString() : type + "\n" + super.toString();
    }

}
