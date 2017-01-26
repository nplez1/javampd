package org.bff.javampd.albumartist;

import org.bff.javampd.genre.MPDGenre;

import java.util.Collection;

/**
 * @author nathanp
 */
public interface AlbumArtistDatabase {
	/**
	 * Returns a {@link java.util.Collection} of {@link MPDAlbumArtist}s of all
	 * album artists in the database.
	 *
	 * @return a {@link java.util.Collection} of {@link MPDAlbumArtist}s containing the album names
	 */
	Collection<MPDAlbumArtist> listAll();

	/**
	 * Returns a {@link java.util.Collection} of {@link MPDAlbumArtist}s of all
	 * album artists by a particular genre.
	 *
	 * @param genre the genre to find album artists
	 * @return a {@link java.util.Collection} of {@link MPDAlbumArtist}s of all
	 * album artists
	 */
	Collection<MPDAlbumArtist> listByGenre(MPDGenre genre);

	/**
	 * Returns a {@link MPDAlbumArtist} with the passed name.
	 *
	 * @param name the name of the album artist
	 * @return a {@link org.bff.javampd.artist.MPDArtist}
	 */
	MPDAlbumArtist listByName(String name);
}
