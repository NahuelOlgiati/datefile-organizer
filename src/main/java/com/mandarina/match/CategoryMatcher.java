package com.mandarina.match;

import java.nio.file.Files;
import java.nio.file.Path;

public enum CategoryMatcher {
	VIDEO("video", "Videos"), //
	IMAGE("image", "Images"), //,
	AUDIO("audio", "Audios");

	private String mime;
	private String label;

	CategoryMatcher(String mime, String label) {
		this.mime = mime;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static CategoryMatcher get(Path path) {
		CategoryMatcher r = null;
		try {
			String mimeType = Files.probeContentType(path);
			for (CategoryMatcher vi : values()) {
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