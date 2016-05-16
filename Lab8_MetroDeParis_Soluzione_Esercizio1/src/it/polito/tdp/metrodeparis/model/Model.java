package it.polito.tdp.metrodeparis.model;

import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metrodeparis.db.MetroDAO;

public class Model {

	private List<Linea> linee;
	private List<Fermata> fermate;
	private List<Connessione> connessioni;

	//In questo esercizio serve un grafo non orientato (perchè ogni linea fa sia andata che 
	//ritorno perciò ogni arco ha sempre entrame le direzioni) PESATO in base al tempo di 
	//percorrenza tra le due stazioni. Ecco perchè per la classe degli archi non si usa più
	//DefaultEdge ma DefaultWeightedEdge
	private List<DefaultWeightedEdge> pathEdgeList = null;
	
	private double pathTempoTotale = 0;
	

	// Undirected Weighted Graph
	//ATT: i nodi del grafo sono solo le Fermate, NON anche le Linee o le Connessioni,
	// non fare cose stupide!
	private SimpleWeightedGraph<Fermata, DefaultWeightedEdge> grafo = null;


	
	public List<Fermata> getStazioni() {

		if (fermate == null)
			throw new RuntimeException("Lista delle stazioni non disponibile!");

		return fermate;
	}

	
	
	public void creaGrafo() {

		MetroDAO dao = new MetroDAO();
		fermate = dao.getAllFermate();
		linee = dao.getAllLinee();
		connessioni = dao.getAllConnessione(fermate, linee);

		// Undirected Weighted
		grafo = new SimpleWeightedGraph<Fermata, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		// Aggiungo un vertice per ogni fermata
		Graphs.addAllVertices(grafo, fermate);

		for (Connessione c : connessioni) {

			// IMPORTANTE:
			// Usando un grafo semplice non prendo in considerazione il caso
			// in cui due fermate siano collegate da più linee.

			//calcolo il peso dell'arco della connessione c, ossia il tempo tra le sue due
			//stazioni
			
			double velocita = c.getLinea().getVelocita();
			//per calcolare la distanza usiamo un metodo statico della classe che ha tutti i
			//metodi inerenti la latitudine e longitudine. Tale classe è LatLngTool, la quale
			//va importata nel nostro progetto (nella cartella lib)
			double distanza = LatLngTool.distance(c.getStazP().getCoords(), c.getStazA().getCoords(), LengthUnit.KILOMETER);
			double tempo = (distanza / velocita) * 60 * 60;// passo da ore a secondi

			// Aggiungo un un arco pesato tra le due fermate
			Graphs.addEdge(grafo, c.getStazP(), c.getStazA(), tempo);
		}

			System.out.println("Grafo creato: " + grafo.vertexSet().size() + " nodi, " + grafo.edgeSet().size() + " archi");
	}

	
	
	/*
	 * RICORDA: quando si deve calcolare un qualche percorso o cammino, pensa subito agli
	 * 	algoritmi di cammini minimi visti in classe!!!
	 */
	public void calcolaPercorso(Fermata partenza, Fermata arrivo) {

		DijkstraShortestPath<Fermata, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<Fermata, DefaultWeightedEdge>(grafo, partenza, arrivo);

		pathEdgeList = dijkstra.getPathEdgeList();
		pathTempoTotale = dijkstra.getPathLength();

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
			throw new RuntimeException("Non è stato creato un percorso.");

		/*
		 * Utilizzo StringBuilder (o anche StringBuffer, tanto nel caso di un solo thread sono
		 * uguali) invece di String perchè mi serve una stringa di dim variabile e non fissa
		 */
		StringBuilder risultato = new StringBuilder();
		risultato.append("Percorso: [ ");

		for (DefaultWeightedEdge edge : pathEdgeList) {
			//getEdgeTarget(edge) restituisce il vertice di arrivo di edge
			risultato.append(grafo.getEdgeTarget(edge).getNome());
			risultato.append(", ");
		}
		//cancella l' ultima ", " messa
		risultato.setLength(risultato.length()-2);
		risultato.append("]");

		return risultato.toString();
	}

	
	
	
	public double getPercorsoTempoTotale() {

		if (pathEdgeList == null)
			throw new RuntimeException("Non è stato creato un percorso.");

		return pathTempoTotale;
	}
}
