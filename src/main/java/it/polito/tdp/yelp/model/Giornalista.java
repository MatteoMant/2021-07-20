package it.polito.tdp.yelp.model;

public class Giornalista {
	
	private int id;	
	private int numeroIntervistati; // ogni giornalista deve sapere quante persone ha intervistato
	
	public Giornalista(int id) {
		super();
		this.id = id;
		this.numeroIntervistati = 0; // all'inizio il numero di persone intervistate è pari a 0
	}

	public int getId() {
		return id;
	}

	public int getNumeroIntervistati() {
		return numeroIntervistati;
	}
	
	public void incrementaNumeroIntervistati() {
		this.numeroIntervistati++;
	}
	
}
