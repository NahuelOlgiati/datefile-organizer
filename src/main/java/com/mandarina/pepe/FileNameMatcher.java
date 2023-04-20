package com.mandarina.pepe;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FileNameMatcher {

	public static LocalDate match(Path path) {
		for (FileNameType m : FileNameType.values()) {
			if (m.matchExt(path)) {
				LocalDate matchDate = m.matchDate(path);
				if (matchDate != null) {
					return matchDate;
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
			return FileSystems.getDefault().getPathMatcher("glob:/**/*.".concat(this.getExt())).matches(path);
		}
	}

	private enum VideoImage {
		VIDEO, //
		IMAGE;
	}
}
