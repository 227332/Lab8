package it.polito.tdp.metrodeparis.model;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metrodeparis.db.MetroDAO;

public class Model {
	List<Fermata> fermate = null;
	WeightedGraph<StazioneLinea,DefaultWeightedEdge> grafo;
	List<Connessione> conn = null;
	List<Linea> linee =null;
	List<StazioneLinea> stazLinee = null;
	

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
		
		grafo = new SimpleDirectedWeightedGraph<StazioneLinea,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		stazLinee = new ArrayList<>();
		for(Connessione c: conn){
			StazioneLinea s1 = new StazioneLinea(c.getStazP(),c.getLinea());
			StazioneLinea s2 = new StazioneLinea(c.getStazA(),c.getLinea());
			
			/*
			 * Faccio il check che non lo contenga già perchè per ogni connessione
			 * (stazP,stazA) avrò anche la sua inversa! In alternativa potevo anche
			 * fare che nel for inserivo solo s1 e basta, perché tanto s2 verrà 
			 * inserita quando stazA sarà nella connessione inversa a questa.
			 * Cmq questo check mi serve solo per la lista stazLinee... Infatti addVertex()
			 * automaticamente lascia il grafo invariato nel caso in cui il vertice sia già
			 * presente!
			 */
			if(!stazLinee.contains(s1)){
				stazLinee.add(s1);
				grafo.addVertex(s1);
			}
			if(!stazLinee.contains(s2)){
				stazLinee.add(s2);
				grafo.addVertex(s2);
			}
			/*
			 * OSS: ormai ho messo tale check, ma in realtà non serve perché addEdge() lascia il
			 * grafo invariato se vi è già un edge tra i due vertici e il grafo è simple 
			 * (non multiplo quindi) o cmq se lo stesso edge c'è già (sia nel caso simple che
			 * multiplo)
			 */
			if(!grafo.containsEdge(s1, s2)){
				double dist = LatLngTool.distance(c.getStazP().getCoords(), c.getStazA().getCoords(), LengthUnit.KILOMETER);
				double peso = (dist/c.getLinea().getVelocita())*60*60;
				Graphs.addEdge(grafo, s1, s2, peso);
			}

		}
		
		//ora calcolo i pesi dei vari edges tra (stessa stazione,linee diverse) e poi li aggiungo al grafo
		for(StazioneLinea s1: stazLinee){
			for(StazioneLinea s2: stazLinee){
				/*
				 * Ho già detto prima che il check containsEdge() non serve ma ormai ce lo lascio
				 */
				if(s1.getStaz().equals(s2.getStaz()) && !s1.equals(s2) && !grafo.containsEdge(s1, s2)){
					//new edge
					double peso = s1.getLinea().getIntervallo()*60;//passo da minuti a secondi
					Graphs.addEdge(grafo, s1, s2, peso);
				}
			}
		}
		
		
		
	}

	public String getPercorsoMin(Fermata stazP,Fermata stazA) {
		double minTempo = Double.MAX_VALUE;
		List<DefaultWeightedEdge> minEdgesPath=null;
		
		for(StazioneLinea s1: stazLinee){
			for(StazioneLinea s2: stazLinee){
				if(s1.getStaz().equals(stazP) && s2.getStaz().equals(stazA)){
					DijkstraShortestPath<StazioneLinea,DefaultWeightedEdge> cammino = new DijkstraShortestPath<>(grafo,s1,s2);
					List<DefaultWeightedEdge> edgesPath = cammino.getPathEdgeList();
					double tempoTot = cammino.getPathLength();
					
					if(tempoTot<minTempo){
						minTempo=tempoTot;
						minEdgesPath=edgesPath;
					}					
				}
			}
		}
			
		if(minEdgesPath.size()>1)
			minTempo+=30*(minTempo-1);//tiene conto del tempo per spostarsi di linea già 
										//con il peso degli edges
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Percorso:\n\n");
		Linea lineaTemp = grafo.getEdgeTarget(minEdgesPath.get(0)).getLinea();
		sb.append("Prendo Linea: " + lineaTemp.getNome() + "\n");

		for (DefaultWeightedEdge edge : minEdgesPath) {
			//quando cambio la linea stampo lo stesso il nome della stazione, anche se è uguale al precedente
			sb.append(grafo.getEdgeTarget(edge).getStaz().getNome());
			sb.append("\n");
			if (!grafo.getEdgeTarget(edge).getLinea().equals(lineaTemp)) {
				sb.append("\n\nCambio su Linea: " + grafo.getEdgeTarget(edge).getLinea().getNome() + "\n");
				lineaTemp = grafo.getEdgeTarget(edge).getLinea();
			}
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
		int tempoTotSec = (int) minTempo;
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
