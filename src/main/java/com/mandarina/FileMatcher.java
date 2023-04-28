package com.mandarina;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileMatcher {

	public static SimpleEntry<VideoImage, LocalDate> match(Path path) {
		VideoImage videoImage = VideoImage.get(path);
		if (videoImage != null) {
			LocalDate localDate = DateFormat.get(path);
			if (localDate != null) {
				return new AbstractMap.SimpleEntry<VideoImage, LocalDate>(videoImage, localDate);
			}
		}
		return null;
	}

	public enum DateFormat {
		SHORT("yyMMdd"), //
		LARGE("yyyyMMdd");

		private String format;

		DateFormat(String format) {
			this.format = format;
		}

		public static LocalDate get(Path path) {
			LocalDate r = null;
			for (String n : getNumbers(path)) {
				for (DateFormat df : values()) {
					var localDate = getLocalDate(df, n);
					if (localDate != null) {
						r = localDate;
						break;
					}
				}
			}
			return r;
		}

		private static List<String> getNumbers(Path path) {
			var fn = path.getFileName().toString();
			var matcher = Pattern.compile("\\d+").matcher(fn);
			List<String> numbers = new ArrayList<>();
			while (matcher.find()) {
				numbers.add(matcher.group());
			}
			return numbers;
		}

		private static LocalDate getLocalDate(DateFormat df, String n) {
			LocalDate localDate = null;
			if (df.format.length() == n.length()) {
				try {
					localDate = LocalDate.parse(n, DateTimeFormatter.ofPattern(df.format));
				} catch (Exception e) {
				}
			}
			return localDate;
		}
	}

	public enum VideoImage {
		VIDEO("video", "Videos"), //
		IMAGE("image", "Images");

		private String mime;
		private String label;

		VideoImage(String mime, String label) {
			this.mime = mime;
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public static VideoImage get(Path path) {
			VideoImage r = null;
			try {
				String mimeType = Files.probeContentType(path);
				for (VideoImage vi : values()) {
					if (mimeType.contains(vi.mime)) {
						r = vi;
						break;
					}
				}
			} catch (IOException e) {
			}
			return r;
		}
	}
}
