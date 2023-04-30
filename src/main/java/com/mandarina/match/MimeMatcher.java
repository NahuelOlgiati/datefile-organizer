package com.mandarina.match;

import java.nio.file.Files;
import java.nio.file.Path;

public enum MimeMatcher {
	VIDEO("video", "Videos"), //
	IMAGE("image", "Images"), //,
	AUDIO("audio", "Audios");

	private String mime;
	private String label;

	MimeMatcher(String mime, String label) {
		this.mime = mime;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static MimeMatcher match(Path path) {
		MimeMatcher r = null;
		try {
			String mimeType = Files.probeContentType(path);
			for (MimeMatcher vi : values()) {
				if (mimeType.contains(vi.mime)) {
					r = vi;
					break;
				}
			}
		} catch (Exception e) {
		}
		return r;
	}
}