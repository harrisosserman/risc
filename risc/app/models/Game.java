package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
import libraries.DBHelper;

public class Game {

    private static final int NUM_TERRITORIES = 25;
    private static final int TOTAL_TROOP_COUNT = 240;   //(2*3*4*5)*2
    private static final int TOTAL_FOOD_COUNT = TOTAL_TROOP_COUNT;
    private static final int TOTAL_TECHNOLOGY_COUNT = TOTAL_TROOP_COUNT / 2;

    private String myGameID;
    private Territory[] myTerritories;

    //used for getting an existing game
    public Game(String gameID){
        myGameID = gameID;
    }

    //used for creating a new game.
    public Game(String gameID, ArrayList<String> usernames){
        myGameID = gameID;

        DBCursor stateCursor = getStateCursor();
        if (!stateCursor.hasNext()) {
            int numPlayers = usernames.size();

            int[] countryOwners = assignCountryOwners(numPlayers);

            int[] foodProductions = assignResourceForTerritoryOwners(numPlayers, countryOwners, TOTAL_FOOD_COUNT);
            int[] techProductions = assignResourceForTerritoryOwners(numPlayers, countryOwners, TOTAL_TECHNOLOGY_COUNT);

            DBObject initialState = makeInitialState(usernames, countryOwners, foodProductions, techProductions);
            DBCollection stateCollection = DBHelper.getStateCollection();
            stateCollection.insert(initialState);
        }
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

    private int[] assignResourceForTerritoryOwners(int numPlayers, int[] countryOwners, int totalResourceCount){
        ArrayList<int[]> resourceArraysForPlayers = new ArrayList<int[]>();
        int resourcesPerPlayer = totalResourceCount / numPlayers;
        int numPlayersWithAdditionalTerritories = NUM_TERRITORIES % numPlayers;
        for (int i = 0; i < numPlayers; i++) {
            int numTerritories = (NUM_TERRITORIES / numPlayers) + ((i < numPlayersWithAdditionalTerritories) ? 1 : 0);
            int[] resourceArr = getRandomArray(numTerritories, resourcesPerPlayer);
            resourceArraysForPlayers.add(resourceArr);
        }

        int[] resourceAssignments = new int[countryOwners.length];
        int[] currentCountryIndexForPlayer = new int[numPlayers];
        for (int i = 0; i < countryOwners.length; i++) {
            int countryOwner = countryOwners[i];
            int[] resourceArr = resourceArraysForPlayers.get(countryOwner);
            resourceAssignments[i] = resourceArr[currentCountryIndexForPlayer[countryOwner]];
            currentCountryIndexForPlayer[countryOwner]++;
        }
        return resourceAssignments;
    }

    //Not very efficient. Only use for reasonably small numbers
    private int[] getRandomArray(int length, int sum){
        int[] array = new int[length];
        Random generator = new Random(); 
        for (int i = 0; i < sum; i++) {
            int index = generator.nextInt(length);
            array[index]++;
        }
        return array;
    }

    private DBObject makeInitialState(ArrayList<String> usernames, int[] countryOwners, int[] foodProductions, int[] techProductions){
        BasicDBObject state = new BasicDBObject();
        state.append(DBHelper.GAME_ID_KEY, myGameID);
        state.append(DBHelper.NUM_PLAYERS_KEY, usernames.size());
        state.append(DBHelper.TURN_KEY, 0);

        ArrayList<DBObject> territories = new ArrayList<DBObject>();
        for (int i = 0; i < countryOwners.length; i++) {
            BasicDBObject territory = new BasicDBObject();

            String countryOwner = usernames.get(countryOwners[i]);
            territory.append(DBHelper.OWNER_KEY, countryOwner);

            territory.append(DBHelper.FOOD_KEY, foodProductions[i]);
            territory.append(DBHelper.TECHNOLOGY_KEY, techProductions[i]);

            territories.add(territory);
        }
        state.append(DBHelper.TERRITORIES_KEY, territories);

        int foodPerPlayer = TOTAL_FOOD_COUNT / usernames.size();
        int techPerPlayer = TOTAL_TECHNOLOGY_COUNT / usernames.size();
        int infantryPerPlayer = TOTAL_TROOP_COUNT / usernames.size();
        ArrayList<DBObject> playerInfo = new ArrayList<DBObject>();
        for (String username : usernames) {
            BasicDBObject info = new BasicDBObject();
            info.append(DBHelper.OWNER_KEY, username);
            info.append(DBHelper.LEVEL_KEY, 0);
            info.append(DBHelper.FOOD_KEY, foodPerPlayer);
            info.append(DBHelper.TECHNOLOGY_KEY, techPerPlayer);
            info.append(DBHelper.ADDITIONAL_INFANTRY_KEY, infantryPerPlayer);

            playerInfo.add(info);
        }
        state.append(DBHelper.PLAYER_INFO_KEY, playerInfo);

        return state;
    }

    private DBCursor getStateCursor(){
        DBCursor stateCursor = DBHelper.getStateCursorForGame(myGameID);
        return stateCursor;
    }

    private DBObject getMostRecentTurn(){
        DBObject mostRecentTurn = DBHelper.getCurrentTurnForGame(myGameID);
        return  mostRecentTurn;
    }

    public boolean areAllPlayersCommitedForMostRecentTurn(){
        DBCollection committedTurnsCollection = DBHelper.getCommittedTurnsCollection();
        BasicDBObject committedTurnsQuery = new BasicDBObject(DBHelper.GAME_ID_KEY, myGameID);
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
        // if no players have committed yet for this turn, return true 
        // because every player is still committed for the last turn
        if(turn == -1) {
            return true;
        }

        DBCollection stateCollection = DBHelper.getStateCollection();
        BasicDBObject stateQuery = new BasicDBObject(DBHelper.GAME_ID_KEY, myGameID).append(DBHelper.TURN_KEY, turn);
        DBObject state = stateCollection.findOne(stateQuery);
        if(state == null) {
            return false;
        }

        return true;
    }

    public String getCurrentGameStateJson(){
        DBObject currentTurn = DBHelper.getCurrentTurnForGame(myGameID);
        return currentTurn.toString();
    }

    public String getGameID(){
        return myGameID;
    }

    public Integer getWaitingPlayerCount(){
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
       //     if (t.getOwner() == pid) {
         //       playersTerritories.add(t);
           // }
        }
        return playersTerritories;
    }

    public int territoryCountForPlayer(int pid){
        int count = 0;
        for (Territory t : myTerritories) {
     //       if (t.getOwner() == pid) {
       //         count++;
       //     }
        }
        return count;
    }
}