package it.polito.tdp.yelp.model;

public class Event implements Comparable<Event>{
	
	public enum EventType {
		DA_INTERVISTARE, // scegliamo una persona che verrà intervistata da un certo giornalista
		FERIE // nessuna persona viene intervistata ma la scelta della persona da intervistare viene rimandata al giorno dopo
	}
	
	private int giorno;
	private EventType type;
	private User intervistato;
	private Giornalista giornalista;
	
	public Event(int giorno, EventType type, User intervistato, Giornalista giornalista) {
		super();
		this.giorno = giorno;
		this.type = type;
		this.intervistato = intervistato;
		this.giornalista = giornalista;
	}

	public int getGiorno() {
		return giorno;
	}
	
	public EventType getType() {
		return type;
	}

	public User getIntervistato() {
		return intervistato;
	}

	public Giornalista getGiornalista() {
		return giornalista;
	}

	@Override
	public int compareTo(Event other) {
		return this.giorno - other.giorno;
	}
	
}
