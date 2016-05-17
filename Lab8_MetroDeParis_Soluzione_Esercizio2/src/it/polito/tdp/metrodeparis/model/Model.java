package it.polito.tdp.metrodeparis.model;

import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metrodeparis.db.MetroDAO;

public class Model {

	//lo metto solo per comodità il debug. Così se lo metto a true mi faccio stampare su console varie
	//cose nel corso del programma in modo da controllare che funzioni. Se lo metto a false il programma
	//non mi stampa nulla su console ma opera solo tramite la GUI
	private static boolean debug = true;

	private List<Linea> linee;
	private List<Fermata> fermate;
	private List<Connessione> connessioni;
	private List<FermataSuLinea> fermateSuLinea;

	private List<DefaultWeightedEdge> pathEdgeList = null;
	private double pathTempoTotale = 0;

	private MetroDAO metroDAO;

	// Directed Weighted Graph
	private DefaultDirectedWeightedGraph<FermataSuLinea, DefaultWeightedEdge> grafo = null;

	public Model() {
		if (debug)
			System.out.println("Costruisco il grafo.");

		metroDAO = new MetroDAO();
	}

	public List<Fermata> getStazioni() {

		if (fermate == null)
			throw new RuntimeException("Lista delle fermate non disponibile!");

		return fermate;
	}

	public void creaGrafo() {

		fermate = metroDAO.getAllFermate();
		linee = metroDAO.getAllLinee();
		connessioni = metroDAO.getAllConnessioni(fermate, linee);
		fermateSuLinea = metroDAO.getAllFermateSuLinea(fermate, linee);

		// In questo esercizio serve un Directed Weighted Graph
		grafo = new DefaultDirectedWeightedGraph<FermataSuLinea, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		// FASE1: Aggiungo ogni fermataSuLinea come vertice del grafo
		Graphs.addAllVertices(grafo, fermateSuLinea);

		// FASE2: Aggiungo ogni connessione, tra 2 fermateSuLinee aventi stessa Linea ma diversa Fermata, 
		//come edge del grafo tra i nodi corrispondenti a tali fermateSuLinee
		//...Tali edges hanno come peso i tempi di collegamento tra le due diverse stazioni
		for (Connessione c : connessioni) {

			double velocita = c.getLinea().getVelocita();
			double distanza = LatLngTool.distance(c.getStazP().getCoords(), c.getStazA().getCoords(),LengthUnit.KILOMETER);
			double tempo = (distanza / velocita) * 60 * 60;

			
			// Nella List fermateSuLinea cerco la fermataSuLinea corrispondente a [c.getStazP(), c.getLinea()]
			// e la fermataSuLinea corrispondente a [c.getStazA(), c.getLinea()]
			FermataSuLinea fslPartenza = fermateSuLinea.get(fermateSuLinea.indexOf(new FermataSuLinea(c.getStazP(), c.getLinea())));
			FermataSuLinea fslArrivo = fermateSuLinea.get(fermateSuLinea.indexOf(new FermataSuLinea(c.getStazA(), c.getLinea())));

			if (fslPartenza != null && fslArrivo != null) {
				// Aggiungo un arco orientato e pesato tra le due fermateSuLinee, con il peso dato dal tempo
				//di trasporto
				Graphs.addEdge(grafo, fslPartenza, fslArrivo, tempo);
			} else {
				System.err.println("Non ho trovato fslPartenza o fslArrivo. Salto alle prossime");
			}
		}

		// FASE3: Aggiungo ogni connessione, tra 2 fermateSuLinee aventi stessa Fermata ma diversa Linea, 
		//come edge del grafo tra i nodi corrispondenti a tali fermateSuLinee
		//...Tali edges hanno come peso i tempi di attesa tra le due diverse Linee della stessa stazione,
		// ossia i tempi di attesa del passeggero per cambiare Linea in una stazione
		for (Fermata fermata : fermate) {
			
			//Ora, per aggiungere gli edges corrispondenti ad un cambio di linea all'interno della stessa
			//stazione, faccio così: per ogni nodo contenente tale fermata aggiungi un arco tra tale nodo
			//e tutti gli altri nodi aventi sempre tale fermata ma che sono diversi dal nodo dato (ossia 
			//che hanno la linea diversa)
			for (FermataSuLinea fslP : fermata.getFermateSuLinea()) {
				for (FermataSuLinea fslA : fermata.getFermateSuLinea()) {
					if (!fslP.equals(fslA)) {
						// Aggiungo un arco orientato e pesato tra le due fermateSuLinee, con il peso dato dal tempo
						//di attesa dovuto al cambio linea all'interno di tale stazione
						Graphs.addEdge(grafo, fslP, fslA, fslA.getLinea().getIntervallo() * 60);
						//la traccia diceva che il tempo di attesa è dato dall' attributo intervallo 
						//specificato nell' oggetto Linea. Moltiplico poi per 60 perchè esso è espresso in
						//minuti mentre io lo voglio in secondi
					}
				}
			}
		}

		if (debug)
			System.out.println("Grafo creato: " + grafo.vertexSet().size() + " nodi, " + grafo.edgeSet().size() + " archi");
	}

