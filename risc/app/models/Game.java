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

        long millis = System.currentTimeMillis();
        state.append(DBHelper.TIMESTAMP, new Long(millis));

        ArrayList<DBObject> territories = new ArrayList<DBObject>();
        for (int i = 0; i < countryOwners.length; i++) {
            BasicDBObject territory = new BasicDBObject();

            String countryOwner = usernames.get(countryOwners[i]);
            territory.append(DBHelper.OWNER_KEY, countryOwner);
            territory.append(DBHelper.POSITION_KEY, i);
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


            ArrayList<Integer> visibleTerritories = initialVisibleTerritories(countryOwners, usernames.indexOf(username));
            info.append(DBHelper.VISIBLE_TERRITORIES_KEY, visibleTerritories);
            
            ArrayList<DBObject> highestTech = initialHighestTech(username, usernames);
            info.append(DBHelper.HIGHEST_TECHNOLOGY_KEY, highestTech);

            playerInfo.add(info);
        }
        state.append(DBHelper.PLAYER_INFO_KEY, playerInfo);

        ArrayList<DBObject> spies = new ArrayList<DBObject>();
        state.append(DBHelper.SPIES_KEY, spies);

        return state;
    }

    private ArrayList<Integer> initialVisibleTerritories(int[] countryOwners, int owner){
        HashSet<Integer> visibleIndexSet = new HashSet<Integer>();
        AdjacencyMap adjacencyMap = new AdjacencyMap();
        for(int i = 0; i < countryOwners.length; i++){
            int ownerNumber = countryOwners[i];
            if (ownerNumber == owner) {
                visibleIndexSet.add(i);

                ArrayList<Integer> adjacencies = adjacencyMap.getAdjacencies(i);
                visibleIndexSet.addAll(adjacencies);
            }
        }
        return new ArrayList<Integer>(visibleIndexSet);
    }

    private ArrayList<DBObject> initialHighestTech(String username, ArrayList<String> usernames){
        ArrayList<DBObject> highestTech = new ArrayList<DBObject>();
        for (String user : usernames) {
            if(!user.equals(username)){
                BasicDBObject tech = new BasicDBObject();
                tech.append(DBHelper.OWNER_KEY, user);
                tech.append(DBHelper.LEVEL_KEY, 0);
                highestTech.add(tech);
            }
        }
        return highestTech;
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

    public String getCurrentGameStateJson(String username, ArrayList<String> usernames){        
        DBObject currentTurn = DBHelper.getCurrentTurnForGame(myGameID);
        DBObject filteredCurrentTurn = filterStateForUsername(currentTurn, username, usernames);
        return filteredCurrentTurn.toString();
    }

    private DBObject filterStateForUsername(DBObject currentTurn, String username, ArrayList<String> usernames){
        ArrayList<String> allies = new ArrayList<String>();
        allies.add(username);

        //Find main user's info
        ArrayList<DBObject> playerInfo = (ArrayList<DBObject>)currentTurn.get(DBHelper.PLAYER_INFO_KEY);
        DBObject targetPlayerInfo = null;
        ArrayList<Integer> territoriesVisible = null;
        for (DBObject info : playerInfo) {
            String owner = (String)info.get(DBHelper.OWNER_KEY);
            if (owner.equals(username)) {
                targetPlayerInfo = info;
                territoriesVisible = (ArrayList<Integer>)targetPlayerInfo.get(DBHelper.VISIBLE_TERRITORIES_KEY);
            }
        }

        //Populate allies
        if (targetPlayerInfo.containsKey(DBHelper.ALLIES_KEY)) {
            ArrayList<String> foreignAllies = (ArrayList<String>)targetPlayerInfo.get(DBHelper.ALLIES_KEY);
            for (String ally : foreignAllies) {
                allies.add(ally);
            }
        }
        System.out.println("Allies: " + allies);

        //Filter territories
        ArrayList<DBObject> filteredTerritories = new ArrayList<DBObject>();
        ArrayList<DBObject> territories = (ArrayList<DBObject>)currentTurn.get(DBHelper.TERRITORIES_KEY);
        for (DBObject territory : territories) {
            Integer position = (Integer)territory.get(DBHelper.POSITION_KEY);
            if (territoriesVisible.contains(position)) {
                filteredTerritories.add(territory);
            }
        }

        currentTurn.put(DBHelper.TERRITORIES_KEY, filteredTerritories);

        //Filter spies
        ArrayList<DBObject> filteredSpies = new ArrayList<DBObject>();
        ArrayList<DBObject> spies = (ArrayList<DBObject>)currentTurn.get(DBHelper.SPIES_KEY);
        if(spies != null){
            for (DBObject spy : spies) {
                String owner = (String)spy.get(DBHelper.OWNER_KEY);
                if (owner.equals(username)) {
                    filteredSpies.add(spy);
                }
            }

            currentTurn.put(DBHelper.SPIES_KEY, filteredSpies);
        }

        //Filter playerInfo
        ArrayList<DBObject> filteredPlayerInfo = new ArrayList<DBObject>();
        // int requestingPlayerNumber = usernames.indexOf(username);
        // ((BasicDBObject)targetPlayerInfo).append(DBHelper.PLAYER_NUMBER_KEY, requestingPlayerNumber);
        // filteredPlayerInfo.add(targetPlayerInfo);
        ArrayList<DBObject> highestTech = (ArrayList<DBObject>)targetPlayerInfo.get(DBHelper.HIGHEST_TECHNOLOGY_KEY);
        for (String user : usernames) {
            for (DBObject tech : highestTech) {
                String owner = (String)tech.get(DBHelper.OWNER_KEY);
                if (owner.equals(user)) {
                    int playerNumber = usernames.indexOf(owner);
                    int level = (Integer)tech.get(DBHelper.LEVEL_KEY);

                    BasicDBObject formattedTech = new BasicDBObject();
                    formattedTech.append(DBHelper.OWNER_KEY, owner);
                    formattedTech.append(DBHelper.PLAYER_NUMBER_KEY, playerNumber);
                    formattedTech.append(DBHelper.LEVEL_KEY, level);
                    filteredPlayerInfo.add(formattedTech);
                }else if (user.equals(username)){
                    int requestingPlayerNumber = usernames.indexOf(username);
                    ((BasicDBObject)targetPlayerInfo).append(DBHelper.PLAYER_NUMBER_KEY, requestingPlayerNumber);
                    filteredPlayerInfo.add(targetPlayerInfo);
                }
            }
        }
        // for (DBObject tech : highestTech) {
        //     String owner = (String)tech.get(DBHelper.OWNER_KEY);
        //     int playerNumber = usernames.indexOf(owner);
        //     int level = (Integer)tech.get(DBHelper.LEVEL_KEY);

        //     BasicDBObject formattedTech = new BasicDBObject();
        //     formattedTech.append(DBHelper.OWNER_KEY, owner);
        //     formattedTech.append(DBHelper.PLAYER_NUMBER_KEY, playerNumber);
        //     formattedTech.append(DBHelper.LEVEL_KEY, level);
        //     filteredPlayerInfo.add(formattedTech);
        // }

        currentTurn.put(DBHelper.PLAYER_INFO_KEY, filteredPlayerInfo);

        return currentTurn;
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