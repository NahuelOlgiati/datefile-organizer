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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mandarina.match.FileMatcher;
import com.mandarina.match.MimeMatcher;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public abstract class Organizer {

	protected boolean copy;
	protected ObservableList<String> listViewItems;
	protected String targetField;

	protected Task<Void> copyTask;
	protected int copiedFilesCount;

	private Path failPath;
	private Path mediaunmatchPath;
	private Path unmatchPath;
	private Path matchPath;

	public Organizer(boolean copy, ObservableList<String> listViewItems, String targetField) {
		this.copy = copy;
		this.listViewItems = listViewItems;
		this.targetField = targetField;
	}

	public abstract void doBeforeOrganize();

	public abstract void doAfterOrganize();

	public void organize() throws Exception {
		initLogs();
		copiedFilesCount = 0;
		copyTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				Thread.sleep(100);
				for (String s : listViewItems) {
					Files.walkFileTree(Paths.get(s), new SimpleFileVisitor<Path>() {

						/*
						 * Copy the directories.
						 */
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
								throws IOException {

							if (isCancelled()) {
								return FileVisitResult.TERMINATE;
							}

							return FileVisitResult.CONTINUE;
						}

						/*
						 * Copy the files.
						 */
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

							if (isCancelled()) {
								return FileVisitResult.TERMINATE;
							}

							organize(copy, file);
							copiedFilesCount++;
							updateMessage(file.toString());

							return FileVisitResult.CONTINUE;
						}
					});
				}
				return null;
			}
		};

		doBeforeOrganize();

		copyTask.setOnFailed(e -> doAfterOrganize());
		copyTask.setOnCancelled(e -> doAfterOrganize());
		copyTask.setOnSucceeded(e -> doAfterOrganize());

		new Thread(copyTask).start(); // Run the copy task
	}

	protected void initLogs() throws IOException {
		var logPath = Paths.get(targetField)//
				.resolve(".logs")//
				.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));
		createDirectories(logPath);

		var mainPath = logPath.resolve("main.log");
		recreateFile(mainPath);
		appendLine("dateTime: " + LocalDateTime.now(), mainPath);
		appendLine("sources: " + listViewItems, mainPath);
		appendLine("target: " + targetField, mainPath);
		appendLine("copy: " + copy, mainPath);

		failPath = logPath.resolve("fail.log");
		recreateFile(failPath);

		unmatchPath = logPath.resolve("unmatch.log");
		recreateFile(unmatchPath);

		mediaunmatchPath = logPath.resolve("unmatch-media.log");
		recreateFile(mediaunmatchPath);

		matchPath = logPath.resolve("match-media.log");
		recreateFile(matchPath);
	}

	private void organize(boolean copy, Path path) {
		try {
			var match = FileMatcher.match(path);
			if (match != null) {
				if (match.getKey() != null) {
					appendFileName(path, matchPath);
					if (copy) {
						copy(match, path);
					}
				} else {
					appendFileName(path, mediaunmatchPath);
				}
			} else {
				appendFileName(path, unmatchPath);
			}
		} catch (Exception e) {
			try {
				appendFileName(path, failPath);
			} catch (IOException e1) {
			}
		}
	}

	private void appendFileName(Path fileNamePath, Path targetPath) throws IOException {
		appendLine(fileNamePath.toString(), targetPath);
	}

	private void appendLine(String value, Path matchPath) throws IOException {
		Files.write(matchPath, getLine(value), StandardOpenOption.APPEND);
	}

	private void copy(SimpleEntry<MimeMatcher, LocalDate> match, Path path) throws IOException {
		var datePath = Paths.get(targetField, match.getKey().getLabel(),
				match.getValue().getYear() + "-" + String.format("%02d", match.getValue().getMonthValue()),
				path.getFileName().toString());
		createDirectories(datePath.getParent());
		Files.copy(path, datePath, StandardCopyOption.REPLACE_EXISTING);
	}

	private static void createDirectories(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createDirectories(p);
		}
	}

	private static byte[] getLine(String value) {
		return value.concat(System.lineSeparator()).getBytes();
	}

	private static void recreateFile(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createFile(p);
		} else {
			Files.delete(p);
			Files.createFile(p);
		}
	}

	public static String valid(List<String> listViewItems, String targetFieldText) {
		if (listViewItems.isEmpty()) {
			return "Required: source folder";
		}
		if (targetFieldText == null || targetFieldText.isEmpty()) {
			return "Required: target folder";
		}
		Path targetPath = Paths.get(targetFieldText);
		if (!isDirectory(targetPath)) {
			return "Parameter targetFolder: is not a directory";
		}
		for (Path sourcePath : listViewItems.stream()//
				.map(Paths::get)//
				.collect(Collectors.toList())) {
			if (!isDirectory(sourcePath)) {
				return "Parameter sourceFolder: is not a directory";
			}
			if (Collections.frequency(listViewItems, sourcePath.toString()) > 1) {
				return "Parameter sourceFolder: directory repeated";
			}
			if (sourcePath.equals(targetPath)) {
				return "SourceFolder and targetFolder can not be the same folder";
			} else {
				Path parent = targetPath.getParent();
				while (parent != null) {
					if (sourcePath.equals(parent)) {
						return "TargetFolder can not be inside sourceFolder";
					}
					parent = parent.getParent();
				}
			}
		}
		return null;
	}

	private static boolean isDirectory(Path p) {
		return Files.exists(p) && Files.isDirectory(p);
	}
}
