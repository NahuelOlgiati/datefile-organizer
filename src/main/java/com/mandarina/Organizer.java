package com.mandarina;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mandarina.match.FileMatcher;

import javafx.collections.ObservableList;

public abstract class Organizer {

	protected boolean copy;
	protected ObservableList<String> listViewItems;
	protected Path targetPath;

	protected SizeCalculationTask sizeCalculationTask;
	protected OrganizeTask organizeTask;

	private Path notCopiedFailPath;
	private Path notCopiedMediaPath;
	private Path notCopiedNotMediaPath;
	private Path notCopiedRpeatedPath;
	private Path copiedMediaPath;
	private Path copiedMediaNameConflictPath;

	private long totalSizeToCopy;

	public Organizer(boolean copy, ObservableList<String> listViewItems, Path targetPath) {
		this.copy = copy;
		this.listViewItems = listViewItems;
		this.targetPath = targetPath;
	}

	public abstract void doBeforeSizeCalculation();

	public abstract void doAfterSizeCalculation();

	public abstract void doBeforeOrganize();

	public abstract void doAfterOrganize();

	public abstract void message(String msg);

	public void organize() throws Exception {
		startSizeCalculationTask();
	}

	private void startSizeCalculationTask() {
		sizeCalculationTask = new SizeCalculationTask(listViewItems);
		sizeCalculationTask.setOnFailed(e -> doAfterSizeCalculation());
		sizeCalculationTask.setOnCancelled(e -> doAfterSizeCalculation());
		sizeCalculationTask.setOnSucceeded(e -> doAfterSizeCalculation());
		sizeCalculationTask.setOnSucceeded(event -> {
			totalSizeToCopy = sizeCalculationTask.getValue();
			try {
				if (FileUtil.hasFreeSpace(targetPath, totalSizeToCopy)) {
					startOrganizeTask();
				} else {
					message("Not enough disk space to perform the task");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		doBeforeSizeCalculation();
		new Thread(sizeCalculationTask).start();
	}

	private void startOrganizeTask() throws IOException {
		initLogs();
		organizeTask = new OrganizeTask(listViewItems, totalSizeToCopy) {

			@Override
			public void organize(Path file) {
				Organizer.this.organize(copy, file);
			}
		};

		organizeTask.setOnFailed(e -> doAfterOrganize());
		organizeTask.setOnCancelled(e -> doAfterOrganize());
		organizeTask.setOnSucceeded(e -> doAfterOrganize());

		doBeforeOrganize();
		new Thread(organizeTask).start();
	}

	protected void initLogs() throws IOException {
		var logPath = targetPath.resolve(".logs")//
				.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));
		FileUtil.createDirectories(logPath);

		var mainPath = logPath.resolve("main.log");
		FileUtil.recreateFile(mainPath);
		FileUtil.appendLine("dateTime: " + LocalDateTime.now(), mainPath);
		FileUtil.appendLine("sources: " + listViewItems, mainPath);
		FileUtil.appendLine("target: " + targetPath, mainPath);
		FileUtil.appendLine("copy: " + copy, mainPath);

		notCopiedFailPath = logPath.resolve("not-copied-fail.log");
		FileUtil.recreateFile(notCopiedFailPath);

		notCopiedMediaPath = logPath.resolve("not-copied-media.log");
		FileUtil.recreateFile(notCopiedMediaPath);

		notCopiedNotMediaPath = logPath.resolve("not-copied-not-media.log");
		FileUtil.recreateFile(notCopiedNotMediaPath);

		notCopiedRpeatedPath = logPath.resolve("not-copied-repeated.log");
		FileUtil.recreateFile(notCopiedRpeatedPath);

		copiedMediaPath = logPath.resolve("copied-media.log");
		FileUtil.recreateFile(copiedMediaPath);

		copiedMediaNameConflictPath = logPath.resolve("copied-media-name-conflict.log");
		FileUtil.recreateFile(copiedMediaNameConflictPath);
	}

	private void organize(boolean copy, Path path) {
		try {
			var match = FileMatcher.match(path);
			if (match != null && match.getKey() != null) {
				var dateMatch = match.getValue();
				if (dateMatch != null) {
					if (copy) {
						var mimeMatch = match.getKey();
						var datePath = FileUtil.getDatePath(mimeMatch, dateMatch, path, targetPath);
						if (FileUtil.isRegularFile(datePath)) {
							boolean sameFile = FileUtil.sameFile(path, datePath);
							if (sameFile) {
								FileUtil.appendFileName(path, notCopiedRpeatedPath);
							} else {
								Path newDatePath = FileUtil.getIncrementedFilename(datePath);
								FileUtil.appendFileName(datePath, copiedMediaNameConflictPath);
								FileUtil.appendFileName(newDatePath, copiedMediaNameConflictPath);
								FileUtil.appendSeparator(copiedMediaNameConflictPath);
								FileUtil.createFolderAndCopy(newDatePath, path);
							}
						} else {
							FileUtil.appendFileName(path, copiedMediaPath);
							FileUtil.createFolderAndCopy(datePath, path);
						}
					}
				} else {
					FileUtil.appendFileName(path, notCopiedMediaPath);
				}
			} else {
				FileUtil.appendFileName(path, notCopiedNotMediaPath);
			}
		} catch (Exception e) {
			try {
				FileUtil.appendFileName(path, notCopiedFailPath);
			} catch (IOException e1) {
			}
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
		if (!FileUtil.isDirectory(targetPath)) {
			return "Parameter targetFolder: is not a directory";
		}
		for (Path sourcePath : listViewItems.stream()//
				.map(Paths::get)//
				.collect(Collectors.toList())) {
			if (!FileUtil.isDirectory(sourcePath)) {
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
}
