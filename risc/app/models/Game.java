package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import java.net.UnknownHostException;

public class Game {

	private static final String DEFAULT_GAME_ID = "12345";
	private static final int NUM_TERRITORIES = 50;
	private static final String INITIALIZATION_DB = "initialization";
    private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
    private static final String NAME = "name";
    private static final String COUNT = "count";
    private static final String READY = "ready";
    private static final String PLAYERS = "players";

	private String myGameID;
	private ArrayList<Player> myPlayers;
	private Territory[] myTerritories;

	public Game(){
		this.myGameID = DEFAULT_GAME_ID;
		this.myPlayers = new ArrayList<Player>();
	}

	public void addPlayer(String name){	//can likely delete this
		Player p = new Player(name);
		myPlayers.add(p);
	}

	public void addWaitingPlayer(String playerName) throws UnknownHostException{
		MongoConnection connection = new MongoConnection();
        DB initialization = connection.getDB(INITIALIZATION_DB);
        DBCollection waitingPlayers = initialization.getCollection(WAITING_PLAYERS_COLLECTION);
        if (!initialization.collectionExists(WAITING_PLAYERS_COLLECTION)) {
            BasicDBObject firstPlayer = new BasicDBObject(NAME, playerName).append(READY, false);
            ArrayList<BasicDBObject> playersList = new ArrayList<BasicDBObject>();
            playersList.add(firstPlayer);
            BasicDBObject players = new BasicDBObject(PLAYERS, playersList);
            waitingPlayers.insert(players);

            BasicDBObject playerCount = new BasicDBObject(COUNT, 1);
            waitingPlayers.insert(playerCount);
        }else{
            BasicDBObject joiningPlayer = new BasicDBObject(PLAYERS, new BasicDBObject(NAME, playerName).append(READY, false));
            DBObject updateQuery = new BasicDBObject("$push", joiningPlayer);
            waitingPlayers.update(new BasicDBObject(), updateQuery);

            BasicDBObject query = new BasicDBObject(COUNT,  new BasicDBObject("$gte", 0));
            BasicDBObject incValue = new BasicDBObject(COUNT, 1);
            BasicDBObject intModifier = new BasicDBObject("$inc", incValue);
            waitingPlayers.update(query, intModifier);
        }

        connection.closeConnection();
	}

	public void start(){
		System.out.println("Should start game: " + this.myGameID);
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

	public String getGameID(){
		return this.myGameID;
	}

	public int getPlayerCount(){	//can likely delete later
		return this.myPlayers.size();
	}

	public int getWaitingPlayerCount() throws UnknownHostException{
		MongoConnection connection = new MongoConnection();
		DB initialization = connection.getDB(INITIALIZATION_DB);
        DBCollection waitingPlayers = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);
        BasicDBObject query = new BasicDBObject(COUNT,  new BasicDBObject("$gt", 0));
        DBCursor cursor = waitingPlayers.find(query);

        if (!cursor.hasNext()) {
        	return 0;
        }

        int count = (int)cursor.next().get(COUNT);
		return count;
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