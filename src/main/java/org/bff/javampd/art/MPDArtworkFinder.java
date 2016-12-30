package org.bff.javampd.art;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bff.javampd.MPDException;
import org.bff.javampd.album.MPDAlbum;
import org.bff.javampd.artist.MPDArtist;
import org.bff.javampd.song.SongDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MPDArtworkFinder implements ArtworkFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MPDArtworkFinder.class);

    private SongDatabase songDatabase;

    @Inject
    public MPDArtworkFinder(SongDatabase songDatabase) {
        this.songDatabase = songDatabase;
    }

    @Override
    public List<MPDArtwork> find(MPDAlbum album) {
        List<MPDArtwork> artworkList = new ArrayList<>();
        List<String> paths = new ArrayList<>();

        this.songDatabase.findAlbum(album)
                .forEach(song -> paths.add(song.getFile().substring(0, song.getFile().lastIndexOf(File.separator))));

        paths.stream().distinct().forEach(path -> artworkList.addAll(find(path)));

        return artworkList;
    }

    @Override
    public List<MPDArtwork> find(MPDArtist artist) {
        List<MPDArtwork> artworkList = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        List<String> albumPaths = new ArrayList<>();

        this.songDatabase.findArtist(artist)
                .forEach(song -> albumPaths.add(song.getFile().substring(0, song.getFile().lastIndexOf(File.separator))));

        albumPaths.forEach(path -> {
            if (path.contains(artist.getName())) {
                paths.add(path.substring(0, path.lastIndexOf(File.separator)));
            }
        });

        paths.addAll(albumPaths);
        paths.stream().distinct().forEach(path -> artworkList.addAll(find(path)));

        return artworkList;
    }

    @Override
    public List<MPDArtwork> find(String path) {
        List<MPDArtwork> artworkList = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path), "**.{jpg,jpeg,png}")) {
            stream.forEach(file -> {
                        file.getFileName();
                        MPDArtwork artwork = new MPDArtwork(file.getFileName().toString(),
                                file.toAbsolutePath().toString());
                        artwork.setBytes(loadFile(file));
                        artworkList.add(artwork);
                    }
            );
        } catch (IOException e) {
            LOGGER.error("Could not load art in {}", path, e);
            throw new MPDException("Could not read path: " + path, e);
        }

        return artworkList;
    }

    private static byte[] loadFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            LOGGER.error("Could not load art in {}", path, e);
            throw new MPDException("Could not read path: " + path, e);
        }
    }
}