package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import model.ChatMessage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class FileBubbleRightController {

    @FXML private ImageView downloadIcon;
    @FXML private Label fileName;
    @FXML private Label fileSize;

    private String filePath;

    public void setData(ChatMessage message) {
        this.filePath = message.getFilePath();
        File file = new File(filePath);

        fileName.setText(file.getName());
        fileSize.setText(readableFileSize(file.length()));

        downloadIcon.setOnMouseClicked(this::handleDownloadClick);
    }

    private void handleDownloadClick(MouseEvent event) {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            showAlert("File không tồn tại!", "Không thể tải vì file không tồn tại trên máy bạn.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Lưu file về...");
        fileChooser.setInitialFileName(sourceFile.getName());

        File destination = fileChooser.showSaveDialog(null);
        if (destination != null) {
            try {
                java.nio.file.Files.copy(
                    sourceFile.toPath(),
                    destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                showAlert("Thành công", "Đã tải file về: " + destination.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể tải file: " + e.getMessage());
            }
        }
    }
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    private String readableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
