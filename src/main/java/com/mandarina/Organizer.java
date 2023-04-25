package com.mandarina;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.Date;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.mandarina.FileNameMatcher.FileNameType;

import javafx.scene.control.Label;

public class Organizer {

	public static void process(boolean copy, Path sourcePath, Path targetPath, Label statusLabel) throws IOException {
		createDirectories(targetPath);

		var unmatchPath = targetPath.resolve("unmatch.txt");
		recreateFile(unmatchPath);

		var matchPath = targetPath.resolve("match.txt");
		recreateFile(matchPath);

		try (Stream<Path> s = Files.find(sourcePath, Integer.MAX_VALUE, isRegularFile())) {
			s.forEach(p -> {
				System.out.println(p);
				statusLabel.setText("Processing: " + p);
				var match = FileNameMatcher.match(p);
				try {
					org(match, p, targetPath, matchPath, unmatchPath, copy, statusLabel);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	public static String valid(String sourceText, String targetText, Path sourcePath, Path targetPath) {
		if (sourceText == null || sourceText.isEmpty()) {
			return "Required: source folder";
		}
		if (targetText == null || targetText.isEmpty()) {
			return "Required: target folder";
		}
		if (!isDirectory(sourcePath)) {
			return "Parametro targetFolder: is not a directory";
		}
		if (!isDirectory(targetPath)) {
			return "Parametro targetFolder: is not a directory";
		}
		return null;
	}

	private static boolean isDirectory(Path p) {
		return Files.exists(p) && Files.isDirectory(p);
	}

	private static BiPredicate<Path, BasicFileAttributes> isRegularFile() {
		return (filePath, fileAttr) -> fileAttr.isRegularFile();
	}

	private static void org(AbstractMap.SimpleEntry<FileNameType, LocalDate> match, Path path, Path workPath,
			Path matchPath, Path unmatchPath, boolean copy, Label statusLabel) throws IOException {
		if (match != null) {
			var matcher = match.getKey();
			var dateMatch = match.getValue();
			if (dateMatch != null) {
				Files.write(matchPath, getBytes(path), StandardOpenOption.APPEND);
				if (copy) {
					statusLabel.setText("Copying: " + path);
					copy(matcher, dateMatch, path, workPath);
				}
			} else {
				Files.write(unmatchPath, getBytes(path), StandardOpenOption.APPEND);
			}
		} else {
			Files.write(unmatchPath, getBytes(path), StandardOpenOption.APPEND);
		}
	}

	private static void copy(FileNameType matcher, LocalDate dateMatch, Path path, Path workPath) throws IOException {
		var datePath = Paths.get(workPath.toString(), matcher.getVideoImage().getLabel(),
				dateMatch.getYear() + "-" + String.format("%02d", dateMatch.getMonthValue()),
				path.getFileName().toString());
		createDirectories(datePath.getParent());
		Files.copy(path, datePath, StandardCopyOption.REPLACE_EXISTING);
	}

	public LocalDate dateToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	private static byte[] getBytes(Path path) {
		return path.toString().concat(System.lineSeparator()).getBytes();
	}

	private static void createDirectories(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createDirectories(p);
		}
	}

	private static void recreateFile(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createFile(p);
		} else {
			Files.delete(p);
			Files.createFile(p);
		}
	}
}
