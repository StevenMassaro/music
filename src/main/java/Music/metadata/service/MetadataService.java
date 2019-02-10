package Music.metadata.service;

import Music.files.service.FileService;
import org.apache.commons.io.FileUtils;
import org.gagravarr.flac.FlacNativeFile;
import org.gagravarr.flac.FlacTags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class MetadataService {

    @Autowired
    private FileService fileService;

    public String getMetadataTest() throws IOException {
        List<File> files = new ArrayList<>(fileService.listMusicFiles());


        FlacNativeFile flac = new FlacNativeFile(FileUtils.openInputStream(files.get(0)));
        FlacTags flacTags = flac.getTags();
        //        Map<String, List<String>> allComments = flacTags.getAllComments();
//
//        Iterator it = allComments.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            List<String> pair.getValue()
//            it.remove(); // avoids a ConcurrentModificationException
//        }
        String stringBuilder = "TITLE " +
                flacTags.getTitle() +
                "ARTIST " +
                flacTags.getArtist() +
                "ALBUM " +
                flacTags.getAlbum();
        return stringBuilder;
    }
}
