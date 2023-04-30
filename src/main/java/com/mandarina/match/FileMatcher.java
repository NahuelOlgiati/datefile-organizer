package com.mandarina.match;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;

public class FileMatcher {

	public static SimpleEntry<MimeMatcher, LocalDate> match(Path path) {
		MimeMatcher mimeMatch = MimeMatcher.match(path);
		if (mimeMatch != null) {
			LocalDate localDate = DateFormat.get(path);
			return new AbstractMap.SimpleEntry<MimeMatcher, LocalDate>(mimeMatch, localDate);
		}
		return null;
	}
}
