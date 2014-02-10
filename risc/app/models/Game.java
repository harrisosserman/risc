package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;

public class Game {

    private static final String DEFAULT_GAME_ID = "12345";
    private static final int NUM_TERRITORIES = 50;
    private static final int TOTAL_TROOP_COUNT = 240;   //(2*3*4*5)*2
    private static final String INITIALIZATION_DB = "initialization";
    private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
    private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
    private static final String STATE_COLLECTION = "state";
    private static final String GAME_DB = "game";
    private static final String MAP_COLLECTION = "map";
    private static final String NAME = "name";
    private static final String COUNT = "count";
    private static final String READY = "ready";
    private static final String PLAYERS = "players";
    private static final String GAME_ID = "gameID";
    private static final String NUM_PLAYERS = "numPlayers";
    private static final String TERRITORIES = "territories";
    private static final String OWNER = "owner";
    private static final String TROOPS = "troops";
    private static final String ADDITIONAL_TROOPS = "additionalTroops";
    private static final String TURN = "turn";
    private static final String ACTIVE_PLAYER_COUNT = "activePlayerCount";
    private static final String ACTIVE_PLAYERS = "activePlayers";
    private static final String PLAYER_NUMBER = "playerNumber";

    private String myGameID;
    private ArrayList<Player> myPlayers;
    private Territory[] myTerritories;

    public Game(){
        this.myGameID = DEFAULT_GAME_ID;
        this.myPlayers = new ArrayList<Player>();
    }

    public void addPlayer(String name){ //can likely delete this
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
        String trimmedJson = json.substring(1, json.length() - 1);  //need to remove '[' and ']' which converts it from BSON to JSON

        connection.closeConnection();
        return trimmedJson;
    }

    public void start(String gameID, int startingPlayerNumber, String startingPlayerName) throws UnknownHostException{
        //update initialization.waitingPlayers to show that someone is ready
        MongoConnection connection = new MongoConnection();

        markWaitingPlayerReady(connection, gameID, startingPlayerNumber, startingPlayerName);

		if (areAllPlayersReady(connection, gameID) && getWaitingPlayerCount() >= 2) {
			int[] territoryOwners = assignCountryOwners(getWaitingPlayerCount());
			makeInitialGameMap(territoryOwners, gameID);
		}

		connection.closeConnection();
	}

    private boolean areAllPlayersReady(MongoConnection connection, String gameID) throws UnknownHostException{
        int waitingPlayerCount = getWaitingPlayerCount();

        DBCursor playersListCursor = getPlayersList(connection, gameID);
        if (!playersListCursor.hasNext()) {
            return false;
        }

        DBObject playersList = playersListCursor.next();

        ArrayList<BasicDBObject> players = (ArrayList<BasicDBObject>)playersList.get(PLAYERS);
        int readyCount = 0;
        for (DBObject player : players) {
           if ((boolean)player.get(READY)) {
                readyCount++;
           }
        }
        return readyCount == waitingPlayerCount;
    }

    public boolean areAllPlayersCommitted() throws UnknownHostException{
        MongoConnection connection = new MongoConnection();

        DBCollection stateCollection = connection.getDB(GAME_DB).getCollection(STATE_COLLECTION);
        BasicDBObject stateQuery = new BasicDBObject(GAME_ID, DEFAULT_GAME_ID);
        DBObject state = stateCollection.findOne(stateQuery);
        int completedTurns = (Integer)state.get(TURN);

        DBCollection committedTurnsCollection = connection.getDB(GAME_DB).getCollection(COMMITTED_TURNS_COLLECTION);
        BasicDBObject committedTurnsQuery = new BasicDBObject(GAME_ID, DEFAULT_GAME_ID);
        committedTurnsQuery.append(TURN, new Integer(completedTurns));
        DBCursor committedTurnsCursor = committedTurnsCollection.find(committedTurnsQuery);
        int committedTurnsCount = committedTurnsCursor.count();

        //TODO: put activePlayerCount into game.state
        DBCollection waitingPlayersCollection = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);
        BasicDBObject waitingPlayersQuery = new BasicDBObject(GAME_ID, DEFAULT_GAME_ID);
        DBObject waitingPlayers = waitingPlayersCollection.findOne(waitingPlayersQuery);
        int playerCount = (Integer)waitingPlayers.get(COUNT);

