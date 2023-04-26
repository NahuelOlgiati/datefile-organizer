package com.mandarina;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;

import org.apache.commons.io.FilenameUtils;

public class FileNameMatcher {

	public static AbstractMap.SimpleEntry<FileNameType, LocalDate> match(Path path) {
		for (FileNameType m : FileNameType.values()) {
			if (m.matchExt(path)) {
				LocalDate matchDate = m.matchDate(path);
				if (matchDate != null) {
					return new AbstractMap.SimpleEntry<FileNameType, LocalDate>(m, matchDate);
				}
			}
		}
		return null;
	}

	public enum FileNameType {
		_1("", "yyyyMMdd", "jpg", VideoImage.IMAGE), //
		_2("IMG-", "yyyyMMdd", "jpg", VideoImage.IMAGE), //
		_3("", "yyyyMMdd", "mp4", VideoImage.VIDEO), //
		_4("VID-", "yyyyMMdd", "mp4", VideoImage.VIDEO);

		private String prefix;
		private String datePattern;
		private String ext;
		private VideoImage videoImage;

		FileNameType(String prefix, String datePattern, String ext, VideoImage videoImage) {
			this.prefix = prefix;
			this.datePattern = datePattern;
			this.ext = ext;
			this.videoImage = videoImage;
		}

		public String getPrefix() {
			return prefix;
		}

		public String getDatePattern() {
			return datePattern;
		}

		public String getExt() {
			return ext;
		}

		public VideoImage getVideoImage() {
			return videoImage;
		}

		private String getStr(String fileName) {
			return fileName.substring(this.getPrefix().length(),
					this.getPrefix().length() + this.getDatePattern().length());
		}

		public LocalDate matchDate(Path path) {
			LocalDate localDate = null;
			try {
				var strDate = getStr(path.getFileName().toString());
				var dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				localDate = LocalDate.parse(strDate, dateFormatter);
			} catch (Exception e) {
			}
			return localDate;
		}

		public boolean matchExt(Path path) {
			return FilenameUtils.getExtension(path.getFileName().toString()).equals(this.getExt());
		}
	}

	public enum VideoImage {
		VIDEO("Videos"), //
		IMAGE("Images");

		private String label;

		VideoImage(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}
}
