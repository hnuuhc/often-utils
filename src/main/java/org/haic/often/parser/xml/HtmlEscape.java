package org.haic.often.parser.xml;

import org.haic.often.util.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * Html转义枚举
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/8 5:01
 */
public enum HtmlEscape {

    /**
     * 欧元符号
     */
    euro(8364),
    /**
     * 左箭头
     */
    larr(8592),
    /**
     * 上箭头
     */
    uarr(8593),
    /**
     * 右箭头
     */
    rarr(8594),
    /**
     * 下箭头
     */
    darr(8595),
    /**
     * 左和右箭头
     */
    harr(8596),
    /**
     * 回车符号
     */
    crarr(8629),
    /**
     * 左单角引号
     */
    lsaquo(8249),
    /**
     * 右单角引号
     */
    rsaquo(8250),
    /**
     * 着重号
     */
    bull(8226),
    /**
     * 省略号
     */
    hellip(8230),
    /**
     * 短破折号
     */
    ndash(8211),
    /**
     * 长破折号
     */
    mdash(8212),
    /**
     * 半角空格
     */
    ensp(8194),
    /**
     * 全角空格
     */
    emsp(8195),
    /**
     * 窄空格
     */
    thinsp(8201),
    /**
     * 商标
     */
    trade(8482),

    /**
     * 带沉音符的 A
     */
    Agrave(192),
    /**
     * 带重音符的 A
     */
    Aacute(193),
    /**
     * 带抑扬音符的 A
     */
    Acirc(194),
    /**
     * 带腭化符的 A
     */
    Atilde(195),
    /**
     * 带变音符的 A
     */
    Auml(196),
    /**
     * 带圆圈的 A
     */
    Aring(197),
    /**
     * 大写 AE 符号
     */
    AElig(198),
    /**
     * 带变音符的 C
     */
    Ccedil(199),
    /**
     * 带沉音符的 E
     */
    Egrave(200),
    /**
     * 带重音符的 E
     */
    Eacute(201),
    /**
     * 带抑扬音符的 E
     */
    Ecirc(202),
    /**
     * 带变音符的 E
     */
    Euml(203),
    /**
     * 带沉音符的 I
     */
    Igrave(204),
    /**
     * 带重音符的 I
     */
    Iacute(205),
    /**
     * 带抑扬音符的 I
     */
    Icirc(206),
    /**
     * 带变音符的 I
     */
    Iuml(207),
    /**
     * Capital eth（冰岛语）
     */
    ETH(208),
    /**
     * 带腭化符的 N
     */
    Ntilde(209),
    /**
     * 带沉音符的 O
     */
    Ograve(210),
    /**
     * 带重音符的 O
     */
    Oacute(211),
    /**
     * 带抑扬音符的 O
     */
    Ocirc(212),
    /**
     * 带腭化符的 O
     */
    Otilde(213),
    /**
     * 带变音符的 O
     */
    Ouml(214),
    /**
     * 带斜线的大写 O
     */
    Oslash(216),
    /**
     * 带沉音符的 U
     */
    Ugrave(217),
    /**
     * 带重音符的 U
     */
    Uacute(218),
    /**
     * 带抑扬音符的 U
     */
    Ucirc(219),
    /**
     * 带变音符的 U
     */
    Uuml(220),
    /**
     * 带重音符的 Y
     */
    Yacute(221),
    /**
     * Capital thorn（冰岛语）
     */
    THORN(222),
    /**
     * Lowercase sharp s (German)
     */
    szlig(223),
    /**
     * 带沉音符的 a
     */
    agrave(224),
    /**
     * 带重音符的 a
     */
    aacute(225),
    /**
     * 带抑扬音符的 a
     */
    acirc(226),
    /**
     * 带腭化符的 a
     */
    atilde(227),
    /**
     * 带变音符的 a
     */
    auml(228),
    /**
     * 带圆圈的 a
     */
    aring(229),
    /**
     * 小写 ae 符号
     */
    aelig(230),
    /**
     * 带变音符的 C
     */
    ccedil(231),
    /**
     * 带沉音符的 e
     */
    egrave(232),
    /**
     * 带重音符的 e
     */
    eacute(233),
    /**
     * 带抑扬音符的 e
     */
    ecirc(234),
    /**
     * 带变音符的 e
     */
    euml(235),
    /**
     * 带沉音符的 i
     */
    igrave(236),
    /**
     * 带重音符的 i
     */
    iacute(237),
    /**
     * 带抑扬音符的 i
     */
    icirc(238),
    /**
     * 带变音符的 i
     */
    iuml(239),
    /**
     * Lowercase eth（冰岛语）
     */
    eth(240),
    /**
     * 带腭化符的 n
     */
    ntilde(241),
    /**
     * 带沉音符的 o
     */
    ograve(242),
    /**
     * 带重音符的 o
     */
    oacute(243),
    /**
     * 带抑扬音符的 o
     */
    ocirc(244),
    /**
     * 带腭化符的 o
     */
    otilde(245),
    /**
     * 带变音符的 o
     */
    ouml(246),
    /**
     * 带斜线的小写 o
     */
    oslash(248),
    /**
     * 带沉音符的 u
     */
    ugrave(249),
    /**
     * 带重音符的 u
     */
    uacute(250),
    /**
     * 带抑扬音符的 u
     */
    ucirc(251),
    /**
     * 带变音符的 u
     */
    uuml(252),
    /**
     * 带重音符的 y
     */
    yacute(253),
    /**
     * Lowercase thorn（冰岛语）
     */
    thorn(254),
    /**
     * 带变音符的 y
     */
    yuml(255),

