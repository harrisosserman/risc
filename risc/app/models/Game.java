package models;

public class Game {

	private static final String DEFAULT_GAME_ID = "12345";

	public String myGameID;

	public Game(){
		this.myGameID = DEFAULT_GAME_ID;
	}

	public void addPlayer(String name){
		System.out.println("add player with name: " + name);
	}

	public void start(){
		System.out.println("Should start game: " + this.myGameID);
	}
}