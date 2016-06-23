package it.polito.tdp.metrodeparis.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

	static private final String jdbcUrl = "jdbc:mysql://localhost/metroparis?user=root";

	public static Connection getConnection() {
		try {
			Connection conn = DriverManager.getConnection(jdbcUrl) ;
			return conn ;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al database");
		}	
	}
}