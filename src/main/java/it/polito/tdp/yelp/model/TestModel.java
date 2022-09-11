package it.polito.tdp.yelp.model;

public class TestModel {

	public static void main(String[] args) {
		Model m = new Model();
		String messaggio = m.creaGrafo(200, 2007);
		System.out.println(messaggio);

	}

}
