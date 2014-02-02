package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
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
    private static final String GAME_ID = "gameID";

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
        	BasicDBObject doc = new BasicDBObject();

            BasicDBObject firstPlayer = new BasicDBObject(NAME, playerName).append(READY, false);
            ArrayList<BasicDBObject> playersList = new ArrayList<BasicDBObject>();
            playersList.add(firstPlayer);
            doc.append(PLAYERS, playersList);
            doc.append(COUNT, 1);
            doc.append(GAME_ID, DEFAULT_GAME_ID);

            waitingPlayers.insert(doc);
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

	public String getWaitingPlayersJson(String gameID) throws UnknownHostException{
		MongoConnection connection = new MongoConnection();

		DBCursor playersList = getPlayersList(connection, gameID);
        String json = JSON.serialize(playersList);
        String trimmedJson = json.substring(1, json.length() - 1);	//need to remove '[' and ']' which converts it from BSON to JSON

        connection.closeConnection();
        return trimmedJson;
	}

	public void start(String gameID, int startingPlayerNumber, String startingPlayerName) throws UnknownHostException{
		//update initialization.waitingPlayers to show that someone is ready
		MongoConnection connection = new MongoConnection();

		markWaitingPlayerReady(connection, gameID, startingPlayerNumber, startingPlayerName);

		//divy territories
		// System.out.println("Should start game: " + this.myGameID);
		// myTerritories = divyCountries();

		//update game.map

		connection.closeConnection();
	}

	private void markWaitingPlayerReady(MongoConnection connection, String gameID, int playerNumber, String playerName) throws UnknownHostException{
		System.out.println("mark player " + playerName + " ready in game " + gameID);
		DBCursor playersListCursor = getPlayersList(connection, gameID);
		DBObject playersList = playersListCursor.next();

		ArrayList<BasicDBObject> players = (ArrayList<BasicDBObject>)playersList.get(PLAYERS);
		int playerIndex = playerNumber - 1;	// - 1 because 1-indexed instead of 0-indexed
		BasicDBObject startingPlayer = players.get(playerIndex);

		BasicDBObject newDocument = new BasicDBObject("$set", playersList);
		String readyPath = "players." + playerIndex + ".ready";
		newDocument.append("$set", new BasicDBObject().append(readyPath, true));

		DBCollection waitingPlayers = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);
		waitingPlayers.update(new BasicDBObject(), newDocument);
	}

	private DBCursor getPlayersList(MongoConnection connection, String gameID){
        DBCollection waitingPlayers = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);

        BasicDBObject query = new BasicDBObject(GAME_ID, gameID);
        DBCursor playersList = waitingPlayers.find(query);
        return playersList;
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
        connection.closeConnection();
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