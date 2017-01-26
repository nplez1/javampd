package org.bff.javampd.albumartist;

import org.bff.javampd.MPDItem;

/**
 * String represents an album artist
 *
 * @author nathanp
 */
public class MPDAlbumArtist extends MPDItem {

	/**
	 * Creates a new album artist
	 *
	 * @param name the name of the album artist
	 */
	public MPDAlbumArtist(String name) {
		setName(name);
	}
}
