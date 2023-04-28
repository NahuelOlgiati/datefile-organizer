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

import com.mandarina.FileMatcher.VideoImage;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class Organizer {

	private boolean copy;
	private Path sourcePath;
	private Path targetPath;
	private Button orgButton;
	private Button stopButton;
	private Label statusLabel;

	private Task<Void> copyTask;
	private int copiedFilesCount;

	public Organizer(boolean copy, Path sourcePath, Path targetPath, Button orgButton, Button stopButton,
			Label statusLabel) {
		this.copy = copy;
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.orgButton = orgButton;
		this.stopButton = stopButton;
		this.statusLabel = statusLabel;
	}

	public void copyRoutine() throws IOException {

		createDirectories(targetPath);

		var unmatchPath = targetPath.resolve("unmatch.txt");
		recreateFile(unmatchPath);

		var matchPath = targetPath.resolve("match.txt");
		recreateFile(matchPath);

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

						org(copy, file, targetPath, matchPath, unmatchPath);
						copiedFilesCount++;
						updateMessage(file.toString());

						return FileVisitResult.CONTINUE;
					}
				});

				return null;
			}
		};

		statusLabel.textProperty().bind(copyTask.messageProperty());

		copyTask.setOnFailed(e -> doTaskEventCloseRoutine());
		copyTask.setOnCancelled(e -> doTaskEventCloseRoutine());
		copyTask.setOnSucceeded(e -> doTaskEventCloseRoutine());

		new Thread(copyTask).start(); // Run the copy task
	}

	private static void org(boolean copy, Path path, Path workPath, Path matchPath, Path unmatchPath)
			throws IOException {
		var match = FileMatcher.match(path);
		if (match != null) {
			appendFileName(path, matchPath);
			if (copy) {
				copy(match, path, workPath);
			}
		} else {
			appendFileName(path, unmatchPath);
		}
	}

	private static void appendFileName(Path path, Path matchPath) throws IOException {
		Files.write(matchPath, getBytes(path), StandardOpenOption.APPEND);
	}

	private static void copy(SimpleEntry<VideoImage, LocalDate> match, Path path, Path workPath) throws IOException {
		var datePath = Paths.get(workPath.toString(), match.getKey().getLabel(),
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
		statusLabel.textProperty().unbind();
		statusLabel.setText(copy ? "Copied Files: " + copiedFilesCount : "Processed Files: " + copiedFilesCount);
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
		return null;
	}

	private static boolean isDirectory(Path p) {
		return Files.exists(p) && Files.isDirectory(p);
	}
}
