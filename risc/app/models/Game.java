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
    private Territory[] myTerritories;

    public Game(){
        myGameID = DEFAULT_GAME_ID;
    }

    public Game(String gameID){
        myGameID = gameID;

        DBCursor stateCursor = DBHelper.getStateCursorForGame(myGameID);
        if (!stateCursor.hasNext()) {
            //TODO: Create game

            //Divy territories between players

            //Divy 2*TOTAL_TROOP_COUNT food resource gens fairly among territories
            //and store in game.info

            //make game map and store in game.state
        }
    }

    private DBCursor getStateCursor(){
        DBCursor stateCursor = DBHelper.getStateCursorForGame(myGameID);
        return stateCursor;
    }

    private DBObject getMostRecentTurn(){
        DBObject mostRecentTurn = DBHelper.getCurrentTurnForGame(myGameID);
        return  mostRecentTurn;
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
        return myGameID;
    }

    public Integer getWaitingPlayerCount() throws UnknownHostException{
        DBObject waitingPlayers = DBHelper.getWaitingPlayersForGame(myGameID);
        if (waitingPlayers == null) {
            return 0;
        }else{
            Integer count = (Integer)waitingPlayers.get(DBHelper.COUNT_KEY);
            return count;
        }
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