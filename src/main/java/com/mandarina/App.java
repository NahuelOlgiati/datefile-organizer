package com.mandarina;

import java.io.File;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
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

	private ListView<String> listView;
	private Button listViewAddButton;
	private Button listViewDeleteButton;
	private Button targetButton;
	private CheckBox copyCheckBox;
	private TextField statusField;
	private Button orgButton;
	private Button stopButton;
	private ProgressBar progressBar;

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		primaryStage.setTitle("Dforg");
		primaryStage.getIcons().add(new Image(cl.getResourceAsStream("assets/nina.png")));
		primaryStage.setResizable(false);

		listViewAddButton = new Button("Source Folders");
		listViewAddButton.setOnAction(this::addSourceFolder);
		listViewDeleteButton = new Button("Delete");
		listViewDeleteButton.setDisable(true);
		listViewDeleteButton.setOnAction(this::deleteSourceFolder);
		var listViewHBox = new HBox(5, listViewAddButton, listViewDeleteButton);

		listView = new ListView<String>();
		listView.setMaxHeight(72);
		listView.setItems(FXCollections.observableArrayList());
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			listViewDeleteButton.setDisable(newValue == null);
			listView.refresh();
		});
		listView.setCellFactory(param -> listViewCellUpdate());

		var targetHBox = new HBox(5);
		targetButton = new Button("Target Folder");
		targetButton.setMinWidth(95);
		var targetTextField = new TextField();
		targetTextField.setDisable(true);
		targetTextField.setPrefWidth(590);
		targetButton.setOnAction(e -> {
			File selectedDirectory = new DirectoryChooser().showDialog(primaryStage);
			if (selectedDirectory != null) {
				targetTextField.setText(selectedDirectory.toPath().toString());
			}
		});
		targetHBox.getChildren().addAll(targetButton, targetTextField);

		copyCheckBox = new CheckBox("Copy");
		copyCheckBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		var processHBox = new HBox(5);
		orgButton = new Button("Process");
		orgButton.setMinWidth(60);
		orgButton.setOnAction(e -> organize(cl, listView, targetTextField, copyCheckBox));
		stopButton = new Button("Stop");
		stopButton.setMinWidth(50);
		stopButton.setDisable(true);
		statusField = new TextField();
		statusField.setDisable(true);
		statusField.setPrefWidth(570);
		statusField.setVisible(false);
		processHBox.getChildren().addAll(orgButton, stopButton, statusField);

		progressBar = new ProgressBar();
		progressBar.setPrefWidth(700);
		progressBar.setProgress(0);
		progressBar.setStyle("-fx-accent: #AFA6D2;");
		progressBar.setVisible(false);

		var mainVBox = new VBox(10);
		mainVBox.getChildren().addAll(listViewHBox, listView, targetHBox, copyCheckBox, new VBox(10), processHBox,
				progressBar);

		var mainGridPane = new GridPane();
		mainGridPane.setPadding(new Insets(15));
		mainGridPane.setBackground(new Background(new BackgroundFill(//
				new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#ECA074")),
						new Stop(1, Color.web("#F39E9C"))),
				null, null)));
		mainGridPane.getChildren().add(mainVBox);

		Scene scene = new Scene(mainGridPane, 720, 270);
		scene.getStylesheets().add(cl.getResource("assets/styles.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void organize(ClassLoader cl, ListView<String> listView, TextField targetField, CheckBox copyCheckBox) {
		String msg = Organizer.valid(listView.getItems(), targetField.getText());
		if (msg == null) {
			try {
				getOrganizer(listView, targetField, copyCheckBox).organize();
			} catch (Exception e) {
				e.printStackTrace();
				alert(cl, "Fatal error");
			}
		} else {
			alert(cl, msg);
		}
	}

	private Organizer getOrganizer(ListView<String> listView, TextField targetField, CheckBox copyCheckBox) {
		return new Organizer(copyCheckBox.isSelected(), listView.getItems(), Paths.get(targetField.getText())) {

			@Override
			public void doBeforeSizeCalculator() {
				disableComponents();
			}

			@Override
			public void doAfterSizeCalculator() {
				enableComponents();
			}

			@Override
			public void doBeforeOrganize() {
				stopButton.setOnAction(e -> organizeTask.cancel());
				statusField.textProperty().bind(organizeTask.messageProperty());
				statusField.setVisible(true);
				progressBar.setVisible(true);
				progressBar.progressProperty().bind(organizeTask.progressProperty());
				disableComponents();
			}

			@Override
			public void doAfterOrganize() {
				statusField.textProperty().unbind();
				progressBar.progressProperty().unbind();
				progressBar.setProgress(0);
				progressBar.setVisible(false);
				statusField.setText(this.copy ? "Copied Files: " + organizeTask.copiedFilesCount
						: "Processed Files: " + organizeTask.copiedFilesCount);
				enableComponents();
			}
		};
	}

	private void disableComponents() {
		Platform.runLater(() -> {
			listView.setDisable(true);
			listViewAddButton.setDisable(true);
			targetButton.setDisable(true);
			copyCheckBox.setDisable(true);
			orgButton.setDisable(true);
			stopButton.setDisable(false);
		});
	}

	private void enableComponents() {
		Platform.runLater(() -> {
			listView.setDisable(false);
			listView.getSelectionModel().clearSelection();
			listViewAddButton.setDisable(false);
			targetButton.setDisable(false);
			copyCheckBox.setDisable(false);
			orgButton.setDisable(false);
			stopButton.setDisable(true);
			orgButton.setDisable(false);
		});
	}

	private static ListCell<String> listViewCellUpdate() {
		return new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setBackground(null);
				} else {
					setText(item);
					int index = getIndex();
					setBackground(index % 2 == 0 ? new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))
							: new Background(new BackgroundFill(Color.WHITE, null, null)));
				}
				setTextFill(Color.BLACK);
				if (isSelected()) {
					setBackground(new Background(new BackgroundFill(Color.web("#AFA6D2"), null, null)));
				}
			}
		};
	}

	private void addSourceFolder(ActionEvent event) {
		File selectedDirectory = new DirectoryChooser().showDialog(null);
		if (selectedDirectory != null) {
			listView.getItems().add(selectedDirectory.getAbsolutePath());
		}
	}

	private void deleteSourceFolder(ActionEvent event) {
		String selectedPath = listView.getSelectionModel().getSelectedItem();
		listView.getItems().remove(selectedPath);
		listView.getSelectionModel().clearSelection();
		listViewDeleteButton.setDisable(true);
		listView.refresh();
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
