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

    void update(@Param("track") Track track);

    void insert(@Param("track") Track track);

	List<Track> listByLibraryId(@Param("libraryId") long libraryId);

	List<Track> list();

    List<Track> listByAlbum(String album, String artist, Long disc);

    List<Track> listDeleted();

    List<Track> listDeletedByLibraryId(@Param("libraryId") long libraryId);

    List<Track> listWithSmartPlaylist(@Param("dynamicSql") String dynamicSql);

    long countPurgableTracks();

    List<Track> listPurgableTracks();

    List<Track> listPlaysByDate(Date date);

    List<Date> listHistoricalDates();

    Track get(@Param("id") long id);

    Track getByLocationAndLibrary(@Param("location") String location, long libraryId);

    Track getByTitleArtistAlbum(String title, String artist, String album);

    void deleteById(@Param("id") long id);

    void markDeletedById(@Param("id")long id, @Param("deletedInd") boolean deletedInd);

    void setRatingById(long id, byte rating);

    void updateFieldById(long id, String field, Object newValue, String type);
}
