package music.service;

import music.model.DeferredTrack;
import music.model.Library;
import music.model.SyncResult;
import music.model.Track;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.util.ResourceUtils;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static music.helper.BuilderKt.track;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ContextConfiguration(initializers = IntegrationTestBase.MusicPropertiesInitializer.class)
public abstract class IntegrationTestBase {

	@Autowired
	private TrackService trackService;

	public File tempFile;
	public File tempFile2;
	public static final Library seededMusicLibrary = new Library(1, "Music", "Music");

	public static class MusicPropertiesInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
				"local.music.file.location=" + System.getProperty("java.io.tmpdir").replace("\\", "\\\\"));
		}
	}
	@Before
	public void before() throws IOException {
		tempFile = createTempFile("sample1.flac");
		tempFile2 = createTempFile("sample2.flac");
	}

	public static File createTempFile(String filename) throws IOException {
		String extension = "." + FilenameUtils.getExtension(filename);
		File dir = new File(System.getProperty("java.io.tmpdir"), seededMusicLibrary.getSubfolder());
		dir.mkdirs();
		File f = File.createTempFile(filename.replace(extension, ""), extension, dir);
		FileUtils.copyFile(ResourceUtils.getFile("classpath:" + filename), f);
		f.deleteOnExit();
		return f;
	}

	@After
	public void after() {
		FileUtils.deleteQuietly(tempFile);
		FileUtils.deleteQuietly(tempFile2);
	}

	public Track insertTempFile() {
		return insertTempFile(tempFile.getName());
	}

	public Track insertTempFile2() {
		return insertTempFile(tempFile2.getName());
	}

	private Track insertTempFile(String location) {
		List<DeferredTrack> fauxtracks = Collections.singletonList(track(location));
		SyncResult syncResult = new SyncResult();
		trackService.upsertTracks(fauxtracks, syncResult);
		return syncResult.getNewTracks().get(0);
	}
}
