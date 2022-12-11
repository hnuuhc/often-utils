package org.haic.often.net.aria2;

import org.haic.often.annotations.Contract;

/**
 * Aria2方法名常量
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/24 10:35
 */
public enum Aria2Method {
	/**
	 * This method adds a new download. uris is an array of HTTP/FTP/SFTP/BitTorrent URIs (strings) pointing to the same resource. If you mix URIs pointing to different resources, then the
	 * download may fail or be corrupted without aria2 complaining. When adding BitTorrent Magnet URIs, uris must have only one element and it should be BitTorrent Magnet URI. options is a struct
	 * and its members are pairs of option name and value. See Options below for more details. If position is given, it must be an integer starting from 0. The new download will be inserted at
	 * position in the waiting queue. If position is omitted or position is larger than the current size of the queue, the new download is appended to the end of the queue. This method returns the
	 * GID of the newly registered download.
	 */
	ADDURI("aria2.addUri"),
	/**
	 * This method adds a BitTorrent download by uploading a ".torrent" file. If you want to add a BitTorrent Magnet URI, use the aria2.addUri() method instead. torrent must be a base64-encoded
	 * string containing the contents of the ".torrent" file. uris is an array of URIs (string). uris is used for Web-seeding. For single file torrents, the URI can be a complete URI pointing to
	 * the resource; if URI ends with /, name in torrent file is added. For multi-file torrents, name and path in torrent are added to form a URI for each file. options is a struct and its members
	 * are pairs of option name and value. See Options below for more details. If position is given, it must be an integer starting from 0. The new download will be inserted at position in the
	 * waiting queue. If position is omitted or position is larger than the current size of the queue, the new download is appended to the end of the queue. This method returns the GID of the
	 * newly registered download. If --rpc-save-upload-metadata is true, the uploaded data is saved as a file named as the hex string of SHA-1 hash of data plus ".torrent" in the directory
	 * specified by --dir option. E.g. a file name might be 0a3893293e27ac0490424c06de4d09242215f0a6.torrent. If a file with the same name already exists, it is overwritten! If the file cannot be
	 * saved successfully or --rpc-save-upload-metadata is false, the downloads added by this method are not saved by --save-session.
	 */
	ADDTORRENT("aria2.addTorrent"),
	/**
	 * This method adds a Metalink download by uploading a ".metalink" file. metalink is a base64-encoded string which contains the contents of the ".metalink" file. options is a struct and its
	 * members are pairs of option name and value. See Options below for more details. If position is given, it must be an integer starting from 0. The new download will be inserted at position in
	 * the waiting queue. If position is omitted or position is larger than the current size of the queue, the new download is appended to the end of the queue. This method returns an array of
	 * GIDs of newly registered downloads. If --rpc-save-upload-metadata is true, the uploaded data is saved as a file named hex string of SHA-1 hash of data plus ".metalink" in the directory
	 * specified by --dir option. E.g. a file name might be 0a3893293e27ac0490424c06de4d09242215f0a6.metalink. If a file with the same name already exists, it is overwritten! If the file cannot be
	 * saved successfully or --rpc-save-upload-metadata is false, the downloads added by this method are not saved by --save-session.
	 */
	ADDMETALINK("aria2.addMetalink"),
	/**
	 * This method removes the download denoted by gid (string). If the specified download is in progress, it is first stopped. The status of the removed download becomes removed. This method
	 * returns GID of removed download.
	 */
	REMOVE("aria2.remove"),
	/**
	 * This method removes the download denoted by gid. This method behaves just like aria2.remove() except that this method removes the download without performing any actions which take time,
	 * such as contacting BitTorrent trackers to unregister the download first.
	 */
	FORCEREMOVE("aria2.forceRemove"),
	/**
	 * This method pauses the download denoted by gid (string). The status of paused download becomes paused. If the download was active, the download is placed in the front of waiting queue.
	 * While the status is paused, the download is not started. To change status to waiting, use the aria2.unpause() method. This method returns GID of paused download.
	 */
	PAUSE("aria2.pause"),
	/**
	 * This method is equal to calling aria2.pause() for every active/waiting download. This methods returns OK.
	 */
	PAUSEALL("aria2.pauseAll"),
	/**
	 * This method pauses the download denoted by gid. This method behaves just like aria2.pause() except that this method pauses downloads without performing any actions which take time, such as
	 * contacting BitTorrent trackers to unregister the download first.
	 */
	FORCEPAUSE("aria2.forcePause"),
	/**
	 * This method is equal to calling aria2.forcePause() for every active/waiting download. This methods returns OK.
	 */
	FORCEPAUSEALL("aria2.forcePauseAll"),
	/**
	 * This method changes the status of the download denoted by gid (string) from paused to waiting, making the download eligible to be restarted. This method returns the GID of the unpaused
	 * download.
	 */
	UNPAUSE("aria2.unpause"),
	/**
	 * This method is equal to calling aria2.unpause() for every paused download. This methods returns OK.
	 */
	UNPAUSEALL("aria2.unpauseAll"),
	/**
	 * This method returns the progress of the download denoted by gid (string). keys is an array of strings. If specified, the response contains only keys in the keys array. If keys is empty or
	 * omitted, the response contains all keys. This is useful when you just want specific keys and avoid unnecessary transfers. For example, aria2.tellStatus("2089b05ecca3d829", ["gid",
	 * "status"]) returns the gid and status keys only. The response is a struct and contains following keys. Values are strings.
	 * <p>
	 * gid - GID of the download.
	 * <p>
	 * status - active for currently downloading/seeding downloads. waiting for downloads in the queue; download is not started. paused for paused downloads. error for downloads that were stopped
	 * because of error. complete for stopped and completed downloads. removed for the downloads removed by user.
	 * <p>
	 * totalLength - Total length of the download in bytes.
	 * <p>
	 * completedLength - Completed length of the download in bytes.
	 * <p>
	 * uploadLength - Uploaded length of the download in bytes.
	 * <p>
	 * bitfield - Hexadecimal representation of the download progress. The highest bit corresponds to the piece at index 0. Any set bits indicate loaded pieces, while unset bits indicate not yet
	 * loaded and/or missing pieces. Any overflow bits at the end are set to zero. When the download was not started yet, this key will not be included in the response.
	 * <p>
	 * downloadSpeed - Download speed of this download measured in bytes/sec.
	 * <p>
	 * uploadSpeed - Upload speed of this download measured in bytes/sec.
	 * <p>
	 * infoHash - InfoHash. BitTorrent only.
	 * <p>
	 * numSeeders - The number of seeders aria2 has connected to. BitTorrent only.
	 * <p>
	 * seeder - true if the local endpoint is a seeder. Otherwise false. BitTorrent only.
	 * <p>
	 * pieceLength - Piece length in bytes.
	 * <p>
	 * numPieces - The number of pieces.
	 * <p>
	 * connections - The number of peers/servers aria2 has connected to.
	 * <p>
	 * errorCode - The code of the last error for this item, if any. The value is a string. The error codes are defined in the EXIT STATUS section. This value is only available for
	 * stopped/completed downloads.
	 * <p>
	 * errorMessage - The (hopefully) human readable error message associated to errorCode.
	 * <p>
	 * followedBy - List of GIDs which are generated as the result of this download. For example, when aria2 downloads a Metalink file, it generates downloads described in the Metalink (see the
	 * --follow-metalink option). This value is useful to track auto-generated downloads. If there are no such downloads, this key will not be included in the response.
	 * <p>
	 * following - The reverse link for followedBy. A download included in followedBy has this object's GID in its following value.
	 * <p>
	 * belongsTo - GID of a parent download. Some downloads are a part of another download. For example, if a file in a Metalink has BitTorrent resources, the downloads of ".torrent" files are
	 * parts of that parent. If this download has no parent, this key will not be included in the response.
	 * <p>
	 * dir - Directory to save files.
	 * <p>
	 * files - Returns the list of files. The elements of this list are the same structs used in aria2.getFiles() method.
	 * <p>
	 * bittorrent - Struct which contains information retrieved from the .torrent (file). BitTorrent only. It contains following keys.
	 * <p>
	 * announceList - List of lists of announce URIs. If the torrent contains announce and no announce-list, announce is converted to the announce-list format.
	 * <p>
	 * comment - The comment of the torrent. comment.utf-8 is used if available.
	 * <p>
	 * creationDate - The creation time of the torrent. The value is an integer since the epoch, measured in seconds.
	 * <p>
	 * mode - File mode of the torrent. The value is either single or multi.
	 * <p>
	 * info - Struct which contains data from Info dictionary. It contains following keys.
	 * <p>
	 * name - name in info dictionary. name.utf-8 is used if available.
	 * <p>
	 * verifiedLength - The number of verified number of bytes while the files are being hash checked. This key exists only when this download is being hash checked.
	 * <p>
	 * verifyIntegrityPending - true if this download is waiting for the hash check in a queue. This key exists only when this download is in the queue.
	 */
	TELLSTATUS("aria2.tellStatus"),
	/**
	 * This method returns the URIs used in the download denoted by gid (string). The response is an array of structs and it contains following keys. Values are string.
	 * <p>
	 * uri - URI
	 * <p>
	 * status - 'used' if the URI is in use. 'waiting' if the URI is still waiting in the queue.
	 */
	GETURIS("aria2.getUris"),
	/**
	 * This method returns the file list of the download denoted by gid (string). The response is an array of structs which contain following keys. Values are strings.
	 * <p>
	 * index - Index of the file, starting at 1, in the same order as files appear in the multi-file torrent.
	 * <p>
	 * path - File path.
	 * <p>
	 * length - File size in bytes.
	 * <p>
	 * completedLength - Completed length of this file in bytes. Please note that it is possible that sum of completedLength is less than the completedLength returned by the aria2.tellStatus()
	 * method. This is because completedLength in aria2.getFiles() only includes completed pieces. On the other hand, completedLength in aria2.tellStatus() also includes partially completed
	 * pieces.
	 * <p>
	 * selected - true if this file is selected by --select-file option. If --select-file is not specified or this is single-file torrent or not a torrent download at all, this value is always
	 * true. Otherwise false.
	 * <p>
	 * uris - Returns a list of URIs for this file. The element type is the same struct used in the aria2.getUris() method.
	 */
	GETFILES("aria2.getFiles"),
	/**
	 * This method returns currently connected HTTP(S)/FTP/SFTP servers of the download denoted by gid (string). The response is an array of structs and contains the following keys. Values are
	 * strings.
	 * <p>
	 * index - Index of the file, starting at 1, in the same order as files appear in the multi-file metalink.
	 * <p>
	 * servers - A list of structs which contain the following keys.
	 * <p>
	 * uri - Original URI.
	 * <p>
	 * currentUri - This is the URI currently used for downloading. If redirection is involved, currentUri and uri may differ.
	 * <p>
	 * downloadSpeed - Download speed (byte/sec)
	 */
	GETPEERS("aria2.getPeers"),
	/**
	 * This method returns currently connected HTTP(S)/FTP/SFTP servers of the download denoted by gid (string). The response is an array of structs and contains the following keys. Values are
	 * strings.
	 * <p>
	 * index - Index of the file, starting at 1, in the same order as files appear in the multi-file metalink.
	 * <p>
	 * servers - A list of structs which contain the following keys.
	 * <p>
	 * 1.uri - Original URI.
	 * <p>
	 * 2.currentUri - This is the URI currently used for downloading. If redirection is involved, currentUri and uri may differ.
	 * <p>
	 * 3.downloadSpeed - Download speed (byte/sec)
	 */
	GETSERVERS("aria2.getServers"),
	/**
	 * This method returns a list of active downloads. The response is an array of the same structs as returned by the aria2.tellStatus() method. For the keys parameter, please refer to the
	 * aria2.tellStatus() method.
	 */
	TRLLACTIVE("aria2.tellActive"),
	/**
	 * This method returns a list of waiting downloads, including paused ones. offset is an integer and specifies the offset from the download waiting at the front. num is an integer and specifies
	 * the max. number of downloads to be returned. For the keys parameter, please refer to the aria2.tellStatus() method.
	 * <p>
	 * If offset is a positive integer, this method returns downloads in the range of [offset, offset + num).
	 * <p>
	 * offset can be a negative integer. offset == -1 points last download in the waiting queue and offset == -2 points the download before the last download, and so on. Downloads in the response
	 * are in reversed order then.
	 * <p>
	 * For example, imagine three downloads "A","B" and "C" are waiting in this order. aria2.tellWaiting(0, 1) returns ["A"]. aria2.tellWaiting(1, 2) returns ["B", "C"]. aria2.tellWaiting(-1, 2)
	 * returns ["C", "B"].
	 * <p>
	 * The response is an array of the same structs as returned by aria2.tellStatus() method.
	 */
	TELLWAITING("aria2.tellWaiting"),
	/**
	 * This method returns a list of stopped downloads. offset is an integer and specifies the offset from the least recently stopped download. num is an integer and specifies the max. number of
	 * downloads to be returned. For the keys parameter, please refer to the aria2.tellStatus() method.
	 * <p>
	 * offset and num have the same semantics as described in the aria2.tellWaiting() method.
	 * <p>
	 * The response is an array of the same structs as returned by the aria2.tellStatus() method.
	 */
	TELLSTOPPED("aria2.tellStopped"),
	/**
	 * This method changes the position of the download denoted by gid in the queue. pos is an integer. how is a string. If how is POS_SET, it moves the download to a position relative to the
	 * beginning of the queue. If how is POS_CUR, it moves the download to a position relative to the current position. If how is POS_END, it moves the download to a position relative to the end
	 * of the queue. If the destination position is less than 0 or beyond the end of the queue, it moves the download to the beginning or the end of the queue respectively. The response is an
	 * integer denoting the resulting position.
	 * <p>
	 * For example, if GID#2089b05ecca3d829 is currently in position 3, aria2.changePosition('2089b05ecca3d829', -1, 'POS_CUR') will change its position to 2. Additionally
	 * aria2.changePosition('2089b05ecca3d829', 0, 'POS_SET') will change its position to 0 (the beginning of the queue).
	 */
	CHANGEPOSITION("aria2.changePosition"),
	/**
	 * This method removes the URIs in delUris from and appends the URIs in addUris to download denoted by gid. delUris and addUris are lists of strings. A download can contain multiple files and
	 * URIs are attached to each file. fileIndex is used to select which file to remove/attach given URIs. fileIndex is 1-based. position is used to specify where URIs are inserted in the existing
	 * waiting URI list. position is 0-based. When position is omitted, URIs are appended to the back of the list. This method first executes the removal and then the addition. position is the
	 * position after URIs are removed, not the position when this method is called. When removing an URI, if the same URIs exist in download, only one of them is removed for each URI in delUris.
	 * In other words, if there are three URIs <a href="http://example.org/aria2">http://example.org/aria2</a> and you want remove them<a href=" all, you have to specif"> all, you have to
	 * specif</a>y (at least) 3 <a href="http://example.org/aria2">http://example.org/aria2</a> in delUris. This method returns a list which contains two integers. The first integer is the number
	 * of URIs deleted. The second integer is the number of URIs added.
	 */
	CHANGEURI("aria2.changeUri"),
	/**
	 * This method returns options of the download denoted by gid. The response is a struct where keys are the names of options. The values are strings. Note that this method does not return
	 * options which have no default value and have not been set on the command-line, in configuration files or RPC methods.
	 */
	GETOPTION("aria2.getOption"),
	/**
	 * This method changes options of the download denoted by gid (string) dynamically. options is a struct. The options listed in Input File subsection are available, except for following
	 * options:
	 * <p>
	 * dry-run
	 * <p>
	 * metalink-base-uri
	 * <p>
	 * parameterized-uri
	 * <p>
	 * pause
	 * <p>
	 * piece-length
	 * <p>
	 * rpc-save-upload-metadata
	 * <p>
	 * Except for the following options, changing the other options of active download makes it restart (restart itself is managed by aria2, and no user intervention is required):
	 * <p>
	 * bt-max-peers
	 * <p>
	 * bt-request-peer-speed-limit
	 * <p>
	 * bt-remove-unselected-file
	 * <p>
	 * force-save
	 * <p>
	 * max-download-limit
	 * <p>
	 * max-upload-limit
	 * <p>
	 * This method returns OK for success.
	 */
	CHANGEOPTION("aria2.changeOption"),
	/**
	 * This method returns the global options. The response is a struct. Its keys are the names of options. Values are strings. Note that this method does not return options which have no default
	 * value and have not been set on the command-line, in configuration files or RPC methods. Because global options are used as a template for the options of newly added downloads, the response
	 * contains keys returned by the aria2.getOption() method.
	 */
	GETGLOBALOPTION("aria2.getGlobalOption"),
	/**
	 * This method changes global options dynamically. options is a struct. The following options are available:
	 * <p>
	 * bt-max-open-files
	 * <p>
	 * download-result
	 * <p>
	 * keep-unfinished-download-result
	 * <p>
	 * log
	 * <p>
	 * log-level
	 * <p>
	 * max-concurrent-downloads
	 * <p>
	 * max-download-result
	 * <p>
	 * max-overall-download-limit
	 * <p>
	 * max-overall-upload-limit
	 * <p>
	 * optimize-concurrent-downloads
	 * <p>
	 * save-cookies
	 * <p>
	 * save-session
	 * <p>
	 * server-stat-of
	 * <p>
	 * In addition, options listed in the Input File subsection are available, except for following options: checksum, index-out, out, pause and select-file.
	 * <p>
	 * With the log option, you can dynamically start logging or change log file. To stop logging, specify an empty string("") as the parameter value. Note that log file is always opened in append
	 * mode. This method returns OK for success.
	 */
	CHANGEGLOBALOPTION("aria2.changeGlobalOption"),
	/**
	 * This method returns global statistics such as the overall download and upload speeds. The response is a struct and contains the following keys. Values are strings.
	 * <p>
	 * downloadSpeed - Overall download speed (byte/sec).
	 * <p>
	 * uploadSpeed - Overall upload speed(byte/sec).
	 * <p>
	 * numActive - The number of active downloads.
	 * <p>
	 * numWaiting - The number of waiting downloads.
	 * <p>
	 * numStopped - The number of stopped downloads in the current session. This value is capped by the --max-download-result option.
	 * <p>
	 * numStoppedTotal - The number of stopped downloads in the current session and not capped by the --max-download-result option.
	 */
	GETGLOBALSTAT("aria2.getGlobalStat"),
	/**
	 * This method purges completed/error/removed downloads to free memory. This method returns
	 */
	PURGEDOWNLOADRESULT("aria2.purgeDownloadResult"),
	/**
	 * This method removes a completed/error/removed download denoted by gid from memory. This method returns OK for success.
	 */
	REMOVEDOWNLOADRESULT("aria2.removeDownloadResult"),
	/**
	 * This method returns the version of aria2 and the list of enabled features. The response is a struct and contains following keys.
	 * <p>
	 * version - Version number of aria2 as a string.
	 * <p>
	 * enabledFeatures - List of enabled features. Each feature is given as a string.
	 */
	GETVERSION("aria2.getVersion"),
	/**
	 * This method returns session information. The response is a struct and contains following key.
	 * <p>
	 * sessionId - Session ID, which is generated each time when aria2 is invoked.
	 */
	GETSESSIONINFO("aria2.getSessionInfo"),
	/**
	 * This method shuts down aria2. This method returns OK.
	 */
	SHUTDOWN("aria2.shutdown"),
	/**
	 * This method shuts down aria2(). This method behaves like :func:'aria2.shutdown` without performing any actions which take time, such as contacting BitTorrent trackers to unregister
	 * downloads first. This method returns OK.
	 */
	FORCESHUTDOWN("aria2.forceShutdown"),
	/**
	 * This method saves the current session to a file specified by the --save-session option. This method returns OK if it succeeds.
	 */
	SAVESESSION("aria2.saveSession"),
	/**
	 * This methods encapsulates multiple method calls in a single request. methods is an array of structs. The structs contain two keys: methodName and params. methodName is the method name to
	 * call and params is array containing parameters to the method call. This method returns an array of responses. The elements will be either a one-item array containing the return value of the
	 * method call or a struct of fault element if an encapsulated method call fails.
	 */
	MULTICALL("aria2.multicall"),
	/**
	 * This method returns all the available RPC methods in an array of string. Unlike other methods, this method does not require secret token. This is safe because this method just returns the
	 * available method names.
	 */
	LISTMETHODS("system.listMethods"),
	/**
	 * This method returns all the available RPC notifications in an array of string. Unlike other methods, this method does not require secret token. This is safe because this method just returns
	 * the available notifications names.
	 */
	LISTNOTIFICATIONS("aria2.listNotifications");

	private final String value;

	Aria2Method(String value) {
		this.value = value;
	}

	@Contract(pure = true)
	public String getValue() {
		return value;
	}
}
