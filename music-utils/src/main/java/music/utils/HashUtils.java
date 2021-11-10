package music.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HashUtils {
	public static String calculateHash(String absolutePath) throws IOException {
		return calculateHash(new File(absolutePath));
	}

	public static String calculateHash(File file) throws IOException {
		try (InputStream inputStream = FileUtils.openInputStream(file)) {
			return DigestUtils.sha512Hex(inputStream);
		}
	}
}
