package org.haic.often.net.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamListener;
import org.jetbrains.annotations.NotNull;
import org.haic.often.exception.DownloadException;
import org.haic.often.net.URIUtil;
import org.haic.often.util.FileUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Https 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/3/9 14:26
 */
public class FTPUtil {

	private FTPUtil() {
	}

	public static FTPConnection connect(@NotNull String host) {
		return connect(host, 21);
	}

	public static FTPConnection connect(@NotNull String host, int port) {
		return connect(host, port, "anonymous", "");
	}

	public static FTPConnection connect(@NotNull String host, int port, @NotNull String user, @NotNull String passwd) {
		return new Connection(host, port, user, passwd);
	}

	private static class Connection extends FTPConnection {
		private final String host;
		private final int port;
		private final String user;
		private final String passwd;
		private final FTPClient ftpClient = new FTPClient();

		private Connection(@NotNull String host, int port, @NotNull String user, @NotNull String passwd) {
			this.host = host;
			this.port = port;
			this.user = user;
			this.passwd = passwd;
			ftpClient.enterLocalPassiveMode(); //设置被动模式传输
			ftpClient.setConnectTimeout(2000);
			ftpClient.setBufferSize(8192);
		}

		public Connection localPassiveMode() {
			ftpClient.enterLocalPassiveMode();
			return this;
		}

		public Connection remotePassiveMode() {
			try {
				ftpClient.enterRemotePassiveMode();
			} catch (IOException e) {
				// e.printStackTrace();
			}
			return this;
		}

		public Connection timeout(int millis) {
			ftpClient.setConnectTimeout(millis);
			return this;
		}

		public Connection bufferSize(int bufferSize) {
			ftpClient.setBufferSize(bufferSize);
			return this;
		}

		public Connection charset(@NotNull String charsetName) {
			return charset(Charset.forName(charsetName));
		}

		public Connection charset(@NotNull Charset charset) {
			ftpClient.setCharset(charset);
			return this;
		}

		public Connection proxy(@NotNull String ipAddr) {
			if (URIUtil.isIPv4Address(ipAddr)) {
				int index = ipAddr.lastIndexOf(":");
				return proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			} else if (URIUtil.isIPv6Address(ipAddr)) {
				return proxy(ipAddr.substring(1, ipAddr.indexOf(']')), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
			} else {
				throw new RuntimeException("代理格式错误");
			}
		}

		public Connection proxy(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		}

		public Connection proxy(@NotNull Proxy proxy) {
			ftpClient.setProxy(proxy);
			return this;
		}

		/**
		 * 运行程序，获取 响应结果
		 *
		 * @return Response
		 */
		public FTPResponse execute() {
			try {
				ftpClient.connect(host, port);
				if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode()) && ftpClient.login(user, passwd)) {
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE); //设置以二进制流的方式传输
				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
			return new Response(ftpClient);
		}

	}

	private static class Response extends FTPResponse {
		private final FTPClient ftpClient;
		private final int status;

		private Response(FTPClient ftpClient) {
			this.ftpClient = ftpClient;
			status = ftpClient.getReplyCode();
		}

		public void disconnect() {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public FTPResponse listener(@NotNull CopyStreamListener listener) {
			ftpClient.setCopyStreamListener(listener);
			return this;
		}

		public int statusCode() {
			return status;
		}

		public List<FTPFile> listFiles(@NotNull String remote) {
			try {
				return Arrays.stream(ftpClient.listFiles(remote)).collect(Collectors.toList());
			} catch (IOException e) {
				return null;
			}
		}

		public int delete(@NotNull String remote) {
			if (status != 200) {
				return status;
			}
			remote = remote.startsWith("/") ? remote : "/" + remote;
			String fileName = remote.substring(remote.lastIndexOf("/") + 1);
			String folder = remote.substring(0, remote.lastIndexOf("/") + 1);
			try {
				ftpClient.changeWorkingDirectory(folder);
				ftpClient.deleteFile(fileName);
			} catch (IOException e) {
				// return status;
			}
			return ftpClient.getReplyCode();
		}

		public int rename(@NotNull String remoteIn, @NotNull String renameOut) {
			if (status != 200) {
				return status;
			}
			try {
				ftpClient.rename(remoteIn, renameOut);
			} catch (IOException e) {
				// return status;
			}
			return ftpClient.getReplyCode();
		}

		public int upload(@NotNull String local, @NotNull String remote) {
			if (status != 200) {
				return status;
			}
			remote = remote.startsWith("/") ? remote : "/" + remote;
			String fileName = remote.substring(remote.lastIndexOf("/") + 1);
			if (fileName.isEmpty()) {
				throw new DownloadException("not has fileName");
			}
			File localFile = new File(local);
			String folder = remote.substring(0, remote.lastIndexOf("/") + 1);
			try (InputStream in = new FileInputStream(localFile)) {
				if (!ftpClient.changeWorkingDirectory(folder) && !ftpClient.makeDirectory(folder)) {
					return ftpClient.getReplyCode();
				}
				// 远程存在文件,开启断点续传
				FTPFile remoteFile = Arrays.stream(ftpClient.listFiles(folder)).filter(l -> fileName.equals(l.getName())).findFirst().orElse(null);
				if (remoteFile != null) {
					long remoteSize = remoteFile.getSize();
					long localSize = localFile.length();
					if (remoteSize >= localSize) {
						return FTPReply.CLOSING_DATA_CONNECTION;
					}
					in.skip(remoteSize);
					ftpClient.setRestartOffset(remoteSize);
				}
				if (!ftpClient.storeFile(fileName, in)) {
					return FTPReply.TRANSFER_ABORTED;
				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
			return ftpClient.getReplyCode();
		}

		public int download(@NotNull String remote, @NotNull String local) {
			remote = remote.startsWith("/") ? remote : "/" + remote;
			String folder = remote.substring(0, remote.lastIndexOf("/") + 1);
			String fileName = remote.substring(remote.lastIndexOf("/") + 1);
			if (fileName.isEmpty()) {
				throw new DownloadException("not has fileName");
			}
			File localFile = new File(local);
			FileUtil.createFolder(localFile.getParentFile());
			try (OutputStream out = new FileOutputStream(local, true)) {
				ftpClient.changeWorkingDirectory(folder);
				FTPFile remoteFile = Arrays.stream(ftpClient.listFiles(folder)).filter(l -> fileName.equals(l.getName())).findFirst().orElse(null);
				if (remoteFile != null && localFile.exists()) {
					long remoteSize = remoteFile.getSize();
					long localSize = localFile.length();
					if (localSize >= remoteSize) {
						return FTPReply.CLOSING_DATA_CONNECTION;
					}
					ftpClient.setRestartOffset(localSize);
				}
				if (!ftpClient.retrieveFile(fileName, out)) {
					return FTPReply.TRANSFER_ABORTED;
				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
			return ftpClient.getReplyCode();
		}

	}

}
