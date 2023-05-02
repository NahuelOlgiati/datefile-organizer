package com.mandarina;

import java.util.concurrent.atomic.AtomicLong;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class SizeCalculatorTask extends Task<Long> {

	protected AtomicLong totalWeight;
	protected ObservableList<String> listViewItems;

	public SizeCalculatorTask(ObservableList<String> listViewItems) {
		this.totalWeight = new AtomicLong(0);
		this.listViewItems = listViewItems;
	}
	
	@Override
	protected Long call() throws Exception {
		return FileUtil.getFolderWeight(listViewItems, totalWeight);
	}
}
