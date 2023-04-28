package com.mandarina;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class App extends Application {

	private Label statusLabel;
	private Button orgButton;
	private Button stopButton;

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		primaryStage.setTitle("Dforg");
		primaryStage.getIcons().add(new Image(cl.getResourceAsStream("assets/nina.png")));
		primaryStage.setResizable(false);

		var sourceHBox = new HBox(5);
		var sourceButton = new Button("Source Folder");
		var sourceTextField = new TextField();
		sourceTextField.setDisable(true);
		sourceTextField.setPrefWidth(518);
		sourceButton.setOnAction(e -> {
			File selectedDirectory = new DirectoryChooser().showDialog(primaryStage);
			if (selectedDirectory != null) {
				sourceTextField.setText(selectedDirectory.toPath().toString());
			}
		});
		sourceHBox.getChildren().addAll(sourceButton, sourceTextField);

		var targetHBox = new HBox(5);
		var targetButton = new Button("Target Folder ");
		var targetTextField = new TextField();
		targetTextField.setDisable(true);
		targetTextField.setPrefWidth(520);
		targetButton.setOnAction(e -> {
			File selectedDirectory = new DirectoryChooser().showDialog(primaryStage);
			if (selectedDirectory != null) {
				targetTextField.setText(selectedDirectory.toPath().toString());
			}
		});
		targetHBox.getChildren().addAll(targetButton, targetTextField);

		var copyCheckBox = new CheckBox("Copy");
		copyCheckBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

		var processHBox = new HBox(5);
		orgButton = new Button("Process");
		stopButton = new Button("Stop");
		stopButton.setDisable(true);
		statusLabel = new Label();
		statusLabel.setPadding(new Insets(5));
		processHBox.getChildren().addAll(orgButton, stopButton, statusLabel);
		orgButton.setOnAction(e -> organize(cl, sourceTextField, targetTextField, copyCheckBox));

		var mainVBox = new VBox(10);
		mainVBox.getChildren().addAll(sourceHBox, targetHBox, copyCheckBox, new VBox(10), processHBox);

		var mainGridPane = new GridPane();
		mainGridPane.setPadding(new Insets(15));
		var bg = new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#ECA074")), new Stop(1, Color.web("#F39E9C"))), null, null));
		mainGridPane.setBackground(bg);
		mainGridPane.getChildren().add(mainVBox);

		Scene scene = new Scene(mainGridPane, 650, 170);
		scene.getStylesheets().add(cl.getResource("assets/styles.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void organize(ClassLoader cl, TextField sourceTextField, TextField targetTextField, CheckBox copyCheckBox) {
		boolean copy = copyCheckBox.isSelected();
		var sourceFolderPath = sourceTextField.getText();
		var targetFolderPath = targetTextField.getText();
		var sourcePath = Paths.get(sourceFolderPath);
		var targetPath = Paths.get(targetFolderPath);
		String msg = Organizer.valid(sourceFolderPath, targetFolderPath, sourcePath, targetPath);
		if (msg == null) {
			Organizer organizer = new Organizer(copy, sourcePath, targetPath, orgButton, stopButton, statusLabel);
			try {
				organizer.copyRoutine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			alert(cl, msg);
		}
	}

	private static void alert(ClassLoader cl, String message) {
		var alert = new Alert(AlertType.WARNING);
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
				.add(new Image(cl.getResourceAsStream("assets/nina.png")));
		alert.setHeaderText("");
		alert.setContentText(message);
		alert.show();
	}
}
