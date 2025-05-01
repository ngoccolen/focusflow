package Util;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Pair;

public class FXMLUtils {
	public static <T> Pair<Parent, T> loadFXML(String fxmlPath) throws IOException {
		FXMLLoader loader = new FXMLLoader(FXMLUtils.class.getResource(fxmlPath));
		Parent root = loader.load();
		T controller = loader.getController();
        return new Pair<>(root, controller);
	}
}
