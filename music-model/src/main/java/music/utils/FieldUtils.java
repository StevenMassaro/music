package music.utils;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class FieldUtils {

    public static Long getLongOrNull(Tag tag, FieldKey fieldKey){
        String val = tag.getFirst(fieldKey);
        return (val == null || val.isEmpty()) ? null : Long.valueOf(val);
    }
}
