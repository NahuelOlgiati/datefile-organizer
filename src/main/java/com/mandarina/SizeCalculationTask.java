package com.mandarina;

import java.util.concurrent.atomic.AtomicLong;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class SizeCalculationTask extends Task<Long> {

	protected AtomicLong totalSize;
	protected ObservableList<String> listViewItems;

	public SizeCalculationTask(ObservableList<String> listViewItems) {
		this.totalSize = new AtomicLong(0);
		this.listViewItems = listViewItems;
	}
	
	@Override
	protected Long call() throws Exception {
		Thread.sleep(100);
		return FileUtil.getFolderSize(listViewItems, totalSize);
	}
}
