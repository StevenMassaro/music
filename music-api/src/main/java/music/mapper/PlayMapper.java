package music.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface PlayMapper {

    void insertPlay(@Param("id") long id, @Param("playdate") Date playDate,
                    @Param("deviceId") long deviceId,
                    @Param("imported") boolean imported);

    void upsertPlayCount(@Param("songid") long songId,
                         @Param("deviceid") long deviceId,
                         @Param("playcount") long playCount,
                         boolean imported);

    void deletePlays(long id);

    void deletePlayCounts(long id);
}
