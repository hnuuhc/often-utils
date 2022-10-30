package org.haic.often.net;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.jetbrains.annotations.Contract;

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
	@Contract(pure = true)
	public static String random() {
		String param = "";
		switch (RandomUtils.nextInt(0, 13)) {
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
	@Contract(pure = true)
	public static String chrome() {
		String result = mozilla + " (";
		switch (RandomUtils.nextInt(0, 3)) {
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
	@Contract(pure = true)
	public static String fireFox() {
		String result = mozilla + " (";
		String version = RandomUtils.nextInt(0, 100) + ".0" + (twoSelectOne() ? "" : "esr");
		switch (RandomUtils.nextInt(0, 3)) {
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
	@Contract(pure = true)
	public static String edge() {
		return chrome() + edgTail();
	}

	/**
	 * Opera Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String opera() {
		return chrome() + oprTail();
	}

	/**
	 * Safari Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String safari() {
		return mozilla + " (" + macPlatform() + ")" + appleWebKitTail() + chromiumTail() + versionTail() + safari();
	}

	/**
	 * IE Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String ie() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + (twoSelectOne() ? "" : tridentHeader()) + ")";
	}

	/**
	 * 傲游浏览器 (Maxthon)
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String maxthon() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; Maxthon/" + RandomUtils.nextInt(2, 10) + " .0 " + ")";
	}

	/**
	 * 世界之窗 (The World)
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String theWorld() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; The World)";
	}

	/**
	 * Tencent TT Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String tt() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; TencentTraveler " + RandomUtils.nextInt(4, 10) + " .0 " + ")";
	}

	/**
	 * 360浏览器
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String threeSixZero() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; 360SE)";
	}

	/**
	 * Avent Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String avent() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + "; Avant Browser)";
	}

	/**
	 * UC Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String uc() {
		return mozilla + " (" + winPlatform() + ")" + appleWebKitTail() + chromiumTail() + ucTail() + safariTail();
	}

	/**
	 * SOGU Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String sogu() {
		return mozilla + " (" + compatibleHeader() + "; " + winPlatform() + tridentHeader() + "; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)";
	}

	@Contract(pure = true)
	private static String language() {
		return twoSelectOne() ? "" : "; " + language[RandomUtils.nextInt(0, language.length)];
	}

	@Contract(pure = true)
	private static String macPlatform() {
		return "Macintosh; Intel Mac OS X " + RandomUtils.nextInt(10, 20) + "_" + RandomUtils.nextInt(0, 20);
	}

	@Contract(pure = true)
	private static String winPlatform() {
		String result = winSystem[RandomUtils.nextInt(0, winSystem.length)];
		result += twoSelectOne() ? "; Win64" : "; WOW64";
		result += twoSelectOne() ? "" : "; x64";
		return result;
	}

	@Contract(pure = true)
	private static String linuxPlatform() {
		String result = "X11; ";
		result += twoSelectOne() ? "" : "U; ";
		result += twoSelectOne() ? "" : linuxSystem[RandomUtils.nextInt(0, linuxSystem.length)] + "; ";
		result += linuxCPU[RandomUtils.nextInt(0, linuxCPU.length)];
		result += twoSelectOne() ? "" : "; on x86_64";
		return result;
	}

	@Contract(pure = true)
	private static boolean twoSelectOne() {
		return Judge.isEmpty(RandomUtils.nextInt(0, 2));
	}

	@Contract(pure = true)
	private static String compatibleHeader() {
		return "compatible; MSIE " + RandomUtils.nextInt(5, 10) + ".0";
	}

	@Contract(pure = true)
	private static String tridentHeader() {
		return "; Trident/ " + RandomUtils.nextInt(4, 10) + ".0";
	}

	@Contract(pure = true)
	private static String uHeader() {
		return twoSelectOne() ? "" : "; U";
	}

	@Contract(pure = true)
	private static String geckoTail() {
		return " Gecko/" + RandomUtils.nextInt(2000, 2020) + "0101";
	}

	@Contract(pure = true)
	private static String versionTail() {
		return " Version/" + RandomUtils.nextInt(5, 20) + Symbol.DOT + RandomUtils.nextInt(0, 10);
	}

	@Contract(pure = true)
	private static String safariTail() {
		return " Safari/" + RandomUtils.nextInt(500, 600) + Symbol.DOT + RandomUtils.nextInt(10, 100);
	}

	@Contract(pure = true)
	private static String appleWebKitTail() {
		return " AppleWebKit/" + RandomUtils.nextInt(500, 800) + Symbol.DOT + RandomUtils.nextInt(10, 100) + " (KHTML, like Gecko)";
	}

	@Contract(pure = true)
	private static String chromiumTail() {
		return " Chrome/" + RandomUtils.nextInt(60, 100) + ".0." + RandomUtils.nextInt(1000, 10000) + Symbol.DOT + RandomUtils.nextInt(100, 1000);
	}

	@Contract(pure = true)
	private static String oprTail() {
		return " OPR/" + RandomUtils.nextInt(60, 100) + ".0." + RandomUtils.nextInt(1000, 10000) + Symbol.DOT + RandomUtils.nextInt(100, 1000);
	}

	@Contract(pure = true)
	private static String edgTail() {
		return " Edg/" + RandomUtils.nextInt(60, 100) + ".0." + RandomUtils.nextInt(1000, 10000) + Symbol.DOT + RandomUtils.nextInt(100, 1000);
	}

	@Contract(pure = true)
	private static String ucTail() {
		return " UBrowser/" + RandomUtils.nextInt(5, 10) + ".0." + RandomUtils.nextInt(1000, 10000) + Symbol.DOT + RandomUtils.nextInt(0, 1000);
	}

	@Contract(pure = true)
	private static String prestoTail() {
		return " Presto/" + RandomUtils.nextInt(2, 10) + Symbol.DOT + RandomUtils.nextInt(0, 10) + Symbol.DOT + RandomUtils.nextInt(100, 1000);
	}

	/**
	 * Get Phone of Random Browser UserAgent
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String randomAsPhone() {
		String result = "";
		switch (RandomUtils.nextInt(0, 5)) {
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
	@Contract(pure = true)
	public static String chromeAsPhone() {
		return mozilla + " (Linux" + androidHeader() + language() + ")" + appleWebKitTail() + chromiumTail() + " Mobile" + safariTail();
	}

	/**
	 * Phone of Safari Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String safariAsPhone() {
		String result = mozilla + " (";
		switch (RandomUtils.nextInt(0, 3)) {
			case 0 -> result += "iPhone" + uHeader() + "; CPU iPhone ";
			case 1 -> result += "iPod" + uHeader() + "; CPU iPhone ";
			case 2 -> result += "iPad" + uHeader() + "; CPU ";
		}
		result += "OS " + RandomUtils.nextInt(5, 20) + "_" + RandomUtils.nextInt(0, 10) + " like Mac OS X" + language() + ")" + appleWebKitTail() + versionTail() + mobileTail() + safariTail();
		return result;
	}

	/**
	 * Phone of Opera Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String operaAsPhone() {
		return "Opera/9.80 (Linux" + androidHeader() + "; Opera Mobi/build-" + RandomStringUtils.randomNumeric(10) + uHeader() + ")" + prestoTail() + versionTail();
	}

	/**
	 * Phone of QQ Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String qqAsPhone() {
		return "MQQBrowser/26 " + mozilla + " (Linux" + uHeader() + androidHeader() + language() + "; MB200 Build/" + RandomStringUtils.randomAlphanumeric(5).toUpperCase() + "; CyanogenMod-7)" + appleWebKitTail() + versionTail() + safariTail();
	}

	/**
	 * Phone of UC Browser
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true)
	public static String ucAsPhone() {
		return mozilla + " (" + compatibleHeader() + ") Opera/UCWEB7.0.2.37/28/999";
	}

	@Contract(pure = true)
	private static String androidHeader() {
		return "; Android " + RandomUtils.nextInt(5, 13) + ".0";
	}

	@Contract(pure = true)
	private static String mobileTail() {
		return " Mobile/ " + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
	}

}