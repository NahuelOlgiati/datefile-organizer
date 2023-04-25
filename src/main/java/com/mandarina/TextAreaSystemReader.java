package com.mandarina;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class TextAreaSystemReader implements Runnable {

	private PipedInputStream standardPipe;
	private PipedInputStream standardErrorPipe;
	private Thread standarReader;
	private Thread standarErrorReader;
	private boolean quit;

	private TextArea textArea;

	TextAreaSystemReader(TextArea textArea) {
		this.textArea = textArea;
	}

	public synchronized void run() {
		appendText(this.standarReader, this.standardPipe);
		appendText(this.standarErrorReader, this.standardErrorPipe);
	}

	private void appendText(Thread reader, PipedInputStream pipe) {
		while (Thread.currentThread() == reader) {
			try {
				wait(100L);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			try {
				appendText(pipe);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (this.quit)
				return;
		}
	}

	private void appendText(PipedInputStream pipe) throws IOException {
		if (pipe.available() != 0) {
			String input = readLine(pipe);
			this.textArea.appendText(input);
		}
	}

	public synchronized String readLine(PipedInputStream pipe) throws IOException {
		String input = "";
		do {
			int available = pipe.available();
			if (available == 0)
				break;
			byte[] b = new byte[available];
			pipe.read(b);
			input = input + new String(b, 0, b.length);
		} while ((!input.endsWith("\n")) && (!input.endsWith("\r\n")) && (!this.quit));
		return input;
	}

	synchronized void startRead() throws IOException {
		this.standardPipe = new PipedInputStream();
		System.setOut(new PrintStream(new PipedOutputStream(this.standardPipe), true));
		this.standardErrorPipe = new PipedInputStream();
		System.setErr(new PrintStream(new PipedOutputStream(this.standardErrorPipe), true));

		this.quit = false;
		this.standarReader = new Thread(this);
		this.standarReader.setDaemon(true);
		this.standarReader.start();

		this.standarErrorReader = new Thread(this);
		this.standarErrorReader.setDaemon(true);
		this.standarErrorReader.start();
	}

	synchronized void stopRead() {
		this.quit = true;
		notifyAll();
		close(standarReader, standardPipe);
		close(standarErrorReader, standardErrorPipe);
		Platform.exit();
		System.exit(0);
	}

	private void close(Thread reader, PipedInputStream pipe) {
		try {
			reader.join(100L);
			pipe.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}