	public void calcolaPercorso(Fermata partenza, Fermata arrivo) {

		DijkstraShortestPath<FermataSuLinea, DefaultWeightedEdge> dijkstra;

		// Usati per salvare i valori temporanei
		double pathTempoTotaleTemp;

		// Usati per salvare i valori migliori
		List<DefaultWeightedEdge> bestPathEdgeList = null;
		double bestPathTempoTotale = Double.MAX_VALUE;
		
		//ATT: Infatti siccome in tale esercizio non hai un nodo per ogni fermata ma più nodi,
		// per trovare il percorso minimo tra Fermata A e Fermata B devi applicare Dijkstra tra ogni 
		// FermataSuLinea contenente Fermata A e ogni FermataSuLinea contenente Fermata B e infine prendere
		// come soluzione il risultato avente tempo minimo

		for (FermataSuLinea fslP : partenza.getFermateSuLinea()) {
			for (FermataSuLinea fslA : arrivo.getFermateSuLinea()) {
				dijkstra = new DijkstraShortestPath<FermataSuLinea, DefaultWeightedEdge>(grafo, fslP, fslA);

				pathTempoTotaleTemp = dijkstra.getPathLength();

				if (pathTempoTotaleTemp < bestPathTempoTotale) {
					bestPathTempoTotale = pathTempoTotaleTemp;
					bestPathEdgeList = dijkstra.getPathEdgeList();
				}
			}
		}

		pathEdgeList = bestPathEdgeList;
		pathTempoTotale = bestPathTempoTotale;

		if (pathEdgeList == null)
			throw new RuntimeException("Non è stato creato un percorso.");



		/* pathTempoTotale non ha tenuto conto del fatto che c'è una sosta di 
		 * 30 secondi per ogni fermata. Devo perciò aggiungere tale tempo, il
		 * quale naturalmente non c'è per le stazioni di partenza e di arrivo
		 * ma solo per quelle intermedie
		 */
		if (pathEdgeList.size() - 1 > 0) {
			pathTempoTotale += (pathEdgeList.size() - 1) * 30;
		}
	}

	public String getPercorsoEdgeList() {

		if (pathEdgeList == null)
			throw new RuntimeException("Non Ã¨ stato creato un percorso.");
		
		/*
		 * Utilizzo StringBuilder (o anche StringBuffer, tanto nel caso di un solo thread sono
		 * uguali) invece di String perchè mi serve una stringa di dim variabile e non fissa
		 */
		StringBuilder risultato = new StringBuilder();
		risultato.append("Percorso:\n\n");

		Linea lineaTemp = grafo.getEdgeTarget(pathEdgeList.get(0)).getLinea();
		risultato.append("Prendo Linea: " + lineaTemp.getNome() + "\n[");

		for (DefaultWeightedEdge edge : pathEdgeList) {
			
			risultato.append(grafo.getEdgeTarget(edge).getNome());

			if (!grafo.getEdgeTarget(edge).getLinea().equals(lineaTemp)) {
				risultato.append("]\n\nCambio su Linea: " + grafo.getEdgeTarget(edge).getLinea().getNome() + "\n[");
				lineaTemp = grafo.getEdgeTarget(edge).getLinea();
				
			} else {
				risultato.append(", ");
			}
		}
		//cancello l' ultima ", " messa
		risultato.setLength(risultato.length() - 2);
		risultato.append("]");

		return risultato.toString();
	}

	public double getPercorsoTempoTotale() {

		if (pathEdgeList == null)
			throw new RuntimeException("Non Ã¨ stato creato un percorso.");

		return pathTempoTotale;
	}
}
