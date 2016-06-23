package it.polito.tdp.metrodeparis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.metrodeparis.model.Connessione;
import it.polito.tdp.metrodeparis.model.Fermata;
import it.polito.tdp.metrodeparis.model.Linea;

public class MetroDAO {

	public List<Fermata> getAllFermate() {

		final String sql = "SELECT id_fermata, nome, coordx, coordy FROM fermata ORDER BY nome ASC";
		List<Fermata> fermate = new ArrayList<Fermata>();

		try {
			/*
			 * RICORDA: devi importarle da java.sql e non java.scene.jdbc
			 */
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Fermata f = new Fermata(rs.getInt("id_Fermata"), rs.getString("nome"),
						new LatLng(rs.getDouble("coordx"), rs.getDouble("coordy")));
				fermate.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}
		
		return fermate;
	}

	public List<Linea> getAllLinee() {
		/*
		 * OSS: siccome non costa nulla, ricorda di ordinare già qui i valori!!!
		 */
		final String sql = "SELECT id_linea, nome, velocita, intervallo FROM linea ORDER BY nome ASC";
		List<Linea> linee = new ArrayList<Linea>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Linea f = new Linea(rs.getInt("id_linea"), rs.getString("nome"), rs.getDouble("velocita"),
						rs.getDouble("intervallo"));
				linee.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return linee;
	}

	public List<Connessione> getAllConn(List<Fermata> fermate, List<Linea> linee) {
		final String sql = "select id_connessione, id_linea, id_stazP, id_stazA from connessione";
		List<Connessione> connessioni = new ArrayList<Connessione>();

		try {
			
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {

				int idLinea = rs.getInt("id_linea");
				int idStazP = rs.getInt("id_stazP");
				int idStazA = rs.getInt("id_stazA");

				/*
				 * ATT: ricorda che, se hai una lista e vuoi  prendere un dato suo elemento,
				 * puoi usare indexOf() il quale ti dice in che posizione sta l'elemento della
				 * lista che è uguale all'oggetto che tu gli passi!
				 */
				Linea myLinea = linee.get(linee.indexOf(new Linea(idLinea)));
				Fermata myStazP = fermate.get(fermate.indexOf(new Fermata(idStazP)));
				Fermata myStazA = fermate.get(fermate.indexOf(new Fermata(idStazA)));

				Connessione cnx = new Connessione(rs.getInt("id_connessione"), myLinea, myStazP, myStazA);

				connessioni.add(cnx);

			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}
		
		return connessioni;
	}
	
	
}
