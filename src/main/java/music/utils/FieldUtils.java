package music.utils;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.util.StringUtils;

public class FieldUtils {

    public static Long getLongOrNull(Tag tag, FieldKey fieldKey){
        String val = tag.getFirst(fieldKey);
        return StringUtils.isEmpty(val) ? null : Long.valueOf(val);
    }
}
