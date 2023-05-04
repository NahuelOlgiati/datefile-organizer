package com.mandarina;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public abstract class OrganizeTask extends Task<Void> {

	protected AtomicInteger processedFileCount;
	protected AtomicLong totalSize;
	protected ObservableList<String> listViewItems;
	protected long totalSizeToCopy;

	public OrganizeTask(ObservableList<String> listViewItems, long totalSizeToCopy) {
		this.processedFileCount = new AtomicInteger(0);
		this.totalSize = new AtomicLong(0);
		this.listViewItems = listViewItems;
		this.totalSizeToCopy = totalSizeToCopy;
	}

	public abstract void organize(Path file);

	@Override
	protected Void call() throws Exception {
		Thread.sleep(100);
		for (String sourceFolder : listViewItems) {
			Files.walkFileTree(Paths.get(sourceFolder), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (isCancelled()) {
						return FileVisitResult.TERMINATE;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isCancelled()) {
						return FileVisitResult.TERMINATE;
					}
					totalSize.addAndGet(attrs.size());
					organize(file);
					processedFileCount.addAndGet(1);
					updateMessage(file.toString());
					updateProgress(totalSize.get(), totalSizeToCopy);
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return null;
	}

}
