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

    private String myGameID;
    private ArrayList<Player> myPlayers;
    private Territory[] myTerritories;

    public Game(){
        this.myGameID = DEFAULT_GAME_ID;
        this.myPlayers = new ArrayList<Player>();
    }

    public void addWaitingPlayer(String playerName) throws UnknownHostException{
        DBCollection waitingPlayersCollection = DBHelper.getWaitingPlayersCollection();

        DBObject waitingPlayers = DBHelper.getWaitingPlayersForGame(myGameID);      

        if (waitingPlayers == null) {
            BasicDBObject doc = new BasicDBObject();

            BasicDBObject firstPlayer = new BasicDBObject(DBHelper.NAME_KEY, playerName).append(DBHelper.READY_KEY, false);
            ArrayList<BasicDBObject> playersList = new ArrayList<BasicDBObject>();
            playersList.add(firstPlayer);
            doc.append(DBHelper.PLAYERS_KEY, playersList);
            doc.append(DBHelper.COUNT_KEY, 1);
            doc.append(DBHelper.GAME_ID_KEY, DEFAULT_GAME_ID);

            waitingPlayersCollection.insert(doc);
        }else{
            BasicDBObject joiningPlayer = new BasicDBObject(DBHelper.PLAYERS_KEY, new BasicDBObject(DBHelper.NAME_KEY, playerName).append(DBHelper.READY_KEY, false));
            DBObject updateQuery = new BasicDBObject("$push", joiningPlayer);
            waitingPlayersCollection.update(new BasicDBObject(), updateQuery);

            BasicDBObject query = new BasicDBObject(DBHelper.COUNT_KEY,  new BasicDBObject("$gte", 0));
            BasicDBObject incValue = new BasicDBObject(DBHelper.COUNT_KEY, 1);
            BasicDBObject intModifier = new BasicDBObject("$inc", incValue);
            waitingPlayersCollection.update(query, intModifier);
        }
    }

    public String getWaitingPlayersJson(String gameID) throws UnknownHostException{
        DBObject waitingPlayers = DBHelper.getWaitingPlayersForGame(myGameID);
        return waitingPlayers.toString();
    }

    public void start(String gameID, int startingPlayerNumber, String startingPlayerName) throws UnknownHostException{
        //update initialization.waitingPlayers to show that someone is ready
        markWaitingPlayerReady(gameID, startingPlayerNumber, startingPlayerName);

		if (areAllPlayersReady(gameID) && getWaitingPlayerCount() >= 2) {
			int[] territoryOwners = assignCountryOwners(getWaitingPlayerCount());
			makeInitialGameMap(territoryOwners, gameID);
            makeInitialInfo();
		}
	}

    private boolean areAllPlayersReady(String gameID) throws UnknownHostException{
        int waitingPlayerCount = getWaitingPlayerCount();

        DBObject waitingPlayers = DBHelper.getWaitingPlayersForGame(gameID);
        if (waitingPlayers == null) {
            return false;
        }

        ArrayList<BasicDBObject> players = (ArrayList<BasicDBObject>)waitingPlayers.get(DBHelper.PLAYERS_KEY);
        int readyCount = 0;
        for (DBObject player : players) {
           if ((Boolean)player.get(DBHelper.READY_KEY)) {
                readyCount++;
           }
        }
        return readyCount == waitingPlayerCount;
    }

    public boolean areAllPlayersCommitted() throws UnknownHostException{
        DBCollection committedTurnsCollection = DBHelper.getCommittedTurnsCollection();
        BasicDBObject committedTurnsQuery = new BasicDBObject(DBHelper.GAME_ID_KEY, DEFAULT_GAME_ID);
        DBCursor committedTurnsCursor = committedTurnsCollection.find(committedTurnsQuery).sort(new BasicDBObject(DBHelper.TURN_KEY, -1));
        int turn = -1;
        int committedTurnsInSameTurn = 0;
        while(committedTurnsCursor.hasNext()) {
            DBObject committedTurn = committedTurnsCursor.next();
            if(turn == -1) {
                turn = (Integer)committedTurn.get(DBHelper.TURN_KEY);
            }
            if(turn != (Integer)committedTurn.get(DBHelper.TURN_KEY)) {
                break;
            }
            committedTurnsInSameTurn++;
        }
        if(turn == -1) {
            return false;
        }

        DBCollection stateCollection = DBHelper.getStateCollection();
        BasicDBObject stateQuery = new BasicDBObject(DBHelper.GAME_ID_KEY, DEFAULT_GAME_ID).append(DBHelper.TURN_KEY, turn);
        DBObject state = stateCollection.findOne(stateQuery);
        if(state == null) {
            return false;
        }

        return true;
    }

	private boolean gameMapHasBeenCreated(String gameID){
        DBObject map = DBHelper.getMapForGame(gameID);
        return (map != null);
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
        DBObject waitingPlayers = DBHelper.getWaitingPlayersForGame(gameID);

        ArrayList<BasicDBObject> players = (ArrayList<BasicDBObject>)waitingPlayers.get(DBHelper.PLAYERS_KEY);
        int playerIndex = playerNumber - 1; // - 1 because 1-indexed instead of 0-indexed
        BasicDBObject startingPlayer = players.get(playerIndex);

        BasicDBObject newDocument = new BasicDBObject("$set", waitingPlayers);
        String readyPath = DBHelper.PLAYERS_KEY + "." + playerIndex + "." + DBHelper.READY_KEY;
        newDocument.append("$set", new BasicDBObject().append(readyPath, true));

        DBCollection waitingPlayersCollection = DBHelper.getWaitingPlayersCollection();
        waitingPlayersCollection.update(new BasicDBObject(), newDocument);
    }

    private void makeInitialGameMap(int[] territoryOwners, String gameID) throws UnknownHostException{
        DBCollection mapCollection = DBHelper.getMapCollection();

        BasicDBObject doc = new BasicDBObject();

        doc.append(DBHelper.GAME_ID_KEY, gameID);

        int waitingPlayerCount = getWaitingPlayerCount();
        doc.append(DBHelper.NUM_PLAYERS_KEY, waitingPlayerCount);

        ArrayList<BasicDBObject> territories = new ArrayList<BasicDBObject>();
        for (int ownerIndex : territoryOwners) {
            BasicDBObject territory = new BasicDBObject();
            int ownerNumber = ownerIndex + 1; //beacuse 1-indexed instead of 0-indexed
            territory.append(DBHelper.OWNER_KEY, ownerNumber);
            territory.append(DBHelper.TROOPS_KEY, 0);

            territories.add(territory);
        }
        doc.append(DBHelper.TERRITORIES_KEY, territories);

        ArrayList<BasicDBObject> additionalTroops = new ArrayList<BasicDBObject>();
        for (int i = 0; i < waitingPlayerCount; i++) {
        	BasicDBObject additionalTroop = new BasicDBObject();
        	int ownerNumber = i + 1;
        	additionalTroop.append(DBHelper.OWNER_KEY, ownerNumber);
        	additionalTroop.append(DBHelper.TROOPS_KEY, (TOTAL_TROOP_COUNT/waitingPlayerCount));
            additionalTroops.add(additionalTroop);
        }
        doc.append(DBHelper.ADDITIONAL_TROOPS_KEY, additionalTroops);

        mapCollection.insert(doc);
    }

    private void makeInitialInfo(){
        DBObject waitingPlayers = DBHelper.getWaitingPlayersForGame(myGameID);
        int activePlayerCount = (Integer)waitingPlayers.get(DBHelper.COUNT_KEY);

        ArrayList<DBObject> activePlayers = new ArrayList<DBObject>();
        for (int i = 1; i <= activePlayerCount; i++) {
            DBObject activePlayer = new BasicDBObject(DBHelper.PLAYER_NUMBER_KEY, i);
            activePlayers.add(activePlayer);
        }

        DBCollection infoCollection = DBHelper.getInfoCollection();

        BasicDBObject info = new BasicDBObject(DBHelper.GAME_ID_KEY, myGameID);
        info.append(DBHelper.ACTIVE_PLAYERS_KEY, activePlayers);

        infoCollection.insert(info);
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
        DBObject map = DBHelper.getMapForGame(gameID);
        return map.toString();
    }

    public String getCurrentGameStateJson(String gameID) throws UnknownHostException{
        DBObject currentTurn = DBHelper.getCurrentTurnForGame(gameID);
        return currentTurn.toString();
    }

    public String getGameID(){
        return this.myGameID;
    }

    public Integer getWaitingPlayerCount() throws UnknownHostException{
        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();
        BasicDBObject query = new BasicDBObject(DBHelper.COUNT_KEY,  new BasicDBObject("$gt", 0));
        DBCursor cursor = waitingPlayers.find(query);

        if (!cursor.hasNext()) {
            return 0;
        }

        Integer count = ((Integer)(cursor.next().get(DBHelper.COUNT_KEY)));
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
        System.out.println("------- Remove called");
        //First update info collection
        //decrement game.state.activePlayerCount
        DBCollection infoCollection = DBHelper.getInfoCollection();
        DBObject info = DBHelper.getInfoForGame(myGameID);

        //info is null while the players are still waiting and the map hasn't yet been created
        if(info == null){
            DBCollection waitingPlayersCollection = DBHelper.getWaitingPlayersCollection();
            DBObject waitingPlayers = DBHelper.getWaitingPlayersForGame(myGameID);
            ArrayList<DBObject> players = (ArrayList<DBObject>)waitingPlayers.get(DBHelper.PLAYERS_KEY);
            players.remove((pid - 1));
            waitingPlayers.put(DBHelper.COUNT_KEY, players.size());

            BasicDBObject newWaitingPlayers = new BasicDBObject("$set", waitingPlayers);
            newWaitingPlayers.append("$set", new BasicDBObject(DBHelper.PLAYERS_KEY, players));
            BasicDBObject gameQuery = new BasicDBObject(DBHelper.GAME_ID_KEY, myGameID);
            waitingPlayersCollection.update(gameQuery, newWaitingPlayers);

            BasicDBObject newWaitingPlayers2 = new BasicDBObject("$set", waitingPlayers);
            newWaitingPlayers2.append("$set", new BasicDBObject(DBHelper.COUNT_KEY, players.size()));
            waitingPlayersCollection.update(gameQuery, newWaitingPlayers2);

            if (players.size() == 0) {
                DBHelper.reset(myGameID);
                System.out.println("----- Resetting game");
                return;
            }
        }

        //remove player from activePlayers
        ArrayList<DBObject> activePlayers = (ArrayList<DBObject>)info.get(DBHelper.ACTIVE_PLAYERS_KEY);
        ArrayList<DBObject> updatedActivePlayers = new ArrayList<DBObject>();
        for (DBObject activePlayer : activePlayers) {
            if ((Integer)activePlayer.get(DBHelper.PLAYER_NUMBER_KEY) != pid) {
                updatedActivePlayers.add(activePlayer);
            }
        }

        if (updatedActivePlayers.size() == 0) {
            DBHelper.reset(myGameID);
            System.out.println("----- Resetting game");
            return;
        }

        BasicDBObject newInfo = new BasicDBObject("$set", info);
        newInfo.append("$set", new BasicDBObject(DBHelper.ACTIVE_PLAYERS_KEY, updatedActivePlayers));
        BasicDBObject gameQuery = new BasicDBObject(DBHelper.GAME_ID_KEY, myGameID);
        infoCollection.update(gameQuery, newInfo);

        //Second update most recent state turn
        DBObject currentTurn = DBHelper.getCurrentTurnForGame(myGameID);

        // is null if no turns have yet been committed to state
        if (currentTurn == null) {
            return;
        }

        //Then set all troops in that player's territories to 0
        ArrayList<DBObject> territories = (ArrayList<DBObject>)currentTurn.get(DBHelper.TERRITORIES_KEY);
        for (DBObject territory : territories) {
            boolean isOwnedByPid = ((Integer)territory.get(DBHelper.OWNER_KEY)).equals(pid);
            if (isOwnedByPid) {
                territory.put(DBHelper.TROOPS_KEY, 0);
            }
        }

        BasicDBObject updatedCurrentTurn = new BasicDBObject("$set", currentTurn);
        updatedCurrentTurn.append("$set", new BasicDBObject(DBHelper.TERRITORIES_KEY, territories));
        DBCollection stateCollection = DBHelper.getStateCollection();
        stateCollection.update(gameQuery, updatedCurrentTurn);
    }
}