package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ListSongController {
	@FXML private Label indexLabel;
	@FXML private Label songNameLabel;
	@FXML private Label durationLabel;
	public void setData(int index, String name, String duration) {
		indexLabel.setText(String.valueOf(index));
		songNameLabel.setText(name);
		durationLabel.setText(duration);
	}
}
