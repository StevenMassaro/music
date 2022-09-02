package music.model;

import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static music.utils.HashUtils.calculateHash;


/**
 * A track object, where the calculation of the hash is deferred until it is requested. On the first sync performed
 * the hash will be calculated for each file and put into the database. The first sync will be slow. Subsequent syncs
 * do not need a hash calculation because the data is already in the database, and thus, it can be deferred until it is
 * required.
 */
public class DeferredTrack extends Track {

    private String musicFileSource;

    public DeferredTrack (){}

    public DeferredTrack(Tag v2tag, AudioHeader header, String location, File file, String musicFileSource, Library library) throws IOException {
        super(v2tag, header, location, null, new Date(file.lastModified()), library);
        this.musicFileSource = musicFileSource;
    }

    @Override
    public String getHash() throws IOException {
    	return calculateHash(musicFileSource + File.separator + super.getLibraryPath());
    }

    @Override
    public String toString() {
        return "DeferredTrack{" +
                "musicFileSource='" + musicFileSource + '\'' +
				"track='" + super.toString() + '\'' +
                '}';
    }
}
