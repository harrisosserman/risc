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
        ArrayList<Attacker> attackers = new ArrayList<Attacker>();
       // for(Integer i=0; i<25; i++){
            System.out.println("test1");
          //  if(jsonObject.asJson().get(TERRITORIES).get(i.toString())!=null){
                System.out.println("test2");
                JsonNode territoryData = jsonObject.asJson().get(TERRITORIES).get(0).get("1");
                System.out.println(jsonObject.asJson().get(TERRITORIES).get(0).get("1").toString());
                System.out.println(territoryData.get(TROOPS).toString());
                String troops_str = territoryData.get(TROOPS).toString();
                int troops = Integer.parseInt(troops_str);
                Territory territory = new Territory(1, playerID, troops);
                territories.add(territory);
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

           // }
       // }
        
        Attacker a = attackers.get(0);
        int turn_ = a.getOwner();

        connection.closeConnection();

        return turn_;
	}



}