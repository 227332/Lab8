package it.polito.tdp.metrodeparis;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.metrodeparis.model.Model;
import it.polito.tdp.metrodeparis.model.Fermata;
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
	private ChoiceBox<Fermata> choiceBoxPartenza;

	@FXML
	private ChoiceBox<Fermata> choiceBoxArrivo;

	@FXML
	private Button btnCalcola;

	void setModel(Model model) {

		this.model = model;
		
		try {
			model.creaGrafo();
			
			//ora devo popolare le due ChoiceBox con la List di tutte le possibili Fermate
			List<Fermata> stazioni = model.getStazioni();
			choiceBoxPartenza.getItems().addAll(stazioni);
			choiceBoxArrivo.getItems().addAll(stazioni);
			
		} catch (RuntimeException e) {
			txtRisultato.setText(e.getMessage());//così in caso un problema, esso viene
												 //visualizzato non su console ma sulla GUI
		}
	}

	@FXML
	void calcolaPercorso(ActionEvent event) {

		/* OSS:
		 * Si poteva fare anche, equivalentemente:
		 * Fermata stazioneDiPartenza = choiceBoxPartenza.getSelectionModel().getSelectedItem();
		 * 
		 */
		Fermata stazioneDiPartenza = choiceBoxPartenza.getValue();
		Fermata stazioneDiArrivo = choiceBoxArrivo.getValue();

		if (stazioneDiPartenza != null && stazioneDiArrivo != null) {

			if (!stazioneDiPartenza.equals(stazioneDiArrivo)) {

				try {
					// Calcolo il percorso tra le due stazioni
					model.calcolaPercorso(stazioneDiPartenza, stazioneDiArrivo);

					// Ottengo il tempo di percorrenza, espresso tutto in secondi
					int tempoTotaleInSecondi = (int) model.getPercorsoTempoTotale();
					int ore = tempoTotaleInSecondi / 3600;
					int minuti = (tempoTotaleInSecondi % 3600) / 60;
					int secondi = tempoTotaleInSecondi % 60;
					String timeString = String.format("%02d:%02d:%02d", ore, minuti, secondi);
					//RIC: %02d vuol dire che visualizzo solo 2 cifre, non voglio zeri inutili tipo 032

					/*
					 * OSS: StringBuilder è identico a StringBuffer nei casi di singolo thread
					 *  ma più veloce. Ossia è, essenzialmente, una String di lunghezza mutabile.
					 *  Volendo potevo usare anche StringBuffer, essendo identici in questo caso.
					 *  Non uso String perchè sommo vari pezzi in momenti diversi e la String non
					 *  supporta ciò. Se volevo usare String dovevo fare in un'unica istruzione:
					 *  String risultato = model.getPercorsoEdgeList()+
					 *  					"\n\nTempo di percorrenza stimato: " + 
					 *  						timeString + "\n";
					 */
					StringBuilder risultato = new StringBuilder();
					// Ottengo il percorso
					//model.getPercorsoEdgeList() restituisce già la lista in formato String
					risultato.append(model.getPercorsoEdgeList());
					risultato.append("\n\nTempo di percorrenza stimato: " + timeString + "\n");

					// Aggiorno la TextArea
					txtRisultato.setText(risultato.toString());//RICORDA il toString!!!
					
				} catch (RuntimeException e) {
					txtRisultato.setText(e.getMessage());
				}

			} else {

				txtRisultato.setText("Inserire una stazione di arrivo diversa da quella di partenza.");
			}
			
		} else {
			
			txtRisultato.setText("Inserire una stazione di arrivo ed una di partenza.");
		}
	}

	@FXML
	void initialize() {

		assert txtRisultato != null : "fx:id=\"txtElencoStazioni\" was not injected: check your FXML file 'gui.fxml'.";
		assert choiceBoxPartenza != null : "fx:id=\"choiceBoxPartenza\" was not injected: check your FXML file 'gui.fxml'.";
		assert choiceBoxArrivo != null : "fx:id=\"choiceBoxArrivo\" was not injected: check your FXML file 'gui.fxml'.";
		assert btnCalcola != null : "fx:id=\"btnCalcola\" was not injected: check your FXML file 'gui.fxml'.";
	}
}