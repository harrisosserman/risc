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
import models.*;
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
    private static final String NAME = "name";
    private static final String USERNAME = "username";
    private static final String ATTACKING = "attacking";
    private static final String TROOPS = "troops";
    private static final String STATE = "state";
    private static final String TERRITORIES = "territories";
    private static final String TERRITORY = "territory";
    private static final String TURN = "turn";
    private static final String GAME_ID = "gameID";
    private static final String POSITION = "position";
    private static final String COUNT = "count";
    private static final String FOOD = "food";
    private static final String TECHNOLOGY = "technology";
    private static final String TECHNOLOGY_LEVEL = "technology_level";
    private static final String MOVES = "moves";
    private static final String TROOPTYPE = "troopType";
    private static final String MOVETYPE = "moveType";
    private static final String START = "start";
    private static final String END = "end";
    private static final String UPGRADETYPE = "upgradeType";
    private static final String TIMESTAMP = "timeStamp";
    private static final String COMMITTED = "committed";
    private static final String PLAYERS = "players";

    private ArrayList<Territory> territories;
    private String playerID;
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

    public int createTurn(RequestBody jsonObject){
        myGameID = API.removeQuotes(jsonObject.asJson().get(GAME_ID).toString());
        String username = API.removeQuotes(jsonObject.asJson().get(USERNAME).toString());
        Player player_ = new Player(username);
        int food = Integer.parseInt(jsonObject.asJson().get(FOOD).toString());
        int technology = Integer.parseInt(jsonObject.asJson().get(TECHNOLOGY).toString());
        int technology_level = Integer.parseInt(jsonObject.asJson().get(TECHNOLOGY_LEVEL).toString());
        int committed = Integer.parseInt(jsonObject.asJson().get(COMMITTED).toString());
        Long timeStamp = Long.parseLong(jsonObject.asJson().get(TIMESTAMP).toString());
        player_.setFood(food);
        player_.setTechnology(technology);
        player_.setTechnologyLevel(technology_level);
        player_.setTurnCommitted(committed);
        player_.setTimeStamp(timeStamp);
        Integer nodes = jsonObject.asJson().get(MOVES).size();
        ArrayList<MoveType> moves = new ArrayList<MoveType>();
        Iterator<JsonNode> movesData = jsonObject.asJson().get(MOVES).elements();
        while(movesData.hasNext()){
                JsonNode moveData = movesData.next();
                int moveType = Integer.parseInt(moveData.get(MOVETYPE).toString());
                String troopType = API.removeQuotes(moveData.get(TROOPTYPE).toString());
                if(moveType == 0){
                    String upgradeType = API.removeQuotes(moveData.get(UPGRADETYPE).toString());
                    int position = Integer.parseInt(moveData.get(POSITION).toString());
                    Upgrade newMove = new Upgrade(moveType, troopType, upgradeType, position);
                    moves.add(newMove);
                }
                else if(moveType == 1){
                    int start = Integer.parseInt(moveData.get(START).toString());
                    int end = Integer.parseInt(moveData.get(END).toString());
                    Move newMove = new Move(moveType, troopType, start, end);
                    moves.add(newMove);
                }
                else if(moveType == 2){
                    int start = Integer.parseInt(moveData.get(START).toString());
                    int end = Integer.parseInt(moveData.get(END).toString());
                    Attack newMove = new Attack(moveType, troopType, start, end);
                    moves.add(newMove);
                }
                else if(moveType == 3){
                    int position = Integer.parseInt(moveData.get(POSITION).toString());
                    Place newMove = new Place(moveType, troopType, position);
                    moves.add(newMove);
                }
                else if(moveType == 4){
                    
                }
                
        }
        int result = commitTurn(moves, player_);

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



    // fix this to sort by time stamp and get 0 or 1. 
    public boolean allTurnsCommitted(){
        DBObject gameInfoObject = DBHelper.getInfoForGame(myGameID);
        BasicDBList players = (BasicDBList) gameInfoObject.get(PLAYERS);
        BasicDBObject[] playersArray = players.toArray(new BasicDBObject[0]);
        DBObject currentTurn = DBHelper.getCurrentTurnForGame(myGameID);
        int highestTurn_value = Integer.parseInt(currentTurn.get(TURN).toString());
    //    System.out.println("highest turn is " + highestTurn_value);
        
        for(BasicDBObject play : playersArray){
  //          System.out.println("entered player array " + play.get(NAME));
            DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
            String username = play.get(NAME).toString();
            BasicDBObject isPlayerCommitted = new BasicDBObject(GAME_ID, myGameID);
            isPlayerCommitted.put(TURN, highestTurn_value + 1);
            isPlayerCommitted.put(USERNAME, username);
            DBCursor lastCommit = committedTurns.find(isPlayerCommitted).sort(new BasicDBObject(TIMESTAMP, -1));
            if(!lastCommit.hasNext()){
         //       System.out.println("last commit does nt have next" + username);
                return false;
            }
            DBObject playerInfo = lastCommit.next();
            int committed = Integer.parseInt(playerInfo.get(COMMITTED).toString());
            if(committed == 0){
       //         System.out.println("didnt commit?");
                return false;
            }
        }
     //   System.out.println("test passed");
        return true;
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

    public int commitTurn(ArrayList<MoveType> moves, Player player1){
        DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
        DBCursor states = DBHelper.getStateCursorForGame(myGameID);
        if(!states.hasNext()){
            turn = 1;
        }
        else{
            DBObject highestTurn = DBHelper.getCurrentTurnForGame(myGameID);
            turn = (Integer) highestTurn.get(TURN);
            turn ++;
        }
        BasicDBObject turn_doc = new BasicDBObject();
        turn_doc.append(GAME_ID, myGameID);
        turn_doc.append(USERNAME, player1.getName());
        turn_doc.append(TURN, turn);
        turn_doc.append(FOOD, player1.getFood());
        turn_doc.append(COMMITTED, player1.getTurnCommitted());
        turn_doc.append(TECHNOLOGY, player1.getTechnology());
        turn_doc.append(TECHNOLOGY_LEVEL, player1.getTechnologyLevel());
        turn_doc.append(TIMESTAMP, player1.getTimeStamp());
        List<BasicDBObject> move_list = new ArrayList<BasicDBObject>();
        for(int i=0; i<moves.size(); i++){
         //   System.out.println("in loop of move size " + i);
            BasicDBObject move_doc = new BasicDBObject();
            move_doc.append(MOVETYPE, moves.get(i).getMoveType());
            move_doc.append(TROOPTYPE, moves.get(i).getTroopType());
            if(moves.get(i).getMoveType() == 0){
                Upgrade upgrade = (Upgrade) moves.get(i);
                move_doc.append(UPGRADETYPE, upgrade.getUpgradeType());
                move_doc.append(POSITION, upgrade.getPosition());
                move_list.add(move_doc);
            }
            else if(moves.get(i).getMoveType() == 1){
                Move move = (Move) moves.get(i);
                move_doc.append(START, move.getStart());
                move_doc.append(END, move.getEnd());
                move_list.add(move_doc);
            }
            else if(moves.get(i).getMoveType() == 2){
                Attack attack = (Attack) moves.get(i);
                move_doc.append(START, attack.getStart());
                move_doc.append(END, attack.getEnd());
                move_list.add(move_doc);
            }
            else if(moves.get(i).getMoveType() == 3){
                Place place = (Place) moves.get(i);
                move_doc.append(POSITION, place.getPosition());
                move_list.add(move_doc);
            }
            
        }

        turn_doc.append(MOVES, move_list);

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
