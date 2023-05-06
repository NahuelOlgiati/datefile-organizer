package com.mandarina.match;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;

public class FileMatcher {

	public static SimpleEntry<CategoryMatcher, LocalDate> match(Path path) {
		CategoryMatcher categoryMatch = CategoryMatcher.get(path);
		LocalDate localDate = null;
		if (categoryMatch != null) {
			localDate = NameDateMatcher.get(path);
			if (localDate != null) {
				return new AbstractMap.SimpleEntry<CategoryMatcher, LocalDate>(categoryMatch, localDate);
			} else {
				localDate = MetadataDateMatcher.get(path);
				if (localDate != null) {
					return new AbstractMap.SimpleEntry<CategoryMatcher, LocalDate>(categoryMatch, localDate);
				}
			}
		}
		return null;
	}
}
