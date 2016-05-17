package it.polito.tdp.metrodeparis.model;

import com.javadocmd.simplelatlng.LatLng;
/*
 * Tale classe è un Java Beans che rappresenta come oggetto una Fermata 
 * della Metropolitana di Parigi, solo che, a differenza della sua classe 
 * padre Fermata, essa contiene al suo interno anche la Linea su cui si trova
 * Perciò per ogni oggetto Fermata ci possono essere più Linee che ci passano 
 * e quindi più oggetti FermataSuLinea
 * 
 */

public class FermataSuLinea extends Fermata {

	private Linea linea;

	public FermataSuLinea(int idFermata, String nome, LatLng coords, Linea linea) {
		super(idFermata, nome, coords);
		this.linea = linea;
	}

	public FermataSuLinea(Fermata fermata, Linea linea) {
		super(fermata.getIdFermata(), fermata.getNome(), fermata.getCoords());
		this.linea = linea;
	}

	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	public Linea getLinea() {
		return this.linea;
	}

	//tale metodo non è come quello della classe padre, perciò faccio l'Override
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((linea == null) ? 0 : linea.hashCode());
		return result;
	}

	//tale metodo non è come quello della classe padre, perciò faccio l'Override
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FermataSuLinea other = (FermataSuLinea) obj;
		if (linea == null) {
			if (other.linea != null)
				return false;
		} else if (!linea.equals(other.linea))
			return false;
		return true;
	}

	//tale metodo non è come quello della classe padre, perciò faccio l'Override
	@Override
	public String toString() {
		return super.toString() + "/" + this.linea;
	}
}
