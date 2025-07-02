// MainController.java
package com.ppi.utility.importer;

import com.ppi.utility.importer.service.ExcelProcessingService; // Import the new service
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired; // Import Autowired
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.CompletableFuture; // For asynchronous processing

/**
 * Controller for the main application view (main-view.fxml).
 * This class handles UI interactions, such as file uploading.
 * It's annotated with @Component to be managed by Spring.
 */
@Component
public class MainController {

    // FXML elements injected by FXMLLoader
    @FXML
    private Label messageLabel; // Label to display messages to the user

    // The primary stage, will be set by the main application class after FXML loading
    private Stage primaryStage;

    // Inject the ExcelProcessingService using Spring's @Autowired
    private final ExcelProcessingService excelProcessingService;

    @Autowired
    public MainController(ExcelProcessingService excelProcessingService) {
        this.excelProcessingService = excelProcessingService;
    }

    /**
     * Initializes the controller. This method is automatically called by FXMLLoader
     * after the FXML file has been loaded and all @FXML annotated fields are injected.
     * It's a good place for initial setup of UI elements.
     */
    @FXML
    public void initialize() {
        // Initial message to the user, hidden until an action is performed.
        messageLabel.setText("");
        messageLabel.setVisible(false);
    }

    /**
     * Setter for the primary stage. This method will be called by the PpiExcelImporterApplication
     * after the MainController has been instantiated and the FXML loaded.
     * @param primaryStage The primary stage of the JavaFX application.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Handles the "Upload File" button click event.
     * This method is automatically called when the button (fx:id="uploadButton") is clicked.
     */
    @FXML
    protected void onUploadButtonClick() {
        // Ensure primaryStage is set before using it
        if (primaryStage == null) {
            System.err.println("Error: Primary Stage is not set in MainController.");
            Platform.runLater(() -> {
                messageLabel.setText("Application error: Stage not ready.");
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setVisible(true);
            });
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            Platform.runLater(() -> {
                messageLabel.setText("Processing file '" + selectedFile.getName() + "'...");
                messageLabel.setStyle("-fx-text-fill: blue;");
                messageLabel.setVisible(true);
            });

            // Process the file in a background thread to keep the UI responsive
            CompletableFuture.runAsync(() -> {
                try {
                    excelProcessingService.processAndSaveExcelData(selectedFile);
                    Platform.runLater(() -> {
                        messageLabel.setText("File '" + selectedFile.getName() + "' processed and data inserted successfully!");
                        messageLabel.setStyle("-fx-text-fill: green;");
                        messageLabel.setVisible(true);
                    });
                } catch (Exception e) {
                    System.err.println("Error processing Excel file: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        messageLabel.setText("Error processing file: " + e.getMessage());
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setVisible(true);
                    });
                }
            });
        } else {
            Platform.runLater(() -> {
                messageLabel.setText("File upload cancelled or no file selected.");
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setVisible(true);
            });
        }
    }
}
