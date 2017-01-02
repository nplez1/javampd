package org.bff.javampd.art;

import org.bff.javampd.MPDException;
import org.bff.javampd.album.MPDAlbum;
import org.bff.javampd.artist.MPDArtist;
import org.bff.javampd.song.MPDSong;
import org.bff.javampd.song.SongDatabase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MPDArtworkFinderTest {
    private ArtworkFinder artworkFinder;

    @Mock
    private SongDatabase songDatabase;

    @Before
    public void before() {
        artworkFinder = new MPDArtworkFinder(this.songDatabase);
    }

    @Test
    public void findArtist() throws Exception {
        String[] artistImages = new String[]{
                "artist200x200.jpg",
                "artist200x200.png"
        };

        String[] albumImages = new String[]{
                "album200x200.jpg",
                "album200x200.png"
        };

        MPDArtist artist = new MPDArtist("artist");

        String path1 = decode(new File(this.getClass().getResource("/images/artist/" + artistImages[0]).getFile()).getParent());
        String path2 = decode(new File(this.getClass().getResource("/images/artist/" + artistImages[1]).getFile()).getParent());
        List<MPDSong> songs = new ArrayList<>();
        songs.add(new MPDSong(path1 + "/album/song1", "song1"));
        songs.add(new MPDSong(path2 + "/album/song2", "song2"));

        when(songDatabase.findArtist(artist)).thenReturn(songs);

        List<MPDArtwork> artworkList = artworkFinder.find(artist);

        assertEquals(artistImages.length + albumImages.length, artworkList.size());

        Arrays.asList(artistImages).forEach(image -> {
            MPDArtwork foundArtwork = artworkList
                    .stream()
                    .filter(artwork -> image.equals(artwork.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(foundArtwork.getPath());
            assertNotNull(foundArtwork.getBytes());
        });

        Arrays.asList(albumImages).forEach(image -> {
            MPDArtwork foundArtwork = artworkList
                    .stream()
                    .filter(artwork -> image.equals(artwork.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(foundArtwork.getPath());
            assertNotNull(foundArtwork.getBytes());
        });
    }

    @Test
    public void findArtistBadPath() throws Exception {
        String[] artistImages = new String[]{
                "artist200x200.png"
        };

        MPDArtist artist = new MPDArtist("artist");

        String path1 = decode(new File(this.getClass().getResource("/images/" + artistImages[0]).getFile()).getParent());
        List<MPDSong> songs = new ArrayList<>();
        songs.add(new MPDSong(path1, "song1"));

        when(songDatabase.findArtist(artist)).thenReturn(songs);

        List<MPDArtwork> artworkList = artworkFinder.find(artist);

        assertEquals(0, artworkList.size());
    }

    @Test
    public void findAlbum() throws Exception {
        String[] albumImages = new String[]{
                "album200x200.jpg",
                "album200x200.png"
        };

        MPDAlbum album = new MPDAlbum("album", "artist");

        String path1 = decode(new File(this.getClass().getResource("/images/artist/album/" + albumImages[0]).getFile()).getParent());
        List<MPDSong> songs = new ArrayList<>();
        songs.add(new MPDSong(path1 + "/song1", "song1"));

        when(songDatabase.findAlbum(album)).thenReturn(songs);

        List<MPDArtwork> artworkList = artworkFinder.find(album);

        assertEquals(albumImages.length, artworkList.size());

        Arrays.asList(albumImages).forEach(image -> {
            MPDArtwork foundArtwork = artworkList
                    .stream()
                    .filter(artwork -> image.equals(artwork.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(foundArtwork.getPath());
            assertNotNull(foundArtwork.getBytes());
        });
    }

    @Test
    public void findPath() throws Exception {
        String[] images = new String[]{
                "artist200x200.jpg",
                "artist200x200.png"
        };

        String testImage = decode(new File(this.getClass().getResource("/images/artist/" + images[0]).getFile()).getParent());
        List<MPDArtwork> artworkList = artworkFinder.find(testImage);

        assertEquals(images.length, artworkList.size());

        Arrays.asList(images).forEach(image -> {
            MPDArtwork foundArtwork = artworkList
                    .stream()
                    .filter(artwork -> image.equals(artwork.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(foundArtwork.getPath());
            assertNotNull(foundArtwork.getBytes());
        });
    }

    @Test(expected = MPDException.class)
    public void findPathIOException() throws Exception {
        File testFile = File.createTempFile("test", ".jpg");

        testFile.setReadable(false);

        artworkFinder.find(testFile.getParent());
    }

    @Test(expected = MPDException.class)
    public void findPathDirectoryIOException() throws Exception {
        String javaTempDir = System.getProperty("java.io.tmpdir");

        File tempDir = new File(javaTempDir + (javaTempDir.endsWith(File.separator) ? "" : File.separator) + "imageTemp");
        System.out.println("tempDir is " + tempDir.getAbsolutePath());
        tempDir.mkdir();
        tempDir.setWritable(true);

        File testFile = null;
        try {
            testFile = File.createTempFile("test", ".jpg", tempDir);
        } catch (IOException e) {
            System.out.println(tempDir);
            e.printStackTrace();
        }

        tempDir.setReadable(false);

        try {
            artworkFinder.find(testFile.getParent());
        } finally {
            tempDir.setReadable(true);
            tempDir.delete();
        }
    }

    @Test(expected = MPDException.class)
    public void findBadPath() throws Exception {
        List<MPDArtwork> artworkList = artworkFinder.find("bad");

        assertEquals(2, artworkList.size());
    }

    private String decode(String encodedString) throws UnsupportedEncodingException {
        return URLDecoder.decode(encodedString, "UTF-8");
    }
}