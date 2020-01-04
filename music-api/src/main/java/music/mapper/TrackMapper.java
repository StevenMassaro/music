package music.mapper;

import music.model.Track;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface TrackMapper {

    void updateByLocation(@Param("track") Track track);

    void insert(@Param("track") Track track);

    List<Track> list();

    List<Track> listByAlbum(String album);

    List<Track> listAll();

    List<Track> listWithSmartPlaylist(@Param("dynamicSql") String dynamicSql);

    long countPurgableTracks();

    List<Track> listPlaysByDate(Date date);

    List<Date> listHistoricalDates();

    Track get(@Param("id") long id);

    Track getByLocation(@Param("location") String location);

    Track getByTitleArtistAlbum(String title, String artist, String album);

    void deleteById(@Param("id") long id);

    void markDeletedById(@Param("id")long id, @Param("deletedInd") boolean deletedInd);

    void setRatingById(long id, byte rating);

    void updateFieldById(long id, String field, Object newValue, String type);
}
