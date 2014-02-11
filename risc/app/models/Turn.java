package models;

import java.util.*;
import com.mongodb.*;
import com.fasterxml.jackson.databind.*;
import com.mongodb.util.JSON;
import play.mvc.Http.RequestBody;
import java.net.UnknownHostException;
import models.Territory;
import models.Troop;
import controllers.API;
import libraries.DBHelper;

/* 
 * The turn model is created every time a POST API call to commitTurn
 * happens. It reads in from the RequestBody, as a JSON obejct, and 
 * parses the data into data structures, validates the turn (not yet), 
 * checks if its the final turn, and stores the turn to the committedTurns
 * collection in the game database. 
 *
 * The private data structures below are used for the entire turn and are 
 * edited by all the methods.
 *
 * 
 */

public class Turn {

	private static final int NUM_TERRITORIES = 25;
	private static final String INITIALIZATION_DB = "initialization";
    private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
    private static final String GAME_DB = "game";
    private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
    private static final String PLAYER = "player";
    private static final String ATTACKING = "attacking";
    private static final String TROOPS = "troops";
    private static final String STATE = "state";
    private static final String TERRITORIES = "territories";
    private static final String TERRITORY = "territory";
    private static final String TURN = "turn";
    private static final String GAME_ID = "gameID";
    private static final String POSITION = "position";
    private static final String COUNT = "count";

    private ArrayList<Territory> territories;
    private int playerID;
    private String myGameID;
    private ArrayList<Attacker> attackers;
    private int turn;


	public Turn(){
        attackers = new ArrayList<Attacker>();
        territories = new ArrayList<Territory>();
	}



    /* Create turn is called directly from the API call and is responsible
     * for manually parsing the data into the data structures the model
     * contains. This is necessary for data validation.  
     * 
     * Territories are looped through and data is extracted and added to 
     * an array list of territories. Each Territory has an ArrayList of attackers
     * that is updated as well. 
     * 
     * Other data extracted and stored includes gameID, and playerID.
     *
     * Create Turn calls committTurn before finishing. 
     *
     * @returns int of the turn number that was just committed (for debugging).
     *
     * @throwsUnknownHostException Thrown to indicate that the IP address of a 
     * host could not be determined.
     *
     * @param RequestBody is the text that comes from the API POST Call, 
     * and is treated like a JSON Object for parsing. 
     */

    public int createTurn(RequestBody jsonObject) throws UnknownHostException{
        myGameID = API.removeQuotes(jsonObject.asJson().get(GAME_ID).toString());
        String _playerID = (String)jsonObject.asJson().get(PLAYER).toString();
        playerID = Integer.parseInt(_playerID);

        for(Integer i=0; i<25; i++){
            if(jsonObject.asJson().get(TERRITORIES).get(i)!=null){
                JsonNode territoryData = jsonObject.asJson().get(TERRITORIES).get(i);
                String troops_str = territoryData.get(TROOPS).toString();
                int troops = Integer.parseInt(troops_str);
                String position_str = territoryData.get(POSITION).toString();
                int position = Integer.parseInt(position_str);
                Territory territory = new Territory(position, playerID, troops);
                territories.add(territory);
                Iterator<JsonNode> attackerData = territoryData.get(ATTACKING).elements();
                while(attackerData.hasNext()){
                    JsonNode n = attackerData.next();
                    int attacker_territory = Integer.parseInt(n.get(TERRITORY).toString());
                   /// change this to a front end change soon!!!
                    if(!n.get(TROOPS).toString().equals("null")){
                    int attacker_number = Integer.parseInt(n.get(TROOPS).toString());
                    Attacker a = new Attacker(playerID, attacker_number, attacker_territory, position);
                    attackers.add(a);
                    } 
                }
            }
        }


        int result = commitTurn();

        return result;
	}

    /*
     * This is a method called from every committedTurns API call, and it checks after
     * each turn is committed if that was the final player to commit the turn. 
     *
     * Turns are checked by querying the committedTurns database for the number of 
     * documents with the current game ID and turn ID, and if it is equal to the number
     * of players in the game.
     *
     *
     *
     * When the method returns true, the state model is instantiated and calculated 
     * because all of the data is now in the database. 
     *
     * @throws UnknownHostException Thrown to indicate that the IP address of a host could not 
     * be determined.
     * 
     * @return boolean determining if all of the turns have been committed. 
     */

