package it.polito.tdp.metrodeparis;


import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.metrodeparis.model.Fermata;
import it.polito.tdp.metrodeparis.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

public class MetroDeParisController {

	Model model;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TextArea txtRisultato;

	@FXML
	private ChoiceBox<Fermata> boxPartenza;

	@FXML
	private ChoiceBox<Fermata> boxArrivo;

	@FXML
	private Button btnCalcola;

	void setModel(Model model) {

		this.model = model;
		
		model.creaGrafo();
		List<Fermata> staz=model.getAllFermate();
		boxPartenza.getItems().addAll(staz);
		boxArrivo.getItems().addAll(staz);
		
	}

	@FXML
	void calcolaPercorso(ActionEvent event) {
		if(boxPartenza.getValue()== null || boxArrivo.getValue()==null){
			txtRisultato.setText("Errore: selezionare le stazioni\n");
		}

		txtRisultato.setText(model.getPercorsoMin(boxPartenza.getValue(),boxArrivo.getValue()));
			
	}

	@FXML
	void initialize() {

		assert txtRisultato != null : "fx:id=\"txtElencoStazioni\" was not injected: check your FXML file 'gui.fxml'.";
		assert boxPartenza != null : "fx:id=\"choiceBoxPartenza\" was not injected: check your FXML file 'gui.fxml'.";
		assert boxArrivo != null : "fx:id=\"choiceBoxArrivo\" was not injected: check your FXML file 'gui.fxml'.";
		assert btnCalcola != null : "fx:id=\"btnCalcola\" was not injected: check your FXML file 'gui.fxml'.";
	}
}