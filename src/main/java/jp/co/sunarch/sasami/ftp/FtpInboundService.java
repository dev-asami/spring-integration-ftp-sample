package jp.co.sunarch.sasami.ftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class FtpInboundService {

	private static final Logger log = LoggerFactory.getLogger(FtpInboundService.class);

	@Autowired
	FtpConfigurationProperties ftpConfig;

	public int downlaod(String target) throws IOException {
		log.info("start Download service");
		// ダウンロード先ローカルパスを作成
		Files.createDirectories(Paths.get(ftpConfig.getLocalPath(), target));

		log.info("Download path: {}", Paths.get(ftpConfig.getLocalPath(), target));

		// FTPセッションを作成
		FtpRemoteFileTemplate template = new FtpRemoteFileTemplate(ftpSessionFactory());

		// リモートファイルの一覧を取得
		String remoteFileWildcard = String.format("%1$s/%2$s", ftpConfig.getRemotePath(), ftpConfig.getWildcardFilename());
		FTPFile[] ftpFiles = template.list(remoteFileWildcard);

		if(log.isTraceEnabled()) {
			log.trace("Remote files: {}", Arrays.asList(ftpFiles));
		}

		// ダウンロード済みファイルリスト
		List<FTPFile> downloadFiles = new ArrayList<>();
		// 対象のファイルをすべてダウンロード
		for(FTPFile ftpFile: ftpFiles) {
			if(ftpFile.isFile()) {
				// ファイルのみ処理
				template.get(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), ftpFile.getName()), is -> {
					FileCopyUtils.copy(is, Files.newOutputStream(
							Paths.get(ftpConfig.getLocalPath(), target, ftpFile.getName())));
				});
				downloadFiles.add(ftpFile);
				log.debug("Download file: {}", ftpFile);
			}
		}
		// ダウンロードファイルのベリファイ
		for(FTPFile ftpFile: downloadFiles) {
			long originalSize = ftpFile.getSize();
			long downloadSize = Files.size(Paths.get(ftpConfig.getLocalPath(), target, ftpFile.getName()));
			log.debug("Verify file: {}({}:{})", ftpFile.getName(), originalSize, downloadSize);
			if(originalSize != downloadSize) {
				throw new IOException(
						String.format("The size of the downloaded file is different.[%1$s(%2$s:%3$s)]" ,
								ftpFile.getName(), originalSize, downloadSize));
			}
		}
		// ダウンロード済みファイルの削除
		for(FTPFile ftpFile: downloadFiles) {
			template.remove(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), ftpFile.getName()));
			log.debug("Remove file: {}", ftpFile);
		}
		return downloadFiles.size();
	}

	private DefaultFtpSessionFactory ftpSessionFactory() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost(ftpConfig.getHost());
		sf.setPort(ftpConfig.getPort());
		sf.setUsername(ftpConfig.getUsername());
		sf.setPassword(ftpConfig.getPassword());
		sf.setDefaultTimeout(ftpConfig.getDefaultTimeout());
		return sf;
	}

}
