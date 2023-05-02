package com.mandarina;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FolderWeightCalculator extends Application {

    private Label resultLabel;

    @Override
    public void start(Stage primaryStage) {
        resultLabel = new Label("Click the button to calculate folder weight.");
        VBox root = new VBox();
        root.getChildren().add(resultLabel);
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();

        // Create a Task to calculate the folder weight in the background
        Task<Long> weightTask = new Task<Long>() {
            @Override
            protected Long call() throws Exception {
                String[] paths = {"C:/Temp", "C:/Temp1"};
                return getFolderWeight(paths);
            }
        };

        // When the weightTask finishes, update the resultLabel on the GUI thread
        weightTask.setOnSucceeded(event -> {
            long result = weightTask.getValue();
            resultLabel.setText("Total weight of folders: " + result + " bytes");
        });

        // Start the weightTask on a new thread
        new Thread(weightTask).start();
    }

    public static long getFolderWeight(String[] paths) {
        AtomicLong totalWeight = new AtomicLong(0);
        for (String path : paths) {
            Path folderPath = Paths.get(path);
            if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
                try {
                    getFolderWeightRecursive(folderPath, totalWeight);
                } catch (IOException e) {
                    System.err.println("Error calculating weight of folder: " + folderPath);
                    e.printStackTrace();
                }
            }
        }
        return totalWeight.get();
    }

    private static void getFolderWeightRecursive(Path folderPath, AtomicLong totalWeight) throws IOException {
        Files.walkFileTree(folderPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                totalWeight.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("Failed to visit file: " + file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
