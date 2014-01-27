package models;

import java.util.*;

public class Game {

	private static final String DEFAULT_GAME_ID = "12345";

	private String myGameId;
	private ArrayList<Player> players;

	public Game(){
		this.myGameId = DEFAULT_GAME_ID;
		this.players = new ArrayList<Player>();
	}

	public void addPlayer(String name){
		Player p = new Player(name);
		players.add(p);
		System.out.println("add player with name: " + name);
	}

	public void start(){
		System.out.println("Should start game: " + this.myGameId);
	}

	public String getGameId(){
		return this.myGameId;
	}

	public int getPlayerCount(){
		return this.players.size();
	}
}