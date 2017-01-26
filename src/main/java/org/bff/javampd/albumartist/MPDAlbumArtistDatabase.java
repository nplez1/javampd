package org.bff.javampd.albumartist;

import com.google.inject.Inject;
import org.bff.javampd.database.TagLister;
import org.bff.javampd.genre.MPDGenre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nathanp
 */
public class MPDAlbumArtistDatabase implements AlbumArtistDatabase {

	private static final Logger LOGGER = LoggerFactory.getLogger(MPDAlbumArtistDatabase.class);

	private TagLister tagLister;

	@Inject
	public MPDAlbumArtistDatabase(TagLister tagLister) {
		this.tagLister = tagLister;
	}

	@Override
	public Collection<MPDAlbumArtist> listAll() {
		return tagLister.list(TagLister.ListType.ALBUM_ARTIST)
				.stream()
				.map(s -> new MPDAlbumArtist(convertResponse(s)))
				.collect(Collectors.toList());
	}

	@Override
	public Collection<MPDAlbumArtist> listByGenre(MPDGenre genre) {
		List<String> list = new ArrayList<>();
		list.add(TagLister.ListType.GENRE.getType());
		list.add(genre.getName());

		return tagLister.list(TagLister.ListType.ALBUM_ARTIST, list)
				.stream()
				.map(s -> new MPDAlbumArtist(convertResponse(s)))
				.collect(Collectors.toList());
	}

	@Override
	public MPDAlbumArtist listByName(String name) {

		List<String> list = new ArrayList<>();
		list.add(TagLister.ListType.ALBUM_ARTIST.getType());
		list.add(name);

		MPDAlbumArtist artist = null;
		List<MPDAlbumArtist> artists = new ArrayList<>(tagLister.list(TagLister.ListType.ALBUM_ARTIST, list)
				.stream()
				.map(s -> new MPDAlbumArtist(convertResponse(s)))
				.collect(Collectors.toList()));

		if (artists.size() > 1) {
			LOGGER.warn("Multiple artists returned for name {}", name);
		}

		if (!artists.isEmpty()) {
			artist = artists.get(0);
		}

		return artist;
	}

	private static String convertResponse(String s) {
		return s.substring(s.split(":")[0].length() + 1).trim();
	}
}
