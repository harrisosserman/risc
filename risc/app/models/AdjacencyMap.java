package models;

import java.util.*;

public class AdjacencyMap{

	private HashMap<Integer, ArrayList<Integer>> myAdjacencies; 

	public AdjacencyMap(){
		myAdjacencies = createMap();
	}

	public HashMap<Integer, ArrayList<Integer>> createMap(){
        return myAdjacencies;
	}

}