package jp.co.sunarch.sasami.ftp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class FtpSampleApplicationTests {

	FakeFtpServer fakeFtpServer;
	String HOME_DIRECTORY = "/";
	String TEST_DIRECTORY = "foo0csv";
	String[][] TEST_FILES1 = {
			{"foo1.txt", "foo1"},
			{"foo2.txt", "foo2"},
			{"foo3.csv", "foo,3"},
			{"foo4.csv", "foo,4"},
			{"bar1.txt", "bar1"},
			{"bar2.txt", "bar2"},
			{"bar3.csv", "bar,3"},
			{"bar4.csv", "bar,4"}
		};
	String[][] TEST_FILES2 = {
			{"foo1.csv1", "foo,1"},
			{"foo2.csv2", "foo,2"},
		};
	String TEST_FILE_BIN = "foo-bin.csv";

	@Autowired
	FtpConfigurationProperties ftpConfig;
	@Autowired
	FtpInboundService ftpService;

	@Before
	public void setupFtp() {
		FileSystemUtils.deleteRecursively(Paths.get(ftpConfig.getLocalPath()).toFile());
		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.setServerControlPort(ftpConfig.getPort());

		fakeFtpServer.addUserAccount(
				new UserAccount(ftpConfig.getUsername(), ftpConfig.getPassword(), HOME_DIRECTORY));

		fakeFtpServer.start();
	}
	@After
	public void stopFtp() {
		FileSystemUtils.deleteRecursively(Paths.get(ftpConfig.getLocalPath()).toFile());
		fakeFtpServer.stop();
	}

	@Test
	public void e2eTest1() throws IOException {
		// ファイルの抽出、ダウンロードテスト
		// テストファイルを設定
		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry(
				String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_DIRECTORY)));
		for(String[] testFile: TEST_FILES1) {
			fileSystem.add(new FileEntry(
					String.format("%1$s/%2$s", ftpConfig.getRemotePath(), testFile[0]), testFile[1]));
		}
		fakeFtpServer.setFileSystem(fileSystem);

		String target = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		int count = ftpService.downlaod(target);
		assertThat(count, is(2));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_DIRECTORY)), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[0][0])), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[1][0])), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[2][0])), is(true));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[3][0])), is(true));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[4][0])), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[5][0])), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[6][0])), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES1[7][0])), is(false));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_DIRECTORY)), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[0][0])), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[1][0])), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[2][0])), is(false));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[3][0])), is(false));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[4][0])), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[5][0])), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[6][0])), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES1[7][0])), is(true));

	}
	@Test
	public void e2eTest2() throws IOException {
		// ファイルの抽出、対象なしテスト
		// テストファイルを設定
		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry(
				String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_DIRECTORY)));
		for(String[] testFile: TEST_FILES2) {
			fileSystem.add(new FileEntry(
					String.format("%1$s/%2$s", ftpConfig.getRemotePath(), testFile[0]), testFile[1]));
		}
		fakeFtpServer.setFileSystem(fileSystem);

		String target = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		int count = ftpService.downlaod(target);
		assertThat(count, is(0));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_DIRECTORY)), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES2[0][0])), is(false));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILES2[1][0])), is(false));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_DIRECTORY)), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES2[0][0])), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILES2[1][0])), is(true));

	}
	@Test
	public void e2eTest3() throws IOException {
		// 対象フォルダなしテスト
		// テストファイルを設定
		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry("/"));
		fakeFtpServer.setFileSystem(fileSystem);

		String target = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		int count = ftpService.downlaod(target);
		assertThat(count, is(0));

	}
	@Test
	public void e2eTest4() throws IOException {
		// バイナリファイルテスト
		// テストファイルを設定
		FileSystem fileSystem = new UnixFakeFileSystem();
		FileEntry fileEntry = new FileEntry(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILE_BIN));
		// バイナリ読み込み
		Resource resource = new ClassPathResource("test.txt.gz");
		fileEntry.setContents(Files.readAllBytes(Paths.get(resource.getURI())));
		fileSystem.add(fileEntry);
		fakeFtpServer.setFileSystem(fileSystem);

		String target = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		int count = ftpService.downlaod(target);
		assertThat(count, is(1));
		assertThat(Files.exists(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILE_BIN)), is(true));
		assertThat(fakeFtpServer.getFileSystem().exists(String.format("%1$s/%2$s", ftpConfig.getRemotePath(), TEST_FILE_BIN)), is(false));
		// gzip解凍
		GZIPInputStream gis = new GZIPInputStream(
				Files.newInputStream(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILE_BIN)));
		FileCopyUtils.copy(gis, Files.newOutputStream(
				Paths.get(ftpConfig.getLocalPath(), target, TEST_FILE_BIN + ".txt")));
		long originalSize = Files.size(Paths.get(new ClassPathResource("test.txt").getURI()));
		long uncompressSize = Files.size(Paths.get(ftpConfig.getLocalPath(), target, TEST_FILE_BIN + ".txt"));
		assertThat(originalSize, is(uncompressSize));
	}

}
