package com.mandarina;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import com.mandarina.match.MimeMatcher;

import javafx.collections.ObservableList;

public class FileUtil {

	public static void appendFileName(Path fileNamePath, Path appendTo) throws IOException {
		appendLine(fileNamePath.toString(), appendTo);
	}

	public static void appendLine(String value, Path appendTo) throws IOException {
		Files.write(appendTo, getLine(value), StandardOpenOption.APPEND);
	}

	public static void appendSeparator(Path appendTo) throws IOException {
		Files.write(appendTo, getSeparator().getBytes(), StandardOpenOption.APPEND);
	}

	public static void createFolderAndCopy(Path datePath, Path path) throws IOException {
		createDirectories(datePath.getParent());
		Files.copy(path, datePath, StandardCopyOption.REPLACE_EXISTING);
	}

	public static Path getDatePath(MimeMatcher mimeMatch, LocalDate dateMatch, Path path, Path targetPath) {
		return Paths.get(targetPath.toString(), mimeMatch.getLabel(),
				dateMatch.getYear() + "-" + String.format("%02d", dateMatch.getMonthValue()),
				path.getFileName().toString());
	}

	public static void createDirectories(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createDirectories(p);
		}
	}

	public static byte[] getLine(String value) {
		return value.concat(getSeparator()).getBytes();
	}

	private static String getSeparator() {
		return System.lineSeparator();
	}

	public static void recreateFile(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createFile(p);
		} else {
			Files.delete(p);
			Files.createFile(p);
		}
	}

	public static boolean isDirectory(Path p) {
		return Files.exists(p) && Files.isDirectory(p);
	}

	public static boolean isRegularFile(Path p) {
		return Files.exists(p) && Files.isRegularFile(p);
	}

	public static long getFolderSize(ObservableList<String> listViewItems, AtomicLong totalSize) throws IOException {
		for (String folderPath : listViewItems) {
			Files.walkFileTree(Paths.get(folderPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					totalSize.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return totalSize.get();
	}

	public static long getFreeSpaceInBytes(Path path) throws IOException {
		return Files.getFileStore(path).getUsableSpace();
	}

	public static boolean hasFreeSpace(Path path, long sizeToCopy) throws IOException {
		return getFreeSpaceInBytes(path) > sizeToCopy;
	}

	public static boolean sameFile(Path a, Path b) throws Exception {
		var aBytes = computeChecksum(a);
		var bBytes = computeChecksum(b);
		if (aBytes.length != bBytes.length) {
			return false;
		}
		return IntStream.range(0, aBytes.length).allMatch(i -> aBytes[i] == bBytes[i]);
	}

	public static byte[] computeChecksum(Path path) throws Exception {
		return MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
	}

	public static Path getIncrementedFilename(Path path) {
		int i = 1;
		Path parent = path.getParent();
		String filename = path.getFileName().toString();
		while (true) {
			String newFilename = getIncrementedFileNameWithNumber(filename, i);
			Path newFilePath = parent.resolve(newFilename);
			if (!Files.exists(newFilePath)) {
				return newFilePath;
			}
			i++;
		}
	}

	private static String getIncrementedFileNameWithNumber(String filename, int i) {
		int dotIndex = filename.lastIndexOf(".");
		String name = dotIndex == -1 ? filename : filename.substring(0, dotIndex);
		String extension = dotIndex == -1 ? "" : filename.substring(dotIndex + 1);
		return name + "(" + i + ")" + (extension.isEmpty() ? "" : "." + extension);
	}
}