    public boolean allTurnsCommitted() throws UnknownHostException{
        DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
        DBCollection state = DBHelper.getStateCollection();
        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();
        BasicDBObject query_turn = new BasicDBObject(GAME_ID, myGameID);
        DBCursor highestTurn = committedTurns.find().sort(new BasicDBObject(TURN, -1));
        int highestTurn_value;
        if(!highestTurn.hasNext())
        {
             highestTurn_value = 1;
        }
        else{

             highestTurn_value = (Integer) highestTurn.next().get(TURN);
        }
        BasicDBObject query_count = new BasicDBObject();
        query_count.put(GAME_ID, myGameID);
        DBObject playerCount = waitingPlayers.findOne(query_count);
        Integer numPlayers = ((Integer)(playerCount.get(COUNT)));
        int numMaxTurns = 0;
        highestTurn = committedTurns.find().sort(new BasicDBObject(TURN, -1));
        while(highestTurn.hasNext()){
            DBObject obj = highestTurn.next();
            int document_turn = (Integer) obj.get(TURN);
            if(highestTurn_value == document_turn){
                numMaxTurns ++;
            }

        }
        boolean full = (numMaxTurns == numPlayers);
        return full;
    }

    /*
     * commitTurn stores the Turn model that has been created down to the database
     * so it can be accessed by the state later. 
     * 
     * commitTurn is called by the createTurn method. In the future it will be called by 
     * validateTurn, and only if the turn is valid will it be committed. 
     *
     * @throws UnknownHostException Thrown to indicate that the IP address of a host could not 
     * be determined.
     *
     * @returns int of the turn number that was committed.
     * 
     */

    public int commitTurn() throws UnknownHostException{
        DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();
        BasicDBObject query = new BasicDBObject();
        query.put(GAME_ID, myGameID);
        DBCursor states = state.find(query);
        if(!states.hasNext()){
            turn = 1;
        }
        else{
            DBCursor highestTurn = state.find().sort( new BasicDBObject(TURN, -1));
            turn = (Integer) highestTurn.next().get(TURN);
            turn ++;
    }
        BasicDBObject turn_doc = new BasicDBObject();
        turn_doc.append(GAME_ID, myGameID);
        turn_doc.append(PLAYER, playerID);
        turn_doc.append(TURN, turn);
        List<BasicDBObject> territory_list = new ArrayList<BasicDBObject>();
        for(int i=0; i<territories.size(); i++){
            BasicDBObject territory_doc = new BasicDBObject();
            int position = territories.get(i).getPosition();
            territory_doc.append(POSITION, position);
            territory_doc.append(TROOPS, territories.get(i).getDefendingArmy());
            List<BasicDBObject> attacker_list = new ArrayList<BasicDBObject>();
            for(int j=0; j< attackers.size(); j++){
                if(attackers.get(j).getHome() == territories.get(i).getPosition()){
                    BasicDBObject attacker_doc = new BasicDBObject();
                    attacker_doc.append(TERRITORY, attackers.get(j).getTerritory());
                    attacker_doc.append(TROOPS, attackers.get(j).getStrength());
                    attacker_list.add(attacker_doc);

                }
            }
            territory_doc.append(ATTACKING, attacker_list);
            territory_list.add(territory_doc);
        }

        turn_doc.append(TERRITORIES, territory_list);

        committedTurns.insert(turn_doc);

        return turn;


    }

    /*
     * Helper method used to return the ID without repeating code in multiple places. 
     * 
     * @param RequestBody is the data from the front end and is treated as a JSON
     * object that is queried for the Game Id. 
     * 
     * @returns String of the gameId.
     */

    public String getGameID(RequestBody jsonObject) {
        myGameID = API.removeQuotes(jsonObject.asJson().get(GAME_ID).toString());
        return myGameID;
    }

}