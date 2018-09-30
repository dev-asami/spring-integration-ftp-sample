package jp.co.sunarch.sasami.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class FtpRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(FtpRunner.class);

	@Autowired
	FtpConfigurationProperties ftpConfig;
	@Autowired
	FtpInboundService ftpService;

	@Override
	public void run(String... args) throws Exception {
		log.info("Command line args: {}", (Object[])args);
		log.info("Config setting: {}", ftpConfig);

		int downloadCount = ftpService.downlaod(ftpConfig.getDownloadTarget());

		log.info("Download cout: {}", downloadCount);
	}

}
