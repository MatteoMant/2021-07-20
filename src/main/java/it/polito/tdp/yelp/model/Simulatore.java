package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.yelp.model.Event.EventType;

public class Simulatore {

	// Dati in ingresso
	private int x1;
	private int x2;
	
	// Dati in uscita
	private List<Giornalista> giornalisti;	// i giornalisti sono rappresentati da un numero compreso tra 0 e x1-1
	private int numeroGiorni; // numero di giorni che servono per intervistare tutte le x2 persone
	
	// Modello del mondo
	private Set<User> intervistati; // lo stato del mondo tiene conto delle persone che sono state intervistate e chi no
	private Graph<User, DefaultWeightedEdge> grafo;
	
	// Coda degli eventi
	private PriorityQueue<Event> queue;
	
	public Simulatore(Graph<User, DefaultWeightedEdge> grafo) {
		this.grafo = grafo;
	}
	
	public void init(int x1, int x2) { // inizializziamo x1 e x2 in init() cosi possiamo fare diverse simulazioni con vari valori nello stesso grafo
		this.x1 = x1;
		this.x2 = x2;
		
		this.intervistati = new HashSet<User>(); // inizialmente l'insieme degli intervistati sarà vuoto
	
		this.numeroGiorni = 0; // siamo al giorno 0 della simulazione
		
		this.giornalisti = new ArrayList<>();
		for (int id = 0; id < this.x1; id++) {
			this.giornalisti.add(new Giornalista(id)); // creiamo una lista di "x1" giornalisti (ciascuno con 0 intervistati all'inizio)
		}
		
		// creo la coda
		this.queue = new PriorityQueue<>();
		
		// adesso pre-carico la coda con le interviste che verranno fatte nel primo giorno
		for (Giornalista g : this.giornalisti) { // ad ogni giornalista assegniamo una persona da intervistare creando il relativo evento
			User intervistato = selezionaIntervistato(this.grafo.vertexSet()); // l'intervistato viene scelto a caso dall'insieme dei vertici presenti nel grafo
			this.intervistati.add(intervistato); // in questo modo evitiamo di intervistare due volte la stessa persona ("black-list")
			g.incrementaNumeroIntervistati();
			this.queue.add(new Event(1, EventType.DA_INTERVISTARE, intervistato, g)); // nella coda metto un evento per ogni giornalista che dovrà intervistare una persona scelta a caso
		}
	}
	
	public void run() {
		while(!this.queue.isEmpty() && this.intervistati.size() < x2) { // appena ho intervistato x2 persone mi fermo
			Event e = this.queue.poll(); // estraggo l'evento
			this.numeroGiorni = e.getGiorno(); // aggiorno la durata della simulazione (il valore finale sarà il giorno dell'ultimo evento estratto)
			processEvent(e); // elaboro l'evento
		}
	}
	
	private void processEvent(Event e) {
		switch(e.getType()) {
		case DA_INTERVISTARE:
			
			double caso = Math.random(); // tiro un numero a caso 
			
			if (caso < 0.6) {
				// caso I
				User vicino = selezionaAdiacente(e.getIntervistato()); // questo metodo gestisce già tutta una serie di casi particolari
				
				if (vicino == null) {
					vicino = selezionaIntervistato(this.grafo.vertexSet()); // scelta dell'intervistato dall'intero grafo
				}
				
				this.queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, vicino, e.getGiornalista()));
				this.intervistati.add(vicino);
				e.getGiornalista().incrementaNumeroIntervistati();
				
			}else if (caso < 0.8) {
				// caso II: rimando all'indomani la scelta della persona da intervistare
				this.queue.add(new Event(e.getGiorno()+1, EventType.FERIE, e.getIntervistato(), e.getGiornalista()));
			}else {
				// caso III: domani continuo con lo stesso utente (restante 20%)
				this.queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, e.getIntervistato(), e.getGiornalista()));
			}
			
			break;
		case FERIE:
			User vicino = selezionaAdiacente(e.getIntervistato());
			if(vicino == null) {
				vicino = selezionaIntervistato(this.grafo.vertexSet());
			}
			
			this.queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, vicino, e.getGiornalista())) ;
			
			this.intervistati.add(vicino);
			e.getGiornalista().incrementaNumeroIntervistati();

			break;
		}
	}

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public List<Giornalista> getGiornalisti() {
		return giornalisti;
	}

	public int getNumeroGiorni() {
		return numeroGiorni;
	}
	
	/**
	 * Seleziona un intervistato dalla Collection passata come parametro, evitando di selezionare coloro che
	 * sono già presenti in this.intervistati
	 * @param lista
	 * @return
	 */
	private User selezionaIntervistato(Collection<User> lista) {
		Set<User> candidati = new HashSet<User>(lista); // creo un Set, facendo una copia della "lista"
		candidati.removeAll(this.intervistati); // in questo modo ottengo l'insieme delle persone che possono essere intervistate
		
		if (candidati.size() == 0) { // nessun User presente nel grafo
			return null;
		}
		
		int scelto = (int)(Math.random()*candidati.size()); // indice casuale per estarre un elemento da "candidati"
		
		return (new ArrayList<User>(candidati)).get(scelto); // da una lista prendo un elemento in posizione casuale usando come indice "scelto"
	}
	
	private User selezionaAdiacente(User u) {
		List<User> vicini = Graphs.neighborListOf(this.grafo, u);
		vicini.removeAll(this.intervistati); // dopo aver tolto gli intervistati posso calcolare il massimo
		
		if (vicini.size() == 0) {
			// in questo caso il vertice è isolato oppure tutti i suoi adiacenti sono già stati intervistati
			return null;
		}
		
		double max = 0.0;
		for (User v : vicini) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(u, v));
			if (peso > max) {
				max = peso;
			}
		}
		// dopo aver calcolato il massimo creo una lista di vicini che hanno questo peso "max"
		List<User> migliori = new ArrayList<>();
		for (User v : vicini) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(u, v));
			if (peso == max) {
				migliori.add(v); // aggiungo il vertice alla lista dei vertici "migliori"
			}
		}
		
		int scelto = (int)(Math.random() * migliori.size());
		return migliori.get(scelto); // restituisco un elemento a caso da questa lista
	}
}
