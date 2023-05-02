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
import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.atomic.AtomicLong;

import com.mandarina.match.MimeMatcher;

import javafx.collections.ObservableList;

public class FileUtil {

	public static void appendFileName(Path fileNamePath, Path targetPath) throws IOException {
		appendLine(fileNamePath.toString(), targetPath);
	}

	public static void appendLine(String value, Path matchPath) throws IOException {
		Files.write(matchPath, getLine(value), StandardOpenOption.APPEND);
	}

	public static void copy(SimpleEntry<MimeMatcher, LocalDate> match, Path path, Path targetPath) throws IOException {
		var datePath = Paths.get(targetPath.toString(), match.getKey().getLabel(),
				match.getValue().getYear() + "-" + String.format("%02d", match.getValue().getMonthValue()),
				path.getFileName().toString());
		createDirectories(datePath.getParent());
		Files.copy(path, datePath, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void createDirectories(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createDirectories(p);
		}
	}

	public static byte[] getLine(String value) {
		return value.concat(System.lineSeparator()).getBytes();
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

	public static long getFolderWeight(ObservableList<String> listViewItems, AtomicLong totalWeight)
			throws IOException {
		for (String folderPath : listViewItems) {
			Files.walkFileTree(Paths.get(folderPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					totalWeight.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return totalWeight.get();
	}

	public static long getFreeSpaceInBytes(Path path) throws IOException {
		return Files.getFileStore(path).getUsableSpace();
	}

	public static boolean haveFreeSpace(Path path, long sizeToCopy) throws IOException {
		return getFreeSpaceInBytes(path) > sizeToCopy;
	}
}
