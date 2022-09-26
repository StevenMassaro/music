package music.service;

import music.model.DeferredTrack;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

import static music.helper.BuilderKt.track;
import static org.junit.Assert.*;

public class FileServiceIT extends IntegrationTestBase {

	@Autowired
	private FileService fileService;

	@Test
	public void testRecursivelyDeletingEmptyDirectories() throws IOException {
		File subdirFile = new File("random/empty/directories/file.txt");
		subdirFile.deleteOnExit();
		FileUtils.write(subdirFile, "helloworld");

		File file = new File("random/otherfile.txt");
		file.deleteOnExit();
		FileUtils.write(file, "helloworld2");

		assertTrue(subdirFile.exists());

		fileService.recursivelyDeleteEmptyDirectories(subdirFile);

		assertFalse(subdirFile.exists());
		assertTrue(file.exists());
		assertFalse(subdirFile.getParentFile().exists());
		assertFalse(subdirFile.getParentFile().getParentFile().exists());
		assertTrue(subdirFile.getParentFile().getParentFile().getParentFile().exists());

		fileService.recursivelyDeleteEmptyDirectories(file);
		assertFalse(file.exists());
		assertFalse(file.getParentFile().exists());
	}

	@Test
	public void testSpecialCharactersInFilenameGeneration() throws Exception {
		DeferredTrack track = track();
		track.setArtist("Joey Bada$$");
		track.setAlbum("B4.DA.A$$");

		String s = fileService.generateFilename(track);
		assertEquals("Music\\Joey Bada\\B4.DA.A\\3 - title.flac", s);
	}
}
