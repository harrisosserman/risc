package models;

import java.util.*;

public class GameIDTracker {
	private static final int MAX_PLAYERS_PER_GAME = 5;

	private static GameIDTracker instance = null;

	private HashMap<String,Integer> gameCounts;
	private int numberOfGames;

	protected GameIDTracker(){
		gameCounts = new HashMap<String,Integer>();
		gameCounts.put("1", 0);
		numberOfGames = 1;
	}

	public static GameIDTracker getInstance(){
		if (instance == null) {
			instance = new GameIDTracker();
		}
		return instance;
	}

	public void addPlayerToGame(String gameID){
		int count = gameCounts.get(gameID);
		count++;
		gameCounts.put(gameID, count);
		System.out.println("count:" + count);
		if (count >= (MAX_PLAYERS_PER_GAME)) {
			numberOfGames++;
			gameCounts.put(Integer.toString(numberOfGames), 1);
		}
		System.out.println("new count:" + count);
		System.out.println("games:" + numberOfGames);
	}

	public String getAvailableGameID(){
		return Integer.toString(numberOfGames);
	}

}