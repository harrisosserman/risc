package models;

import java.util.*;

public class Game {

	private static final String DEFAULT_GAME_ID = "12345";
	private static final int NUM_TERRITORIES = 50;

	private String myGameId;
	private ArrayList<Player> myPlayers;
	private Territory[] myTerritories;

	public Game(){
		this.myGameId = DEFAULT_GAME_ID;
		this.myPlayers = new ArrayList<Player>();
	}

	public void addPlayer(String name){
		Player p = new Player(name);
		myPlayers.add(p);
	}

	public void start(){
		System.out.println("Should start game: " + this.myGameId);
		assert (myPlayers.size() > 1);

		myTerritories = divyCountries();
	}

	private Territory[] divyCountries(){
		int playerCount = myPlayers.size();

		ArrayList<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < NUM_TERRITORIES; i++){
			indices.add(i);
		}
		Collections.shuffle(indices);

		Territory[] territories = new Territory[NUM_TERRITORIES];
		for (int i = 0; i < NUM_TERRITORIES; i++) {
			int index = indices.indexOf(i);
			int ownerIndex = index % playerCount;

			Territory t = new Territory(i, myPlayers.get(ownerIndex));
			territories[i] = t;
		}

		return territories;
	}

	public String getGameId(){
		return this.myGameId;
	}

	public int getPlayerCount(){
		return this.myPlayers.size();
	}

	public ArrayList<Territory> territoriesOwnedByPlayer(Player p){
		ArrayList<Territory> playersTerritories = new ArrayList<Territory>();
		for (Territory t : myTerritories) {
			if (t.getOwner().equals(p)) {
				playersTerritories.add(t);
			}
		}
		return playersTerritories;
	}

	public int territoryCountForPlayer(Player p){
		int count = 0;
		for (Territory t : myTerritories) {
			if (t.getOwner().equals(p)) {
				count++;
			}
		}
		return count;
	}
}