package org.haic.often.net;

import org.haic.often.util.RandomUtil;

/**
 * UserAgent工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/3/7 20:18
 */
public class UserAgent {

	private static final String mozilla = "Mozilla/5.0";
	private static final String[] winSystem = { "Windows NT 11.0", "Windows NT 10.0", "Windows NT 8.1", "Windows NT 8.0", "Windows NT 7.0", "windows NT 6.2", "Windows NT 6.1", "Windows NT 6.0", "Windows NT 5.2", "Windows NT 5.1", "Windows NT 5.0", "Windows ME", "Windows 98" };
	private static final String[] linuxSystem = { "Ubuntu", "Manjaro Linux", "Pop!_OS", "Deepin", "Debian GNU/Linux", "elementary OS" };
	private static final String[] linuxCPU = { "Linux x86_64", "Linux i686", "Linux ppc64", "Linux ppc" };
	private static final String[] language = { "en-us", "zh-cn", "en-GB" };

	/**
	 * Get Random Browser UserAgent
	 *
	 * @return Random UserAgent
	 */
	public static String random() {
		String param = "";
		switch (RandomUtil.nextInt(0, 13)) {
			case 0 -> param = chrome();
			case 1 -> param = safari();
			case 2 -> param = fireFox();
			case 3 -> param = edge();
			case 4 -> param = opera();
			case 5 -> param = ie();
			case 6 -> param = maxthon();
			case 7 -> param = theWorld();
			case 8 -> param = tt();
			case 9 -> param = threeSixZero();
			case 10 -> param = avent();
			case 11 -> param = uc();
			case 12 -> param = sogu();
		}
		return param;
	}

	/**
	 * Chrome Browser
	 *
	 * @return Random UserAgent
	 */
	public static String chrome() {
		String result = mozilla + " (";
		switch (RandomUtil.nextInt(0, 3)) {
			case 0 -> result += winPlatform() + ")"; // windows
			case 1 -> result += macPlatform() + ")";  // mac
			case 2 -> result += linuxPlatform() + ")"; // linux
		}
		result += appleWebKitTail() + chromiumTail() + safariTail();
		return result;
	}

	/**
	 * FireFox Browser
	 *
	 * @return Random UserAgent
	 */
	public static String fireFox() {
		String result = mozilla + " (";
		String version = RandomUtil.nextInt(0, 100) + ".0" + (twoSelectOne() ? "" : "esr");
		switch (RandomUtil.nextInt(0, 3)) {
			case 0 -> result += winPlatform() + "; rv:" + version + ")";  // windows
			case 1 -> result += macPlatform() + "; rv:" + version + ")"; // mac
			case 2 -> result += linuxPlatform() + "; rv:" + version + ")"; // linux
		}
		result += geckoTail() + " Firefox/" + version;
		return result;
	}

	/**
	 * Edge Browser
	 *
	 * @return Random UserAgent
	 */
	public static String edge() {
		return chrome() + edgTail();
	}

	/**
	 * Opera Browser
	 *
	 * @return Random UserAgent
	 */
	public static String opera() {
		return chrome() + oprTail();
	}

	/**
	 * Safari Browser
	 *
	 * @return Random UserAgent
	 */
	public static String safari() {
		return mozilla + " (" + macPlatform() + ")" + appleWebKitTail() + chromiumTail() + versionTail() + safari();
	}

