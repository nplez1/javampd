package org.bff.javampd.processor;

import org.bff.javampd.song.MPDSong;

/**
 * @author nathanp
 */
public class AlbumArtistTagProcessor extends TagResponseProcessor implements
		SongTagResponseProcessor {

	public AlbumArtistTagProcessor() {
		super("AlbumArtist:");
	}

	@Override
	public void processTag(MPDSong song, String line) {
		if (startsWith(line)) {
			song.setAlbumArtistName(line.substring(getPrefix().length()).trim());
		}
	}
}
