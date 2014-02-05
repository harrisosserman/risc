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

	private static final String DEFAULT_GAME_ID = "12345";
	private static final int NUM_TERRITORIES = 25;
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

	}
    
    public int createTurn(RequestBody jsonObject) throws UnknownHostException{
		System.out.println("test");
        MongoConnection connection = new MongoConnection();
        DB game = connection.getDB(GAME_DB);
        DBCollection committedTurns = game.getCollection(COMMITTED_TURNS_COLLECTION);
        territories = new ArrayList<Territory>();
        String gameID = jsonObject.asJson().get(GAME_ID).toString();
        id = Integer.parseInt(gameID);
        String _playerID = jsonObject.asJson().get(PLAYER).toString();
        playerID = Integer.parseInt(_playerID);
        ArrayList<Attacker> attackers = new ArrayList<Attacker>();
        for(Integer i=0; i<25; i++){
            if(jsonObject.asJson().get(TERRITORIES).get(i)!=null){
                System.out.println("test1");
                JsonNode territoryData = jsonObject.asJson().get(TERRITORIES).get(i);
                System.out.println(jsonObject.asJson().get(TERRITORIES).get(i).toString());
                System.out.println(territoryData.get(TROOPS).toString());
                String troops_str = territoryData.get(TROOPS).toString();
                int troops = Integer.parseInt(troops_str);
                String position_str = territoryData.get(POSITION).toString();
                int position = Integer.parseInt(position_str);
                Territory territory = new Territory(position, playerID, troops);
                territories.add(territory);
                System.out.println(territory.getOwner());
                System.out.println(territory.getPosition());
                Iterator<JsonNode> attackerData = territoryData.get(ATTACKING).elements();
                System.out.println(territoryData.get(ATTACKING).toString());
                while(attackerData.hasNext()){
                    System.out.println("test while is in ");
                    JsonNode n = attackerData.next();
                    int attacker_territory = Integer.parseInt(n.get(TERRITORY).toString());
                    int attacker_number = Integer.parseInt(n.get(TROOPS).toString());
                    Attacker a = new Attacker(playerID, attacker_number, attacker_territory);
                    attackers.add(a);
                }    
            }
        }
       
        
        Attacker a = attackers.get(3);
        int turn_ = a.getStrength();

        connection.closeConnection();

        return turn_;
	}



}