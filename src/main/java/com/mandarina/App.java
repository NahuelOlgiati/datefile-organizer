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

public class App {

	// java -jar pepe-0.0.1-SNAPSHOT.jar /home/nahuel/Desktop/temp
	// /home/nahuel/Desktop/temp/done copy
	public static void main(String[] args) throws Exception {
		System.out.println("Hello World!");
		if (args.length < 2) {
			throw new Exception("Parametros requeridos: sourceFolder targetFolder");
		}
		System.out.println(args[0]);
		System.out.println(args[1]);

		String sourceFolderPath = args[0];
		String targetFolderPath = args[1];

		String copyStr = args.length > 2 ? args[2] : "false";
		boolean copy = copyStr != null && copyStr.equals("copy");

		var sourcePath = Paths.get(sourceFolderPath);
		if (!isDirectory(sourcePath)) {
			throw new Exception("Parametro targetFolder: is not a directory");
		}

		var targetPath = Paths.get(targetFolderPath);
		if (!isDirectory(targetPath)) {
			throw new Exception("Parametro targetFolder: is not a directory");
		}
		createDirectories(targetPath);

		var unmatchPath = targetPath.resolve("unmatch.txt");
		recreateFile(unmatchPath);

		var matchPath = targetPath.resolve("match.txt");
		recreateFile(matchPath);

		try (Stream<Path> s = Files.find(sourcePath, Integer.MAX_VALUE, isRegularFile())) {
			s.forEach(p -> {
				System.out.println(p);
				var match = FileNameMatcher.match(p);
				try {
					org(match, p, targetPath, matchPath, unmatchPath, copy);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		System.out.println("Finito!");
	}

	private static BiPredicate<Path, BasicFileAttributes> isRegularFile() {
		return (filePath, fileAttr) -> fileAttr.isRegularFile();
	}

	private static void org(AbstractMap.SimpleEntry<FileNameType, LocalDate> match, Path path, Path workPath,
			Path matchPath, Path unmatchPath,
			boolean copy) throws IOException {
		if (match != null) {
			var matcher = match.getKey();
			var dateMatch = match.getValue();
			if (dateMatch != null) {
				Files.write(matchPath, getBytes(path), StandardOpenOption.APPEND);
				if (copy) {
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

	private static boolean isDirectory(Path p) {
		return Files.exists(p) && Files.isDirectory(p);
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