    /**
     * 倒置感叹号
     */
    iexcl(161),
    /**
     * 美分
     */
    cent(162),
    /**
     * 英镑
     */
    pound(163),
    /**
     * 货币
     */
    curren(164),
    /**
     * 日元
     */
    yen(165),
    /**
     * 已截断的竖线
     */
    brvbar(166),
    /**
     * 区块符号
     */
    sect(167),
    /**
     * 间距分音符
     */
    uml(168),
    /**
     * 版权符号
     */
    copy(169),
    /**
     * 阴性序数记号
     */
    ordf(170),
    /**
     * 开口/直角引号
     */
    laquo(171),
    /**
     * 否定符号
     */
    not(172),
    /**
     * 软连字符
     */
    shy(173),
    /**
     * 注册商标
     */
    reg(174),
    /**
     * 长音符号
     */
    macr(175),
    /**
     * 度
     */
    deg(176),
    /**
     * 加减符号
     */
    plusmn(177),
    /**
     * 上标 2
     */
    sup2(178),
    /**
     * 上标 3
     */
    sup3(179),
    /**
     * 尖音符号
     */
    acute(180),
    /**
     * 微米
     */
    micro(181),
    /**
     * 段落符号
     */
    para(182),
    /**
     * 变音符号
     */
    cedil(184),
    /**
     * 上标 1
     */
    sup1(185),
    /**
     * 阳性序数记号
     */
    ordm(186),
    /**
     * 闭合/直角引号
     */
    raquo(187),
    /**
     * 分数 1/4
     */
    frac14(188),
    /**
     * 分数 1/2
     */
    frac12(189),
    /**
     * 分数 3/4
     */
    frac34(190),
    /**
     * 倒置问号
     */
    iquest(191),
    /**
     * 乘号
     */
    times(215),
    /**
     * 除号
     */
    divide(247),
    /**
     * 空格
     */
    nbsp(32),
    /**
     * 感叹号
     */
    excl(33),
    /**
     * 双引号
     */
    quot(34),
    /**
     * 井号
     */
    num(35),
    /**
     * 美元符号
     */
    dollar(36),
    /**
     * 百分号
     */
    percnt(37),
    /**
     * 连接符号、and 操作符
     */
    amp(38),
    /**
     * 单引号
     */
    apos(39),
    /**
     * 左括号
     */
    lpar(40),
    /**
     * 右括号
     */
    rpar(41),
    /**
     * 星号
     */
    ast(42),
    /**
     * 加号
     */
    plus(43),
    /**
     * 逗号
     */
    comma(44),
    /**
     * 句点
     */
    period(46),
    /**
     * 斜线
     */
    sol(47),
    /**
     * 冒号
     */
    colon(58),
    /**
     * 分号
     */
    semi(59),
    /**
     * 小于符号
     */
    lt(60),
    /**
     * 等号
     */
    equals(61),
    /**
     * 大于符号
     */
    gt(62),
    /**
     * 问号
     */
    quest(63),
    /**
     * AT 符号
     */
    commat(64),
    /**
     * 左中括号
     */
    lsqb(91),
    /**
     * 反斜线
     */
    bsol(92),
    /**
     * 右中括号
     */
    rsqb(93),
    /**
     * 脱字符号
     */
    Hat(94),
    /**
     * 下划线
     */
    lowbar(95),
    /**
     * 沉音符
     */
    grave(96),
    /**
     * 左大括号
     */
    lcub(123),
    /**
     * 竖线
     */
    verbar(124),
    /**
     * 右大括号
     */
    rcub(125),
    /**
     * 全部
     */
    forall(8704),
    /**
     * 一部分
     */
    part(8706),
    /**
     * 已存在
     */
    exist(8707),
    /**
     * 空集
     */
    empty(8709),
    /**
     * 微分
     */
    nabla(8711),
    /**
     * 是否属于
     */
    isin(8712),
    /**
     * 是否不属于
     */
    notin(8713),
    /**
     * Ni
     */
    ni(8715),
    /**
     * 乘积
     */
    prod(8719),
    /**
     * 求和
     */
    sum(8721),
    /**
     * 减号
     */
    minus(8722),
    /**
     * 星号
     */
    lowast(8727),
    /**
     * 平方根
     */
    radic(8730),
    /**
     * 与...成比例
     */
    prop(8733),
    /**
     * 无限大
     */
    infin(8734),
    /**
     * 角度
     */
    ang(8736),
    /**
     * 和
     */
    and(8743),
    /**
     * 或
     */
    or(8744),
    /**
     * 交集
     */
    cap(8745),
    /**
     * 并集
     */
    cup(8746),
    /**
     * 所以
     */
    there4(8756),
    /**
     * 相似
     */
    sim(8764),
    /**
     * 全等
     */
    cong(8773),
    /**
     * 约等于
     */
    asymp(8776),
    /**
     * 不等于
     */
    ne(8800),
    /**
     * 等同于
     */
    equiv(8801),
    /**
     * 小于等于
     */
    le(8804),
    /**
     * 大于等于
     */
    ge(8805),
    /**
     * （集合）小于
     */
    sub(8834),
    /**
     * （集合）大于
     */
    sup(8835),
    /**
     * （集合）不小于
     */
    nsub(8836),
    /**
     * （集合）小于等于
     */
    sube(8838),
    /**
     * （集合）大于等于
     */
    supe(8839),
    /**
     * 带圆圈的加号
     */
    oplus(8853),
    /**
     * 带圆圈的乘号
     */
    otimes(8855),
    /**
     * 正交
     */
    perp(8869),
    /**
     * 点操作符
     */
    sdot(8901),
    /**
     * Alpha
     */
    Alpha(913),
    /**
     * Beta
     */
    Beta(914),
    /**
     * Gamma
     */
    Gamma(915),
    /**
     * Delta
     */
    Delta(916),
    /**
     * Epsilon
     */
    Epsilon(917),
    /**
     * Zeta
     */
    Zeta(918),
    /**
     * Eta
     */
    Eta(919),
    /**
     * Theta
     */
    Theta(920),
    /**
     * Iota
     */
    Iota(921),
    /**
     * Kappa
     */
    Kappa(922),
    /**
     * Lambda
     */
    Lambda(923),
    /**
     * Mu
     */
    Mu(924),
    /**
     * Nu
     */
    Nu(925),
    /**
     * Xi
     */
    Xi(926),
    /**
     * Omicron
     */
    Omicron(927),
    /**
     * Pi
     */
    Pi(928),
    /**
     * Rho
     */
    Rho(929),
    /**
     * Sigma
     */
    Sigma(931),
    /**
     * Tau
     */
    Tau(932),
    /**
     * Upsilon
     */
    Upsilon(933),
    /**
     * Phi
     */
    Phi(934),
    /**
     * Chi
     */
    Chi(935),
    /**
     * Psi
     */
    Psi(936),
    /**
     * Omega
     */
    Omega(937),
    /**
     * alpha
     */
    alpha(945),
    /**
     * beta
     */
    beta(946),
    /**
     * gamma
     */
    gamma(947),
    /**
     * delta
     */
    delta(948),
    /**
     * epsilon
     */
    epsilon(949),
    /**
     * zeta
     */
    zeta(950),
    /**
     * eta
     */
    eta(951),
    /**
     * theta
     */
    theta(952),
    /**
     * iota
     */
    iota(953),
    /**
     * kappa
     */
    kappa(954),
    /**
     * lambda
     */
    lambda(955),
    /**
     * mu
     */
    mu(956),
    /**
     * nu
     */
    nu(957),
    /**
     * xi
     */
    xi(958),
    /**
     * omicron
     */
    omicron(959),
    /**
     * pi
     */
    pi(960),
    /**
     * rho
     */
    rho(961),
    /**
     * sigmaf
     */
    sigmaf(962),
    /**
     * sigma
     */
    sigma(963),
    /**
     * tau
     */
    tau(964),
    /**
     * upsilon
     */
    upsilon(965),
    /**
     * phi
     */
    phi(966),
    /**
     * chi
     */
    chi(967),
    /**
     * psi
     */
    psi(968),
    /**
     * omega
     */
    omega(969),
    /**
     * Theta symbol
     */
    thetasym(977),
    /**
     * Upsilon symbol
     */
    upsih(978),
    /**
     * Pi symbol
     */
    piv(982),
    /**
     * 连字 OE（大写）
     */
    OElig(338),
    /**
     * 连字 oe（小写）
     */
    oelig(339),
    /**
     * 带声调的 S（大写）
     */
    Scaron(352),
    /**
     * 带声调的 s（小写）
     */
    scaron(353),
    /**
     * 带分音符号的 Y
     */
    Yuml(376),
    /**
     * 函数符号（小写）
     */
    fnof(402),
    /**
     * 抑扬音符号
     */
    circ(710),
    /**
     * 腭化符号
     */
    tilde(732),
    /**
     * 零宽度非连接
     */
    zwnj(8204),
    /**
     * 零宽度连接
     */
    zwj(8205),
    /**
     * 从左到右标记
     */
    lrm(8206),
    /**
     * 从右到左标记
     */
    rlm(8207),
    /**
     * 左单引号
     */
    lsquo(8216),
    /**
     * 右单引号
     */
    rsquo(8217),
    /**
     * low-9 单引号
     */
    sbquo(8218),
    /**
     * 左双引号
     */
    ldquo(8220),
    /**
     * 右双引号
     */
    rdquo(8221),
    /**
     * low-9 双引号
     */
    bdquo(8222),
    /**
     * 匕首符号
     */
    dagger(8224),
    /**
     * 双匕首符号
     */
    Dagger(8225),
    /**
     * 千分号
     */
    permil(8240),
    /**
     * 分钟（度）
     */
    prime(8242),
    /**
     * 秒（度）
     */
    Prime(8243),
    /**
     * 上划线
     */
    oline(8254),
    /**
     * Left ceiling
     */
    lceil(8968),
    /**
     * Right ceiling
     */
    rceil(8969),
    /**
     * Left floor
     */
    lfloor(8970),
    /**
     * Right floor
     */
    rfloor(8971),
    /**
     * 菱形
     */
    loz(9674),
    /**
     * 黑桃
     */
    spades(9824),
    /**
     * 梅花
     */
    clubs(9827),
    /**
     * 红心
     */
    hearts(9829),
    /**
     * 方块（钻石）
     */
    diams(9830);

    private final int c;

    HtmlEscape(int c) {
        this.c = c;
    }

    public final char getValue() {
        return (char) c;
    }

    /**
     * 反转义当前转义字符,如果为未知转义符,则不做转义处理
     *
     * @param s 转义字符
     * @return 反转义后的字符
     */
    public static char unescape(@NotNull String s) {
        Validate.isTrue(s.startsWith("&") && s.endsWith(";"), "转义字符串格式不正确");
        return s.charAt(1) == '#' ? (char) (s.charAt(2) == 'x' ? Integer.parseInt(s.substring(3, s.length() - 1), 16) : Integer.parseInt(s.substring(2, s.length() - 1))) : HtmlEscape.valueOf(s.substring(1, s.length() - 1)).getValue();
    }

    /**
     * 获取当前字符转义字符串,如果不存在对应值,则返回其本身
     *
     * @param c 字符
     * @return 转义后的字符串
     */
    public static String escape(char c) {
        for (var value : HtmlEscape.values()) if (value.getValue() == c) return '&' + value.name() + ';';
        return String.valueOf(c);
    }

}
