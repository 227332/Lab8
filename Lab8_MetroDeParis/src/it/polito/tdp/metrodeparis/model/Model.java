package it.polito.tdp.metrodeparis.model;

import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metrodeparis.db.MetroDAO;

public class Model {
	List<Fermata> fermate = null;
	WeightedGraph<Fermata,DefaultWeightedEdge> grafo;
	List<Connessione> conn = null;
	List<Linea> linee =null;
	

	public List<Fermata> getAllFermate() {
		if(fermate==null){
			MetroDAO dao= new MetroDAO();
			fermate = dao.getAllFermate();
		}
		
		return fermate;
	}
	
	public List<Linea> getAllLinee() {
		if(linee==null){
			MetroDAO dao= new MetroDAO();
			linee = dao.getAllLinee();
		}
		
		return linee;
	}
	
	/*
	 * OSS: devo passare anche fermate e linee perché così quando 
	 * creo un oggetto conn gli passo i rifarimenti di Fermata e Linea
	 * che ho già creato in precedenza, perché non voglio creare nuovi
	 * oggetti Fermata e Linea!
	 */
	public List<Connessione> getAllConn(List<Fermata> fermate,List<Linea> linee) {
		if(conn==null){
			MetroDAO dao= new MetroDAO();
			conn = dao.getAllConn(fermate,linee);
		}
		
		return conn;
	}

	public void creaGrafo() {
		fermate=getAllFermate();
		linee=getAllLinee();
		conn=getAllConn(fermate,linee);
		grafo= new SimpleWeightedGraph<Fermata,DefaultWeightedEdge>(DefaultWeightedEdge.class);
	
		/*
		 * ATT: è vero che grafo non ha un metodo per aggiungere tutti i 
		 * vertici direttamente, ma NON devi fare:
		 * 	for(Fermata f: fermate)
				grafo.addVertex(f);
			perché c'è la classe Graphs che ha vari metodi utili 
			tra cui addAllVertices()!!!
		 */
		Graphs.addAllVertices(grafo,fermate);
		
		for(Connessione c: conn){
			Fermata stazP = c.getStazP();
			Fermata stazA = c.getStazA();
			Linea l = c.getLinea();
			
			double dist = LatLngTool.distance(stazP.getCoords(), stazA.getCoords(), LengthUnit.KILOMETER);
			
			double peso = (dist/l.getVelocita())*60*60;//siccome poi ho soste in secondi, passo da Km/h a Km/s
			
			/*
			 * ATT:
			 * sebbene le classi di tipo grafo hanno il metodo addEdge(), esso non va usato
			 * perché richiede di passare l'edge già come DefaultWeightedEdge, ma ciò non è
			 * possibile visto che tale classe non consente di settare il peso dell'edge.
			 * Ecco perché nella doc di tale classe c'è scritto:
			 * 
			 * "All access to the weight of an edge must go through the graph interface, 
			 * which is why this class doesn't expose any public methods."
			 * 
			 * Perciò devi usare sempre la classe Graphs per aggiungere gli edges,
			 * NON dimenticarlo!!!
			 */
			Graphs.addEdge(grafo, stazP, stazA, peso);
		}
		
		
		
	}

	public String getPercorsoMin(Fermata stazP,Fermata stazA) {
		
		DijkstraShortestPath<Fermata,DefaultWeightedEdge> cammino = new DijkstraShortestPath<>(grafo,stazP,stazA);
		
		List<DefaultWeightedEdge> edgesPath = cammino.getPathEdgeList();
		double tempoTot = cammino.getPathLength();
		
		if(edgesPath.size()>1)
			tempoTot+=30*(tempoTot-1);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Percorso:\n");

		for (DefaultWeightedEdge edge : edgesPath) {
			sb.append(grafo.getEdgeTarget(edge).getNome());
			sb.append("\n");
		}
		
		sb.append("\n");
		sb.append("Tempo di percorrenza stimato: ");
		
		/*
		 * OSS: Ricorda come passare da secondi a hh:mm:ss!
		 * - le ore sono la divisione intera con 3600
		 * - i minuti sono la divisione intera tra quello che rimane
		 * 	 dopo aver diviso per le ore e 60
		 * - il rimanente sono i secondi
		 */
		int tempoTotSec = (int) tempoTot;
		int ore = tempoTotSec / 3600;
		int minuti = (tempoTotSec % 3600) / 60;
		int secondi = tempoTotSec % 60;
		sb.append(String.format("%02d:%02d:%02d", ore, minuti, secondi));
	   /*RIC: %02d vuol dire che utilizzo sempre 2 spazi e, se un numero non è
		a due cifre, allora lo spazio vuoto lo riempio con 0. Invece %2d lascia
		2 spazi ma lo spazio vuoto riane vuoto. Infine %d lascia solo lo spazio
		necessario per scrivere il numero. Ad esempio:
		- caso %02d:  02:03:19
		- caso %2d:    2: 3:19
		- caso %d:    2:3:19
	    */
		return sb.toString();

	}



}
