package it.polito.tdp.metrodeparis.model;

import java.util.ArrayList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;
/*
 * Tale classe è un Java Beans che rappresenta come oggetto una Fermata 
 * della Metropolitana di Parigi
 * 
 */

public class Fermata {

	private int idFermata;
	private String nome;
	private LatLng coords;
	//in ogni oggetto Fermata è utile memorizzare la List di tutte le fermateSuLinea
	//in cui esso è la fermata 
	private List<FermataSuLinea> fermateSuLinea;

	public Fermata(int idFermata, String nome, LatLng coords) {
		this.idFermata = idFermata;
		this.nome = nome;
		this.coords = coords;
		this.fermateSuLinea = new ArrayList<FermataSuLinea>();
	}

	public Fermata(int idFermata) {
		this.idFermata = idFermata;
	}

	public List<FermataSuLinea> getFermateSuLinea() {
		return this.fermateSuLinea;
	}

	public void addFermataSuLinea(FermataSuLinea fermataSuLinea) {
		this.fermateSuLinea.add(fermataSuLinea);
	}

	public int getIdFermata() {
		return idFermata;
	}

	public void setIdFermata(int idFermata) {
		this.idFermata = idFermata;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public LatLng getCoords() {
		return coords;
	}

	public void setCoords(LatLng coords) {
		this.coords = coords;
	}

	@Override
	public String toString() {
		return nome;
	}

	@Override
	public int hashCode() {
		return ((Integer) idFermata).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fermata other = (Fermata) obj;
		if (idFermata != other.idFermata)
			return false;
		return true;
	}

}