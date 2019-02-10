package Music.files.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;

@Service
public class FileService {

    public Collection<File> listMusicFiles(){
        return FileUtils.listFiles(new File("/music"), new String[]{"mp3", "flac", "FLAC"}, true);
    }
}