        connection.closeConnection();
        return (playerCount == committedTurnsCount);
    }

	private boolean gameMapHasBeenCreated(MongoConnection connection, String gameID){
		DBCollection mapCollection = connection.getDB(GAME_DB).getCollection(MAP_COLLECTION);

        BasicDBObject query = new BasicDBObject(GAME_ID, gameID);
        DBCursor map = mapCollection.find(query);

        return map.hasNext();
	}

	public boolean canPlayersStillJoin() throws UnknownHostException{
		if (getWaitingPlayerCount() >= 5) {
			return false;
		}

		MongoConnection connection = new MongoConnection();
		if (gameMapHasBeenCreated(connection, myGameID)) {
			return false;
		}
		connection.closeConnection();

		return true;
	}

    private void markWaitingPlayerReady(MongoConnection connection, String gameID, int playerNumber, String playerName) throws UnknownHostException{
        DBCursor playersListCursor = getPlayersList(connection, gameID);
        DBObject playersList = playersListCursor.next();

        ArrayList<BasicDBObject> players = (ArrayList<BasicDBObject>)playersList.get(PLAYERS);
        int playerIndex = playerNumber - 1; // - 1 because 1-indexed instead of 0-indexed
        BasicDBObject startingPlayer = players.get(playerIndex);

        BasicDBObject newDocument = new BasicDBObject("$set", playersList);
        String readyPath = PLAYERS + "." + playerIndex + "." + READY;
        newDocument.append("$set", new BasicDBObject().append(readyPath, true));

        DBCollection waitingPlayers = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);
        waitingPlayers.update(new BasicDBObject(), newDocument);
    }

    private void makeInitialGameMap(int[] territoryOwners, String gameID) throws UnknownHostException{
        MongoConnection connection = new MongoConnection();
        DBCollection map = connection.getDB(GAME_DB).getCollection(MAP_COLLECTION);

        BasicDBObject doc = new BasicDBObject();

        doc.append(GAME_ID, gameID);

        int waitingPlayerCount = getWaitingPlayerCount();
        doc.append(NUM_PLAYERS, waitingPlayerCount);

        ArrayList<BasicDBObject> territories = new ArrayList<BasicDBObject>();
        for (int ownerIndex : territoryOwners) {
            BasicDBObject territory = new BasicDBObject();
            int ownerNumber = ownerIndex + 1; //beacuse 1-indexed instead of 0-indexed
            territory.append(OWNER, ownerNumber);
            territory.append(TROOPS, 0);

            territories.add(territory);
        }
        doc.append(TERRITORIES, territories);

        ArrayList<BasicDBObject> additionalTroops = new ArrayList<BasicDBObject>();
        for (int i = 0; i < waitingPlayerCount; i++) {
        	BasicDBObject additionalTroop = new BasicDBObject();
        	int ownerNumber = i + 1;
        	additionalTroop.append(OWNER, ownerNumber);
        	additionalTroop.append(TROOPS, (TOTAL_TROOP_COUNT/waitingPlayerCount));
            additionalTroops.add(additionalTroop);
        }
        doc.append(ADDITIONAL_TROOPS, additionalTroops);

        map.insert(doc);

        connection.closeConnection();
    }

    private DBCursor getPlayersList(MongoConnection connection, String gameID){
        DBCollection waitingPlayers = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);

        BasicDBObject query = new BasicDBObject(GAME_ID, gameID);
        DBCursor playersList = waitingPlayers.find(query);
        return playersList;
    }

    private int[] assignCountryOwners(int playerCount) {
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for(int i = 0; i < NUM_TERRITORIES; i++){
            indices.add(i);
        }
        Collections.shuffle(indices);

        int[] owners = new int[NUM_TERRITORIES];
        for (int i = 0; i < NUM_TERRITORIES; i++) {
            int index = indices.indexOf(i);
            int ownerIndex = index % playerCount;
            owners[i] = ownerIndex;
        }
        return owners;
    }

    public String getMapJson(String gameID) throws UnknownHostException{
        MongoConnection connection = new MongoConnection();
        DBCollection waitingPlayers = connection.getDB(GAME_DB).getCollection(MAP_COLLECTION);

        BasicDBObject query = new BasicDBObject(GAME_ID, gameID);
        DBCursor mapCursor = waitingPlayers.find(query);

        String json = JSON.serialize(mapCursor);
        String trimmedJson = json.substring(1, json.length() - 1);  //need to remove '[' and ']' which converts it from BSON to JSON

        connection.closeConnection();

        return trimmedJson;
    }

    public String getCurrentGameStateJson(String gameID) throws UnknownHostException{
        MongoConnection connection = new MongoConnection();
        DBCollection stateCollection = connection.getDB(GAME_DB).getCollection(STATE_COLLECTION);

        int currentTurnCount = getTurnCount(connection, gameID);

        BasicDBObject currentTurnQuery = new BasicDBObject(GAME_ID, gameID);
        currentTurnQuery.append(TURN, currentTurnCount);
        DBObject currentTurn = stateCollection.findOne(currentTurnQuery);

        return currentTurn.toString();
    }

    private int getTurnCount(MongoConnection connection, String gameID){
        DBCollection stateCollection = connection.getDB(GAME_DB).getCollection(STATE_COLLECTION);

        BasicDBObject stateQuery = new BasicDBObject(GAME_ID, gameID);
        DBCursor allTurnsCursor = stateCollection.find(stateQuery);
        int currentTurnCount = allTurnsCursor.count();
        return currentTurnCount;
    }

    public String getGameID(){
        return this.myGameID;
    }

    public int getPlayerCount(){    //can likely delete later
        return this.myPlayers.size();
    }

    public Integer getWaitingPlayerCount() throws UnknownHostException{
        MongoConnection connection = new MongoConnection();
        DB initialization = connection.getDB(INITIALIZATION_DB);
        DBCollection waitingPlayers = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);
        BasicDBObject query = new BasicDBObject(COUNT,  new BasicDBObject("$gt", 0));
        DBCursor cursor = waitingPlayers.find(query);

        if (!cursor.hasNext()) {
            return 0;
        }

        Integer count = ((Integer)(cursor.next().get(COUNT)));
        connection.closeConnection();
        return count;
    }

    public ArrayList<Territory> territoriesOwnedByPlayer(int pid){
        ArrayList<Territory> playersTerritories = new ArrayList<Territory>();
        for (Territory t : myTerritories) {
            if (t.getOwner() == pid) {
                playersTerritories.add(t);
            }
        }
        return playersTerritories;
    }

    public int territoryCountForPlayer(int pid){
        int count = 0;
        for (Territory t : myTerritories) {
            if (t.getOwner() == pid) {
                count++;
            }
        }
        return count;
    }

    public void removePlayer(int pid) throws UnknownHostException{
        //decrement game.state.activePlayerCount
        MongoConnection connection = new MongoConnection();
        DBCollection stateCollection = connection.getDB(GAME_DB).getCollection(STATE_COLLECTION);

        BasicDBObject stateQuery = new BasicDBObject(GAME_ID, myGameID);
        DBObject highestTurn = stateCollection.find(stateQuery).sort( new BasicDBObject(TURN, -1)).next();
        int highestTurnCount = (Integer)highestTurn.get(TURN);

        BasicDBObject updateCriteria = new BasicDBObject(GAME_ID, myGameID).append(TURN, highestTurnCount);
        BasicDBObject incValue = new BasicDBObject(ACTIVE_PLAYER_COUNT, -1);
        BasicDBObject intModifier = new BasicDBObject("$inc", incValue);
        stateCollection.update(updateCriteria, intModifier);

        //remove player from activePlayers
        ArrayList<DBObject> activePlayers = (ArrayList<DBObject>)highestTurn.get(ACTIVE_PLAYERS);
        ArrayList<DBObject> updatedActivePlayers = new ArrayList<DBObject>();
        for (DBObject activePlayer : activePlayers) {
            if ((Integer)activePlayer.get(PLAYER_NUMBER) != pid) {
                updatedActivePlayers.add(activePlayer);
            }
        }

        //Then set all troops in that player's territories to 0
        ArrayList<DBObject> territories = (ArrayList<DBObject>)highestTurn.get(TERRITORIES);
        for (DBObject territory : territories) {
            boolean isOwnedByPid = ((Integer)territory.get(OWNER)).equals(pid);
            if (isOwnedByPid) {
                territory.put(TROOPS, 0);
            }
        }

        BasicDBObject newDocument = new BasicDBObject("$set", highestTurn);
        newDocument.append("$set", new BasicDBObject(TERRITORIES, territories));
        newDocument.append("$set", new BasicDBObject(ACTIVE_PLAYERS, updatedActivePlayers));
        stateCollection.update(updateCriteria, newDocument);

        connection.closeConnection();
    }
}