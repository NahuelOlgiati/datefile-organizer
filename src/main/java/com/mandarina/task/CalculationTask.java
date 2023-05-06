package com.mandarina.task;

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

public class CalculationTask extends Task<Void> {

	protected AtomicInteger count;
	protected AtomicLong totalSize;
	protected ObservableList<String> listViewItems;

	public CalculationTask(ObservableList<String> listViewItems) {
		this.count = new AtomicInteger(0);
		this.totalSize = new AtomicLong(0);
		this.listViewItems = listViewItems;
	}

	@Override
	protected Void call() throws Exception {
		Thread.sleep(100);
		for (String folderPath : listViewItems) {
			Files.walkFileTree(Paths.get(folderPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					count.addAndGet(1);
					totalSize.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return null;
	}

	public int getCount() {
		return count.get();
	}

	public long getTotalSize() {
		return totalSize.get();
	}
}
