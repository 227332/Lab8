package it.polito.tdp.metrodeparis;
	
import it.polito.tdp.metrodeparis.model.Model;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MetroDeParis.fxml"));
			BorderPane root = (BorderPane) loader.load();

			//creo la VIEW
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			//creo il MODEL
			Model model = new Model();
			
			//mi faccio restituire il CONTROLLER dal FXMLLoader
			MetroDeParisController controller = loader.getController();
			
			//connetto il CONTROLLER con il MODEL
			controller.setModel(model);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
