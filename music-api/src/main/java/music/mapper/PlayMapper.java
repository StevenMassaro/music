package music.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Mapper
@Repository
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
