package music.mapper;

import music.model.Track;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrackMapper {

    void upsert(@Param("track") Track track);

    List<Track> list();

    List<Track> listAll();

    Track get(@Param("id") long id);

    void deleteById(@Param("id") long id);

    void markDeletedById(@Param("id")long id, @Param("deletedInd") boolean deletedInd);
}
