package music.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * A track object, where the calculation of the hash is deferred until it is requested. On the first sync performed
 * the hash will be calculated for each file and put into the database. The first sync will be slow. Subsequent syncs
 * do not need a hash calculation because the data is already in the database, and thus, it can be deferred until it is
 * required.
 */
public class DeferredTrack extends Track {

    private String musicFileSource;

    public DeferredTrack(Tag v2tag, String location, File file, String musicFileSource) throws IOException {
        super(v2tag, location, null, new Date(file.lastModified()));
        this.musicFileSource = musicFileSource;
    }

    @Override
    public String getHash() throws IOException {
        return DigestUtils.sha512Hex(FileUtils.readFileToByteArray(new File(musicFileSource + File.separator + super.getLocation())));
    }
}
