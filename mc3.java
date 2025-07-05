package com.ppi.utility.importer;

import com.ppi.utility.importer.service.ExcelService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.DecimalFormat;

/**
 * JavaFX Controller for the main-view.fxml.
 * Handles UI interactions, file selection, and delegates Excel processing to ExcelService.
 */
@Component // Mark as a Spring component
@Scope("prototype") // Important: JavaFX controllers are instantiated by FXML loader, not Spring.
                    // Using prototype scope ensures a new instance is created and dependencies are injected.
public class MainController {

    @FXML
    private Button uploadButton; // This button will change text to "Replace File"

    @FXML
    private Button submitButton; // New button for submitting the file

    @FXML
    private Label messageLabel;

    @FXML
    private ProgressIndicator progressIndicator; // New progress indicator

    private final ExcelService excelService;
    private File selectedExcelFile; // To store the selected file

    // Use constructor injection for Spring-managed services
    @Autowired
    public MainController(ExcelService excelService) {
        this.excelService = excelService;
    }

    /**
     * Initializes the controller. This method is automatically called after the FXML fields are injected.
     */
    @FXML
    public void initialize() {
        resetUI(); // Set initial UI state
    }

    /**
     * Resets the UI to its initial state (only "Upload File" button visible).
     */
    private void resetUI() {
        uploadButton.setText("Upload File");
        uploadButton.setVisible(true);
        uploadButton.setManaged(true); // Ensure it takes up space

        submitButton.setVisible(false);
        submitButton.setManaged(false); // Ensure it doesn't take up space

        messageLabel.setText("Click 'Upload File' to select an Excel document.");
        messageLabel.getStyleClass().remove("success-message"); // Remove any previous styling
        messageLabel.getStyleClass().remove("error-message");
        messageLabel.getStyleClass().remove("processing-message");

        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false); // Ensure it doesn't take up space
    }

    /**
     * Handles the action when the "Upload File" / "Replace File" button is clicked.
     */
    @FXML
    private void onUploadButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) uploadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedExcelFile = file;
            updateFileSelectedUI(selectedExcelFile);
        } else {
            // If user cancels file selection, and a file was previously selected, keep the old state.
            // If no file was ever selected, go to initial state.
            if (selectedExcelFile == null) {
                resetUI();
            } else {
                // Do nothing, keep the previously selected file info
            }
        }
    }

    /**
     * Updates the UI after a file has been selected.
     *
     * @param file The selected Excel file.
     */
    private void updateFileSelectedUI(File file) {
        uploadButton.setText("Replace File");
        uploadButton.setVisible(true);
        uploadButton.setManaged(true);

        submitButton.setVisible(true);
        submitButton.setManaged(true);

        DecimalFormat df = new DecimalFormat("#.##");
        String fileSize = df.format((double) file.length() / (1024 * 1024)); // Size in MB
        messageLabel.setText("Selected file: " + file.getName() + " (" + fileSize + " MB)");
        messageLabel.getStyleClass().remove("success-message");
        messageLabel.getStyleClass().remove("error-message");
        messageLabel.getStyleClass().remove("processing-message");

        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
    }

    /**
     * Handles the action when the "Submit File" button is clicked.
     */
    @FXML
    private void onSubmitButtonClick() {
        if (selectedExcelFile == null) {
            messageLabel.setText("Please select an Excel file first.");
            messageLabel.getStyleClass().add("error-message");
            return;
        }

        // Update UI for processing state
        uploadButton.setVisible(false);
        uploadButton.setManaged(false);
        submitButton.setVisible(false);
        submitButton.setManaged(false);

        messageLabel.setText("Processing " + selectedExcelFile.getName() + "...");
        messageLabel.getStyleClass().add("processing-message"); // Add green styling
        messageLabel.getStyleClass().remove("success-message");
        messageLabel.getStyleClass().remove("error-message");

        progressIndicator.setVisible(true);
        progressIndicator.setManaged(true);

        // Create a Task to perform the long-running operation in a background thread
        Task<String> processTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // This code runs in a background thread
                return excelService.processExcelFile(selectedExcelFile);
            }

            @Override
            protected void succeeded() {
                // This code runs on the JavaFX Application Thread
                String resultMessage = getValue();
                messageLabel.setText(resultMessage);
                if (resultMessage.startsWith("Upload successful")) {
                    messageLabel.getStyleClass().add("success-message");
                    messageLabel.getStyleClass().remove("error-message");
                } else {
                    messageLabel.getStyleClass().add("error-message");
                    messageLabel.getStyleClass().remove("success-message");
                }
                messageLabel.getStyleClass().remove("processing-message");

                progressIndicator.setVisible(false);
                progressIndicator.setManaged(false);

                // After processing, return to the initial "Upload File" state
                selectedExcelFile = null; // Clear selected file
                resetUI();
            }

            @Override
            protected void failed() {
                // This code runs on the JavaFX Application Thread
                Throwable exception = getException();
                String errorMessage = "Error: " + (exception != null ? exception.getMessage() : "Unknown error.");
                messageLabel.setText(errorMessage);
                messageLabel.getStyleClass().add("error-message");
                messageLabel.getStyleClass().remove("success-message");
                messageLabel.getStyleClass().remove("processing-message");
                System.err.println("Error processing Excel file: " + errorMessage);
                if (exception != null) {
                    exception.printStackTrace();
                }

                progressIndicator.setVisible(false);
                progressIndicator.setManaged(false);

                // After processing (even if failed), return to the initial "Upload File" state
                selectedExcelFile = null; // Clear selected file
                resetUI();
            }
        };

        // Start the task in a new thread
        new Thread(processTask).start();
    }
}
