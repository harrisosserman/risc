package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.fasterxml.jackson.databind.*;
import com.mongodb.util.JSON;
import play.mvc.Http.RequestBody;
import java.net.UnknownHostException;
import models.Territory;
import models.Troop;


public class Turn {

	private static final int NUM_TERRITORIES = 25;
	private static final String INITIALIZATION_DB = "initialization";
    private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
    private static final String GAME_DB = "game";
    private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
    private static final String PLAYER = "player";
    private static final String ATTACKING = "attacking";
    private static final String TROOPS = "troops";
    private static final String TERRITORIES = "territories";
    private static final String TERRITORY = "territory";
    private static final String TURN = "turn";
    private static final String GAME_ID = "_id";
     private static final String POSITION = "position";
	
    private ArrayList<Territory> territories;
    private int playerID;
    private int id;
    private ArrayList<Attacker> attackers;


	public Turn(){
        attackers = new ArrayList<Attacker>();
        territories = new ArrayList<Territory>();
	}
    
    public int createTurn(RequestBody jsonObject) throws UnknownHostException{
		
        String gameID = jsonObject.asJson().get(GAME_ID).toString();
        id = Integer.parseInt(gameID);
        String _playerID = jsonObject.asJson().get(PLAYER).toString();
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
                    int attacker_number = Integer.parseInt(n.get(TROOPS).toString());
                    Attacker a = new Attacker(playerID, attacker_number, attacker_territory, position);
                    attackers.add(a);
                }    
            }
        }
       
        
        Attacker a = attackers.get(3);
        int turn_ = a.getStrength();

        commitTurn();

        return turn_;
	}

    public boolean validateTurn(){
        return false;
    }

    public void commitTurn() throws UnknownHostException{
        MongoConnection connection = new MongoConnection();
        DB game = connection.getDB(GAME_DB);
        DBCollection committedTurns = game.getCollection(COMMITTED_TURNS_COLLECTION);
        DB initialization_database = connection.getDB(INITIALIZATION_DB);
        DBCollection waitingPlayers = game.getCollection(WAITING_PLAYERS_COLLECTION);
        DBObject waitingPlayers_doc = initialization_database.waitingPlayers.findOne(new BasicDBObject().append(GAME_ID, id));
        //this might not work
        int turn_number = waitingPlayers_doc.get(TURN);
        BasicDBObject turn_doc = new BasicDBObject();
        turn_doc.append(GAME_ID, id);
        turn_doc.append(PLAYER, playerID);
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


        connection.closeConnection();

        return;


    }
        

}