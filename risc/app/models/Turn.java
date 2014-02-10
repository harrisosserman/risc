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

    public String getGameID(RequestBody jsonObject) {
        myGameID = API.removeQuotes(jsonObject.asJson().get(GAME_ID).toString());
        return myGameID;
    }

    public int createTurn(RequestBody jsonObject) throws UnknownHostException{
        myGameID = API.removeQuotes(jsonObject.asJson().get(GAME_ID).toString());
        System.out.println(myGameID);
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
             System.out.println("auto adding 1 as turn");
        }
        else{

             highestTurn_value = (Integer) highestTurn.next().get(TURN);
        }
        BasicDBObject query_count = new BasicDBObject();
        query_count.put(GAME_ID, myGameID);
        System.out.println("myGameID is " + myGameID + " and is of type " + myGameID.getClass().getName());
        System.out.println("basic db object is " + query_count);
        DBObject playerCount = waitingPlayers.findOne(query_count);
        System.out.println("playerCount is: " + playerCount);
        Integer numPlayers = ((Integer)(playerCount.get(COUNT)));
        System.out.println("returned " );
        int numMaxTurns = 0;
        highestTurn = committedTurns.find().sort(new BasicDBObject(TURN, -1));
        while(highestTurn.hasNext()){
            DBObject obj = highestTurn.next();
            System.out.println("entered while loop " );
            int document_turn = (Integer) obj.get(TURN);
            if(highestTurn_value == document_turn){
                System.out.println("entered while loop  if statement :) " );
                numMaxTurns ++;
            }

        }
        System.out.println("numMaxTurns = " + numMaxTurns + "numPlayers = " + numPlayers);
        boolean full = (numMaxTurns == numPlayers);
        return full;
    }

    public int commitTurn() throws UnknownHostException{
        System.out.println("commit Turn started");
        DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
        DBCollection waitingPlayers = DBHelper.getWaitingPlayersCollection();
        DBCollection state = DBHelper.getStateCollection();
        System.out.println(myGameID);
        BasicDBObject query = new BasicDBObject();
        query.put(GAME_ID, myGameID);
        DBCursor states = state.find(query);
        System.out.println(states);
        if(!states.hasNext()){
            System.out.println("in if statement ");
            turn = 1;
        }
        else{
            System.out.println("in else statement ");
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



        System.out.println("turn was committed");

        return turn;


    }

}