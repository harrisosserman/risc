package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import libraries.DBHelper;

public class Game {

    private static final String DEFAULT_GAME_ID = "12345";
    private static final int NUM_TERRITORIES = 50;
    private static final int TOTAL_TROOP_COUNT = 240;   //(2*3*4*5)*2
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
        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();

        BasicDBObject waitingPlayersQuery = new BasicDBObject(GAME_ID, myGameID);
        DBCursor waitingPlayersCursor = waitingPlayers.find(waitingPlayersQuery);

        if (!waitingPlayersCursor.hasNext()) {
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
    }

    public String getWaitingPlayersJson(String gameID) throws UnknownHostException{
        DBCursor playersList = getPlayersList(gameID);
        String json = JSON.serialize(playersList);
        String trimmedJson = json.substring(1, json.length() - 1);  //need to remove '[' and ']' which converts it from BSON to JSON
        return trimmedJson;
    }

    public void start(String gameID, int startingPlayerNumber, String startingPlayerName) throws UnknownHostException{
        //update initialization.waitingPlayers to show that someone is ready
        markWaitingPlayerReady(gameID, startingPlayerNumber, startingPlayerName);

		if (areAllPlayersReady(gameID) && getWaitingPlayerCount() >= 2) {
			int[] territoryOwners = assignCountryOwners(getWaitingPlayerCount());
			makeInitialGameMap(territoryOwners, gameID);
		}
	}

    private boolean areAllPlayersReady(String gameID) throws UnknownHostException{
        int waitingPlayerCount = getWaitingPlayerCount();

        DBCursor playersListCursor = getPlayersList(gameID);
        if (!playersListCursor.hasNext()) {
            return false;
        }

        DBObject playersList = playersListCursor.next();

        ArrayList<BasicDBObject> players = (ArrayList<BasicDBObject>)playersList.get(PLAYERS);
        int readyCount = 0;
        for (DBObject player : players) {
           if ((Boolean)player.get(READY)) {
                readyCount++;
           }
        }
        return readyCount == waitingPlayerCount;
    }

    public boolean areAllPlayersCommitted() throws UnknownHostException{
        DBCollection committedTurnsCollection = DBHelper.getCommittedTurnsCollection();
        BasicDBObject committedTurnsQuery = new BasicDBObject(GAME_ID, DEFAULT_GAME_ID);
        DBCursor committedTurnsCursor = committedTurnsCollection.find(committedTurnsQuery).sort(new BasicDBObject(TURN, -1));
        int turn = -1;
        int committedTurnsInSameTurn = 0;
        while(committedTurnsCursor.hasNext()) {
            DBObject committedTurn = committedTurnsCursor.next();
            if(turn == -1) {
                turn = (Integer)committedTurn.get(TURN);
            }
            if(turn != (Integer)committedTurn.get(TURN)) {
                break;
            }
            committedTurnsInSameTurn++;
        }
        if(turn == -1) {
            return false;
        }

        DBCollection stateCollection = DBHelper.getStateCollection();
        BasicDBObject stateQuery = new BasicDBObject(GAME_ID, DEFAULT_GAME_ID).append(TURN, turn);
        DBObject state = stateCollection.findOne(stateQuery);
        if(state == null) {
            return false;
        }

        return true;
    }

	private boolean gameMapHasBeenCreated(String gameID){
		DBCollection mapCollection = DBHelper.getMapCollection();

        BasicDBObject query = new BasicDBObject(GAME_ID, gameID);
        DBCursor map = mapCollection.find(query);

        return map.hasNext();
	}

	public boolean canPlayersStillJoin() throws UnknownHostException{
		if (getWaitingPlayerCount() >= 5) {
			return false;
		}

		if (gameMapHasBeenCreated(myGameID)) {
			return false;
		}

		return true;
	}

    private void markWaitingPlayerReady(String gameID, int playerNumber, String playerName) throws UnknownHostException{
        DBCursor playersListCursor = getPlayersList(gameID);
        DBObject playersList = playersListCursor.next();

        ArrayList<BasicDBObject> players = (ArrayList<BasicDBObject>)playersList.get(PLAYERS);
        int playerIndex = playerNumber - 1; // - 1 because 1-indexed instead of 0-indexed
        BasicDBObject startingPlayer = players.get(playerIndex);

        BasicDBObject newDocument = new BasicDBObject("$set", playersList);
        String readyPath = PLAYERS + "." + playerIndex + "." + READY;
        newDocument.append("$set", new BasicDBObject().append(readyPath, true));

        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();
        waitingPlayers.update(new BasicDBObject(), newDocument);
    }

    private void makeInitialGameMap(int[] territoryOwners, String gameID) throws UnknownHostException{
        DBCollection map = DBHelper.getMapCollection();

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
    }

    private DBCursor getPlayersList(String gameID){
        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();

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
        DBCollection mapCollection = DBHelper.getMapCollection();

        BasicDBObject query = new BasicDBObject(GAME_ID, gameID);
        DBCursor mapCursor = mapCollection.find(query);

        String json = JSON.serialize(mapCursor);
        String trimmedJson = json.substring(1, json.length() - 1);  //need to remove '[' and ']' which converts it from BSON to JSON

        return trimmedJson;
    }

    public String getCurrentGameStateJson(String gameID) throws UnknownHostException{
        DBCollection stateCollection = DBHelper.getStateCollection();

        int currentTurnCount = getTurnCount(gameID);

        BasicDBObject currentTurnQuery = new BasicDBObject(GAME_ID, gameID);
        currentTurnQuery.append(TURN, currentTurnCount);
        DBObject currentTurn = stateCollection.findOne(currentTurnQuery);

        return currentTurn.toString();
    }

    private int getTurnCount(String gameID){
        DBCollection stateCollection = DBHelper.getStateCollection();

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
        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();
        BasicDBObject query = new BasicDBObject(COUNT,  new BasicDBObject("$gt", 0));
        DBCursor cursor = waitingPlayers.find(query);

        if (!cursor.hasNext()) {
            return 0;
        }

        Integer count = ((Integer)(cursor.next().get(COUNT)));
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
        DBCollection stateCollection = DBHelper.getStateCollection();

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
    }
}