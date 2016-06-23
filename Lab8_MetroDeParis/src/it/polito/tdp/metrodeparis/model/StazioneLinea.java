package it.polito.tdp.metrodeparis.model;

public class StazioneLinea {
	Fermata staz;
	Linea linea;
	
	public StazioneLinea(Fermata f, Linea l){
		staz=f;
		linea=l;
	}

	public Fermata getStaz() {
		return staz;
	}

	public void setStaz(Fermata staz) {
		this.staz = staz;
	}

	public Linea getLinea() {
		return linea;
	}

	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linea == null) ? 0 : linea.hashCode());
		result = prime * result + ((staz == null) ? 0 : staz.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StazioneLinea other = (StazioneLinea) obj;
		if (linea == null) {
			if (other.linea != null)
				return false;
		} else if (!linea.equals(other.linea))
			return false;
		if (staz == null) {
			if (other.staz != null)
				return false;
		} else if (!staz.equals(other.staz))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StazioneLinea [staz=" + staz + ", linea=" + linea + "]";
	}

	
}
