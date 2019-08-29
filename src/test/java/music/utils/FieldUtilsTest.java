package music.utils;

import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FieldUtilsTest {

    @Test
    @Ignore
    public void testUnusualNumbers() throws FieldDataInvalidException {
        // I haven't yet decided if the application should handle these edge cases, or expect the file to have the
        // proper format. It probably makes more sense to have the application reject improperly formatted files.
        VorbisCommentTag vorbisCommentTag = VorbisCommentTag.createNewTag();
        Tag tag = new FlacTag(vorbisCommentTag, Collections.emptyList());
        tag.addField(FieldKey.DISC_NO, "1 of 1");
        assertEquals(1, (long) FieldUtils.getLongOrNull(tag, FieldKey.DISC_NO));
    }
}
