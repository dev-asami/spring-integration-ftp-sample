package jp.co.sunarch.sasami.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="sasami.ftp")
public class FtpConfigurationProperties {

	String host;
	int port;
	String username;
	String password;
	int connectionMode = FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE;
	/** Socket connect timeout.(ms) */
	int defaultTimeout = 10000;
	String remotePath;
	String localPath;
	String wildcardFilename;
	String downloadTarget;

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getConnectionMode() {
		return connectionMode;
	}
	public void setConnectionMode(int connectionMode) {
		this.connectionMode = connectionMode;
	}
	public int getDefaultTimeout() {
		return defaultTimeout;
	}
	public void setDefaultTimeout(int defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}
	public String getRemotePath() {
		return remotePath;
	}
	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}
	public String getLocalPath() {
		return localPath;
	}
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	public String getWildcardFilename() {
		return wildcardFilename;
	}
	public void setWildcardFilename(String wildcardFilename) {
		this.wildcardFilename = wildcardFilename;
	}
	public String getDownloadTarget() {
		return downloadTarget;
	}
	public void setDownloadTarget(String downloadTarget) {
		this.downloadTarget = downloadTarget;
	}
	@Override
	public String toString() {
		return String.format(
				"FtpConfigurationProperties [host=%s, port=%s, username=%s, password=%s, connectionMode=%s, defaultTimeout=%s, remotePath=%s, localPath=%s, wildcardFilename=%s, downloadTarget=%s]",
				host, port, username, password, connectionMode, defaultTimeout, remotePath, localPath, wildcardFilename,
				downloadTarget);
	}

}
