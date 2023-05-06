package com.mandarina.match;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public enum MetadataDateMatcher {
	LARGE("yyyyMMdd"), //
	SHORT("yyMMdd");

	private String format;

	MetadataDateMatcher(String format) {
		this.format = format;
	}

	public static LocalDate get(Path path) {
		LocalDate r = null;
		try (InputStream is = Files.newInputStream(path)) {
			Metadata metadata = ImageMetadataReader.readMetadata(is);
			outerloop: for (Directory d : metadata.getDirectories()) {
				for (Tag t : d.getTags()) {
					if (isDateCreationMetada(t)) {
						var localDate = getLocalDate(t);
						if (localDate != null) {
							r = localDate;
							break outerloop;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return r;
	}

	private static boolean isDateCreationMetada(Tag tag) {
		var tagName = tag.getTagName();
		return tagName != null && tagName.toUpperCase().contains("DATE") && //
				(tagName.toUpperCase().contains("CREAT") || tagName.toUpperCase().contains("ORIGIN"));
	}

	private static LocalDate getLocalDate(Tag tag) {
		LocalDate r = null;
		var tagDescription = tag.getDescription();
		if (tagDescription != null) {
			var digits = tagDescription.replaceAll("\\D+", "");
			for (MetadataDateMatcher df : values()) {
				try {
					var localDate = LocalDate.parse(digits.substring(0, df.format.length()),
							DateTimeFormatter.ofPattern(df.format));
					if (localDate != null) {
						r = localDate;
						break;
					}
				} catch (Exception e) {
				}
			}
		}
		return r;
	}
}