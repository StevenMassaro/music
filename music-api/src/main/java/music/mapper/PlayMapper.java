package music.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface PlayMapper {

    void insertPlay(@Param("id") long id, @Param("playdate") Date playDate,
                    @Param("deviceId") long deviceId);
}
