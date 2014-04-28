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
        myGameID = API.removeQuotes(jsonObject.asJson().get(Constants.GAME_ID).toString());
        String username = API.removeQuotes(jsonObject.asJson().get(Constants.USERNAME).toString());
        Player player_ = new Player(username);
        int food = Integer.parseInt(jsonObject.asJson().get(Constants.FOOD).toString());
        int technology = Integer.parseInt(jsonObject.asJson().get(Constants.TECHNOLOGY).toString());
        int technology_level = Integer.parseInt(jsonObject.asJson().get(Constants.TECHNOLOGY_LEVEL).toString());
        int committed = Integer.parseInt(jsonObject.asJson().get(Constants.COMMITTED).toString());
        Long timeStamp = Long.parseLong(jsonObject.asJson().get(Constants.TIMESTAMP).toString());
        player_.setFood(food);
        player_.setTechnology(technology);
        player_.setTechnologyLevel(technology_level);
        player_.setTurnCommitted(committed);
        player_.setTimeStamp(timeStamp);
        Integer nodes = jsonObject.asJson().get(Constants.MOVES).size();
        ArrayList<MoveType> moves = new ArrayList<MoveType>();
        Iterator<JsonNode> movesData = jsonObject.asJson().get(Constants.MOVES).elements();
        while(movesData.hasNext()){
                JsonNode moveData = movesData.next();
                int moveType = Integer.parseInt(moveData.get(Constants.MOVETYPE).toString());
                if(moveType == 0){
                    String troopType = API.removeQuotes(moveData.get(Constants.TROOPTYPE).toString());
                    String upgradeType = API.removeQuotes(moveData.get(Constants.UPGRADETYPE).toString());
                    int position = Integer.parseInt(moveData.get(Constants.POSITION).toString());
                    Upgrade newMove = new Upgrade(moveType, troopType, upgradeType, position);
                    moves.add(newMove);
                }
                else if(moveType == 1){
                    String troopType = API.removeQuotes(moveData.get(Constants.TROOPTYPE).toString());
                    int start = Integer.parseInt(moveData.get(Constants.START).toString());
                    int end = Integer.parseInt(moveData.get(Constants.END).toString());
                    Move newMove = new Move(moveType, troopType, start, end);
                    moves.add(newMove);
                }
                else if(moveType == 2){
                    String troopType = API.removeQuotes(moveData.get(Constants.TROOPTYPE).toString());
                    int start = Integer.parseInt(moveData.get(Constants.START).toString());
                    int end = Integer.parseInt(moveData.get(Constants.END).toString());
                    Attack newMove = new Attack(moveType, troopType, start, end);
                    moves.add(newMove);
                }
                else if(moveType == 3){
                    String troopType = API.removeQuotes(moveData.get(Constants.TROOPTYPE).toString());
                    int position = Integer.parseInt(moveData.get(Constants.POSITION).toString());
                    Place newMove = new Place(moveType, troopType, position);
                    moves.add(newMove);
                }
                else if(moveType == 4){
                    Iterator<JsonNode> tradeData_ = moveData.get(Constants.OFFER).elements();
                    JsonNode tradeData = tradeData_.next();
                    System.out.println("trade");
                    String giver_ = API.removeQuotes(tradeData.get(Constants.GIVER).toString());
                    System.out.println("trade");
                    String receiver_ = API.removeQuotes(tradeData.get(Constants.RECEIVER).toString());
                    System.out.println("trade");
                    int amount = Integer.parseInt(API.removeQuotes(tradeData.get(Constants.NUMBER).toString()));
                    System.out.println(amount);
                    System.out.println("trade");
                    String type = API.removeQuotes(tradeData.get(Constants.TYPE).toString());
                    System.out.println("trade");
                    Trade newMove = new Trade(moveType, giver_, receiver_, amount, type);
                    System.out.println("trade");
                    moves.add(newMove);
                    System.out.println("trade");
                }
                else if(moveType == 5){
                    String formString = moveData.get(Constants.FORMALLIANCE).toString();
                    boolean form = Boolean.parseBoolean(formString);
                    String owner = API.removeQuotes(moveData.get(Constants.OWNER).toString());
                    String ally = API.removeQuotes(moveData.get(Constants.ALLY).toString());
                    Allign newMove = new Allign(moveType, form, owner, ally);
                    moves.add(newMove);
                }
        }
        System.out.println("trade");
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
        BasicDBList players = (BasicDBList) gameInfoObject.get(Constants.PLAYERS);
        BasicDBObject[] playersArray = players.toArray(new BasicDBObject[0]);
        DBObject currentTurn = DBHelper.getCurrentTurnForGame(myGameID);
        int highestTurn_value = Integer.parseInt(currentTurn.get(Constants.TURN).toString());
        System.out.println("highest turn is " + highestTurn_value);
        
        for(BasicDBObject play : playersArray){
            System.out.println("entered player array " + play.get(Constants.NAME));
            DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
            String username = play.get(Constants.NAME).toString();
            BasicDBObject isPlayerCommitted = new BasicDBObject(Constants.GAME_ID, myGameID);
            isPlayerCommitted.put(Constants.TURN, highestTurn_value + 1);
            isPlayerCommitted.put(Constants.USERNAME, username);
            DBCursor lastCommit = committedTurns.find(isPlayerCommitted).sort(new BasicDBObject(Constants.TIMESTAMP, -1));
            if(!lastCommit.hasNext()){
                System.out.println("last commit does nt have next" + username);
                return false;
            }
            DBObject playerInfo = lastCommit.next();
            int committed = Integer.parseInt(playerInfo.get(Constants.COMMITTED).toString());
            if(committed == 0){
                System.out.println("didnt commit?");
                return false;
            }
        }
        System.out.println("test passed");
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
            turn = (Integer) highestTurn.get(Constants.TURN);
            turn ++;
        }
        BasicDBObject turn_doc = new BasicDBObject();
        turn_doc.append(Constants.GAME_ID, myGameID);
        turn_doc.append(Constants.USERNAME, player1.getName());
        turn_doc.append(Constants.TURN, turn);
        turn_doc.append(Constants.FOOD, player1.getFood());
        turn_doc.append(Constants.COMMITTED, player1.getTurnCommitted());
        turn_doc.append(Constants.TECHNOLOGY, player1.getTechnology());
        turn_doc.append(Constants.TECHNOLOGY_LEVEL, player1.getTechnologyLevel());
        turn_doc.append(Constants.TIMESTAMP, player1.getTimeStamp());
        List<BasicDBObject> move_list = new ArrayList<BasicDBObject>();
        for(int i=0; i<moves.size(); i++){
         //   System.out.println("in loop of move size " + i);
            BasicDBObject move_doc = new BasicDBObject();
            move_doc.append(Constants.MOVETYPE, moves.get(i).getMoveType());
            if(moves.get(i).getMoveType() == 0){
                Upgrade upgrade = (Upgrade) moves.get(i);
                move_doc.append(Constants.TROOPTYPE, upgrade.getTroopType());
                move_doc.append(Constants.UPGRADETYPE, upgrade.getUpgradeType());
                move_doc.append(Constants.POSITION, upgrade.getPosition());
                move_list.add(move_doc);
            }
            else if(moves.get(i).getMoveType() == 1){
                Move move = (Move) moves.get(i);
                move_doc.append(Constants.TROOPTYPE, move.getTroopType());
                move_doc.append(Constants.START, move.getStart());
                move_doc.append(Constants.END, move.getEnd());
                move_list.add(move_doc);
            }
            else if(moves.get(i).getMoveType() == 2){
                Attack attack = (Attack) moves.get(i);
                move_doc.append(Constants.TROOPTYPE, attack.getTroopType());                
                move_doc.append(Constants.START, attack.getStart());
                move_doc.append(Constants.END, attack.getEnd());
                move_list.add(move_doc);
            }
            else if(moves.get(i).getMoveType() == 3){
                Place place = (Place) moves.get(i);
                move_doc.append(Constants.TROOPTYPE, place.getTroopType());                
                move_doc.append(Constants.POSITION, place.getPosition());
                move_list.add(move_doc);
            }
            else if(moves.get(i).getMoveType() == 5){
                Allign ally = (Allign) moves.get(i);
                move_doc.append(Constants.FORMALLIANCE, ally.forming());
                move_doc.append(Constants.OWNER, ally.getOwner());
                move_doc.append(Constants.ALLY, ally.getAlly());
                move_list.add(move_doc);
            }

        }

            BasicDBObject move_doc = new BasicDBObject();
            move_doc.append(Constants.MOVETYPE, 4);
            List<BasicDBObject> trade_list = new ArrayList<BasicDBObject>();
            boolean isThereATrade = false;
        for(int i=0; i<moves.size(); i++){
            System.out.println("bottom");
            BasicDBObject trade_doc = new BasicDBObject();
            if(moves.get(i).getMoveType() == 4){
                isThereATrade = true;
                            System.out.println("bottom");
                Trade trade = (Trade) moves.get(i);
                trade_doc.append(Constants.GIVER, trade.getGiver());
                            System.out.println("bottom");
                trade_doc.append(Constants.RECEIVER, trade.getReceiver());
                trade_doc.append(Constants.TYPE, trade.getTradeType().toString());
                            System.out.println("bottom");
                trade_doc.append(Constants.NUMBER, trade.getAmount());
                trade_list.add(trade_doc);
                        System.out.println("bottom");
            }
        }
        if(isThereATrade){
                                     System.out.println("bottom");

            move_doc.append(Constants.OFFER, trade_list);
                            System.out.println("bottom");

            move_list.add(move_doc);
        }
                            System.out.println("bottom");

        
        turn_doc.append(Constants.MOVES, move_list);
                            System.out.println("bottom");

        committedTurns.insert(turn_doc);
                            System.out.println("bottom");

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
        myGameID = API.removeQuotes(jsonObject.asJson().get(Constants.GAME_ID).toString());
        return myGameID;
    }
}
