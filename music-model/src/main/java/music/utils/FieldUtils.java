package music.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FieldUtils {

    public static Long getLongOrNull(Tag tag, FieldKey fieldKey){
        String val = tag.getFirst(fieldKey);
        return (val == null || val.isEmpty()) ? null : Long.valueOf(val);
    }

	public static String getStringOrNull(Object val) {
		return val == null ? "" : val.toString();
	}

	public static String calculateHash(String absolutePath) throws IOException {
		return calculateHash(new File(absolutePath));
	}

	public static String calculateHash(File file) throws IOException {
		InputStream inputStream = FileUtils.openInputStream(file);
		String hash = DigestUtils.sha512Hex(inputStream);
		inputStream.close();
		return hash;
	}
}
