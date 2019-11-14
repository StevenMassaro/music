package music.mapper;

import music.model.Track;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface TrackMapper {

    void updateByLocation(@Param("track") Track track);

    void insert(@Param("track") Track track);

    List<Track> list();

    List<Track> listAll();

    List<Track> listPlaysByDate(Date date);

    List<Date> listHistoricalDates();

    Track get(@Param("id") long id);

    Track getByLocation(@Param("location") String location);

    void deleteById(@Param("id") long id);

    void markDeletedById(@Param("id")long id, @Param("deletedInd") boolean deletedInd);

    void setRatingById(long id, byte rating);
}
