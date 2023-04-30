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

import com.mandarina.match.FileMatcher;
import com.mandarina.match.MimeMatcher;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class Organizer {

	private boolean copy;
	private Path sourcePath;
	private Path targetPath;

	private Path failPath;
	private Path mediaunmatchPath;
	private Path unmatchPath;
	private Path matchPath;

	private Button orgButton;
	private Button stopButton;
	private TextField statusField;

	private Task<Void> copyTask;
	private int copiedFilesCount;

	public Organizer(boolean copy, Path sourcePath, Path targetPath, Button orgButton, Button stopButton,
			TextField statusField) {
		this.copy = copy;
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.orgButton = orgButton;
		this.stopButton = stopButton;
		this.statusField = statusField;
	}

	public void organize() throws IOException {

		initPaths();

		copiedFilesCount = 0;

		stopButton.setOnAction(e -> {
			if (copyTask != null) {
				copyTask.cancel();
			}
		});

		copyTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				Platform.runLater(() -> {
					orgButton.setDisable(true);
					stopButton.setDisable(false);
				});

				Thread.sleep(100);
				Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {

					/*
					 * Copy the directories.
					 */
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

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
				return null;
			}
		};

		statusField.textProperty().bind(copyTask.messageProperty());

		copyTask.setOnFailed(e -> doTaskEventCloseRoutine());
		copyTask.setOnCancelled(e -> doTaskEventCloseRoutine());
		copyTask.setOnSucceeded(e -> doTaskEventCloseRoutine());

		new Thread(copyTask).start(); // Run the copy task
	}

	private void initPaths() throws IOException {
		createDirectories(targetPath);

		failPath = targetPath.resolve("fail.txt");
		recreateFile(failPath);

		mediaunmatchPath = targetPath.resolve("mediaunmatch.txt");
		recreateFile(mediaunmatchPath);

		unmatchPath = targetPath.resolve("unmatch.txt");
		recreateFile(unmatchPath);

		matchPath = targetPath.resolve("match.txt");
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

	private void appendFileName(Path path, Path matchPath) throws IOException {
		Files.write(matchPath, getBytes(path), StandardOpenOption.APPEND);
	}

	private void copy(SimpleEntry<MimeMatcher, LocalDate> match, Path path) throws IOException {
		var datePath = Paths.get(targetPath.toString(), match.getKey().getLabel(),
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

	private static byte[] getBytes(Path path) {
		return path.toString().concat(System.lineSeparator()).getBytes();
	}

	private static void recreateFile(Path p) throws IOException {
		if (!Files.exists(p)) {
			Files.createFile(p);
		} else {
			Files.delete(p);
			Files.createFile(p);
		}
	}

	private void doTaskEventCloseRoutine() {
		statusField.textProperty().unbind();
		statusField.setText(copy ? "Copied Files: " + copiedFilesCount : "Processed Files: " + copiedFilesCount);
		Platform.runLater(() -> {
			orgButton.setDisable(false);
			stopButton.setDisable(true);
		});
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
		if (sourcePath.equals(targetPath)) {
			return "Warning: sourceFolder and targetFolder can not be the same folder";
		} else {
			Path parent = targetPath.getParent();
			while (parent != null) {
				if (sourcePath.equals(parent)) {
					return "Warning: targetFolder can not be inside sourceFolder";
				}
				parent = parent.getParent();
			}
		}
		return null;
	}

	private static boolean isDirectory(Path p) {
		return Files.exists(p) && Files.isDirectory(p);
	}
}
