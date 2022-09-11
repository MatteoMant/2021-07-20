package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	private Graph<User, DefaultWeightedEdge> grafo;
	private List<User> utenti; // elenco dei vertici del grafo
	
	// risultati del simulatore
	private int numeroGiorni;
	private List<Giornalista> giornalisti;
	
	public String creaGrafo(int minRevisioni, int anno) {
		this.grafo = new SimpleWeightedGraph<User, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		YelpDao dao = new YelpDao();
		this.utenti = dao.getUsersWithReviews(minRevisioni);
		
		// aggiunta dei vertici
		Graphs.addAllVertices(this.grafo, this.utenti);
		
		// aggiunta degli archi (li creiamo uno ad uno andando a considerare tutte le coppie di vertici)
		for (User u1 : this.utenti) {
			for (User u2 : this.utenti) {
				if (!u1.equals(u2) && u1.getUserId().compareTo(u2.getUserId()) < 0) { // l'ultima condizione mi permette di evitare 
					int sim = dao.calcolaSimilarita(u1, u2, anno); // di aggiungere lo stesso arco 2 volte (evitiamo metà delle chiamate al dao)
					if (sim > 0) { // aggiungo l'arco se il numero di recensioni in comune tra i due utenti è maggiore di 0
						Graphs.addEdge(this.grafo, u1, u2, sim); // il peso è proprio la similarità
					}
				}
			}
		}
		
		return "Grafo creato con " + this.grafo.vertexSet().size() + " vertici e " + 
				this.grafo.edgeSet().size() + " archi\n";		
	}
	
	public List<User> utentiPiuSimili(User u){
		// faccio un primo ciclo for per calcolare il peso massimo degli archi adiacenti
		int max = 0;
		for (DefaultWeightedEdge e : this.grafo.edgesOf(u)) { // scorro su tutti gli archi che toccano il vertice u
			if (this.grafo.getEdgeWeight(e) > max) {
				max = (int)this.grafo.getEdgeWeight(e);
			}	
		}
		// arrivato qui ho trovato il peso massimo degli archi adiacenti al vertice u 
		List<User> result = new ArrayList<User>();
		for (DefaultWeightedEdge e : this.grafo.edgesOf(u)) {
			if (this.grafo.getEdgeWeight(e) == max) {
				User u2 = Graphs.getOppositeVertex(this.grafo, e, u); // metodo che ci dà il vertice opposto al vertice 'u' nell'arco 'e'
				result.add(u2);
			}
		}
		return result;
	}
	
	public void simula(int intervistatori, int utenti) {
		Simulatore sim = new Simulatore(this.grafo);
		sim.init(intervistatori, utenti);
		sim.run();
		this.giornalisti = sim.getGiornalisti();
		this.numeroGiorni = sim.getNumeroGiorni();
	}
	
	public List<User> getUsers() {
		return this.utenti;
	}

	public int getNumeroGiorni() {
		return numeroGiorni;
	}

	public List<Giornalista> getGiornalisti() {
		return giornalisti;
	}
}
