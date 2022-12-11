package org.haic.often.net.download;

import org.haic.often.annotations.Contract;

/**
 * 下载方法枚举<br/> FILE - 配置文件下载<br/> FULL - 全量下载模式<br/> MULTITHREAD - 多线程模式<br/> MANDATORY - 强制多线程模式<br/> 如果下载文件的配置文件存在,将会自动跳转FILE模式,配置信息将会被文件中的配置覆盖
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/9/27 12:30
 */
public enum SionMethod {

	/**
	 * 通过配置文件下载,所有的下载配置都会被文件中配置信息覆盖
	 */
	FILE(true),
	/**
	 * 全量下载模式,不受多线程配置影响
	 */
	FULL(true),
	/**
	 * 分块多线程模式,按照pieceSize大小分块下载<br/> 如pieceSize大小为1M,文件大小为100M,那么文件将会被分为100个块下载
	 */
	PIECE(true),
	/**
	 * 经典多线程模式,按照pieceSize大小进行最小分块<br/> 如pieceSize大小为1M,线程为10,不到1M大小文件一个线程,那么跑满线程文件就需不低于10M
	 */
	MULTITHREAD(true),
	/**
	 * 强制多线程模式,不受pieceSize大小影响,无论文件大小,都将以满线程下载<br/> 极端条件下,如果文件大小过小(文件字节大小小于线程),会发生错误
	 */
	MANDATORY(true);

	private final boolean hasBody;

	SionMethod(boolean hasBody) {
		this.hasBody = hasBody;
	}

	/**
	 * 获得 枚举方法的值
	 *
	 * @return value
	 */
	@Contract(pure = true)
	public boolean hasBody() {
		return hasBody;
	}

}
