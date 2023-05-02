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

	private Path failPath;
	private Path mediaunmatchPath;
	private Path unmatchPath;
	private Path matchPath;

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
				if (FileUtil.haveFreeSpace(targetPath, totalSizeToCopy)) {
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

		failPath = logPath.resolve("fail.log");
		FileUtil.recreateFile(failPath);

		unmatchPath = logPath.resolve("unmatch.log");
		FileUtil.recreateFile(unmatchPath);

		mediaunmatchPath = logPath.resolve("unmatch-media.log");
		FileUtil.recreateFile(mediaunmatchPath);

		matchPath = logPath.resolve("match-media.log");
		FileUtil.recreateFile(matchPath);
	}

	private void organize(boolean copy, Path path) {
		try {
			var match = FileMatcher.match(path);
			if (match != null) {
				if (match.getKey() != null) {
					FileUtil.appendFileName(path, matchPath);
					if (copy) {
						FileUtil.copy(match, path, targetPath);
					}
				} else {
					FileUtil.appendFileName(path, mediaunmatchPath);
				}
			} else {
				FileUtil.appendFileName(path, unmatchPath);
			}
		} catch (Exception e) {
			try {
				FileUtil.appendFileName(path, failPath);
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