	/**
	 * IE Browser
	 *
	 * @return Random UserAgent
	 */
	public static String ie() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + (twoSelectOne() ? "" : tridentHeader()) + ")";
	}

	/**
	 * 傲游浏览器 (Maxthon)
	 *
	 * @return Random UserAgent
	 */
	public static String maxthon() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; Maxthon/" + RandomUtil.nextInt(2, 10) + " .0 " + ")";
	}

	/**
	 * 世界之窗 (The World)
	 *
	 * @return Random UserAgent
	 */
	public static String theWorld() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; The World)";
	}

	/**
	 * Tencent TT Browser
	 *
	 * @return Random UserAgent
	 */
	public static String tt() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; TencentTraveler " + RandomUtil.nextInt(4, 10) + " .0 " + ")";
	}

	/**
	 * 360 浏览器
	 *
	 * @return Random UserAgent
	 */
	public static String threeSixZero() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; 360SE)";
	}

	/**
	 * Avent Browser
	 *
	 * @return Random UserAgent
	 */
	public static String avent() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; Avant Browser)";
	}

	/**
	 * UC Browser
	 *
	 * @return Random UserAgent
	 */
	public static String uc() {
		return mozilla + " (" + winPlatform() + ")" + appleWebKitTail() + chromiumTail() + ucTail() + safariTail();
	}

	/**
	 * SOGU Browser
	 *
	 * @return Random UserAgent
	 */
	public static String sogu() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + tridentHeader() + "; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)";
	}

	private static String language() {
		return twoSelectOne() ? "" : "; " + language[RandomUtil.nextInt(0, language.length)];
	}

	private static String macPlatform() {
		return "Macintosh; Intel Mac OS X " + RandomUtil.nextInt(10, 20) + "_" + RandomUtil.nextInt(0, 20);
	}

	private static String winPlatform() {
		String result = winSystem[RandomUtil.nextInt(0, winSystem.length)];
		result += twoSelectOne() ? "; Win64" : "; WOW64";
		result += twoSelectOne() ? "" : "; x64";
		return result;
	}

	private static String linuxPlatform() {
		String result = "X11; ";
		result += twoSelectOne() ? "" : "U; ";
		result += twoSelectOne() ? "" : linuxSystem[RandomUtil.nextInt(0, linuxSystem.length)] + "; ";
		result += linuxCPU[RandomUtil.nextInt(0, linuxCPU.length)];
		result += twoSelectOne() ? "" : "; on x86_64";
		return result;
	}

	private static boolean twoSelectOne() {
		return RandomUtil.nextInt(0, 2) == 0;
	}

	private static String compatibleHeader() {
		return "compatible; MSIE " + RandomUtil.nextInt(5, 10) + ".0";
	}

	private static String tridentHeader() {
		return "; Trident/ " + RandomUtil.nextInt(4, 10) + ".0";
	}

	private static String uHeader() {
		return twoSelectOne() ? "" : "; U";
	}

	private static String geckoTail() {
		return " Gecko/" + RandomUtil.nextInt(2000, 2020) + "0101";
	}

	private static String versionTail() {
		return " Version/" + RandomUtil.nextInt(5, 20) + "." + RandomUtil.nextInt(0, 10);
	}

	private static String safariTail() {
		return " Safari/" + RandomUtil.nextInt(500, 600) + "." + RandomUtil.nextInt(10, 100);
	}

	private static String appleWebKitTail() {
		return " AppleWebKit/" + RandomUtil.nextInt(500, 800) + "." + RandomUtil.nextInt(10, 100) + " (KHTML, like Gecko)";
	}

	private static String chromiumTail() {
		return " Chrome/" + RandomUtil.nextInt(60, 100) + ".0." + RandomUtil.nextInt(1000, 10000) + "." + RandomUtil.nextInt(100, 1000);
	}

	private static String oprTail() {
		return " OPR/" + RandomUtil.nextInt(60, 100) + ".0." + RandomUtil.nextInt(1000, 10000) + "." + RandomUtil.nextInt(100, 1000);
	}

	private static String edgTail() {
		return " Edg/" + RandomUtil.nextInt(60, 100) + ".0." + RandomUtil.nextInt(1000, 10000) + "." + RandomUtil.nextInt(100, 1000);
	}

	private static String ucTail() {
		return " UBrowser/" + RandomUtil.nextInt(5, 10) + ".0." + RandomUtil.nextInt(1000, 10000) + "." + RandomUtil.nextInt(0, 1000);
	}

	private static String prestoTail() {
		return " Presto/" + RandomUtil.nextInt(2, 10) + "." + RandomUtil.nextInt(0, 10) + "." + RandomUtil.nextInt(100, 1000);
	}

	/**
	 * Get Phone of Random Browser UserAgent
	 *
	 * @return Random UserAgent
	 */
	public static String randomAsPhone() {
		String result = "";
		switch (RandomUtil.nextInt(0, 5)) {
			case 0 -> result = chromeAsPhone();
			case 1 -> result = safariAsPhone();
			case 2 -> result = operaAsPhone();
			case 3 -> result = qqAsPhone();
			case 4 -> result = ucAsPhone();
		}
		return result;
	}

	/**
	 * Phone of Chrome Browser
	 *
	 * @return Random UserAgent
	 */
	public static String chromeAsPhone() {
		return mozilla + " (Linux" + androidHeader() + language() + ")" + appleWebKitTail() + chromiumTail() + " Mobile" + safariTail();
	}

	/**
	 * Phone of Safari Browser
	 *
	 * @return Random UserAgent
	 */
	public static String safariAsPhone() {
		String result = mozilla + " (";
		switch (RandomUtil.nextInt(0, 3)) {
			case 0 -> result += "iPhone" + uHeader() + "; CPU iPhone ";
			case 1 -> result += "iPod" + uHeader() + "; CPU iPhone ";
			case 2 -> result += "iPad" + uHeader() + "; CPU ";
		}
		result += "OS " + RandomUtil.nextInt(5, 20) + "_" + RandomUtil.nextInt(0, 10) + " like Mac OS X" + language() + ")" + appleWebKitTail() + versionTail() + mobileTail() + safariTail();
		return result;
	}

	/**
	 * Phone of Opera Browser
	 *
	 * @return Random UserAgent
	 */
	public static String operaAsPhone() {
		return "Opera/9.80 (Linux" + androidHeader() + "; Opera Mobi/build-" + RandomUtil.randomNumeric(10) + uHeader() + ")" + prestoTail() + versionTail();
	}

	/**
	 * Phone of QQ Browser
	 *
	 * @return Random UserAgent
	 */
	public static String qqAsPhone() {
		return "MQQBrowser/26 " + mozilla + " (Linux" + uHeader() + androidHeader() + language() + "; MB200 Build/" + RandomUtil.randomAlphanumeric(5).toUpperCase() + "; CyanogenMod-7)" + appleWebKitTail() + versionTail() + safariTail();
	}

	/**
	 * Phone of UC Browser
	 *
	 * @return Random UserAgent
	 */
	public static String ucAsPhone() {
		return mozilla + " (" + compatibleHeader() + ") Opera/UCWEB7.0.2.37/28/999";
	}

	private static String androidHeader() {
		return "; Android " + RandomUtil.nextInt(5, 13) + ".0";
	}

	private static String mobileTail() {
		return " Mobile/ " + RandomUtil.randomAlphanumeric(6).toUpperCase();
	}

}
