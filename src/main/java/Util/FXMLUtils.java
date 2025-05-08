package Util;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Pair;

public class FXMLUtils {
	//<T>: generic để trả về bất kì controller
	//Pair: trả về 1 cặp (Parent: giao diện, T: controller đi kèm)
	public static <T> Pair<Parent, T> loadFXML(String fxmlPath) throws IOException {  
		//tìm file fxml
		FXMLLoader loader = new FXMLLoader(FXMLUtils.class.getResource(fxmlPath));
		//load file fxml và lưu root node của giao diện
		Parent root = loader.load();
		//Lấy controller được gắn trong file fxml
		T controller = loader.getController();
		//Trả về cặp giao diện với controller tương ứng
        return new Pair<>(root, controller);
	}
}
