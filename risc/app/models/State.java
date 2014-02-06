package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import models.Territory;
import models.Troop;
import models.Attacker;

public class State{
 
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

	private int	turn; 
	private String myGameID;
	private List<Territory> territories;
	private List<Attacker> attackers;


public State(){
	territories = new ArrayList<Territory>();
	attackers = new ArrayList<Attacker>();
}	

public int assembleState() throws UnknownHostException{
	 MongoConnection connection = new MongoConnection();
     DB game = connection.getDB(GAME_DB);
     DBCollection committedTurns = game.getCollection(COMMITTED_TURNS_COLLECTION);
     ArrayList<BasicDBObject> turns = new ArrayList<BasicDBObject>();
     turns = game.committedTurns.find({turn: 2});
     System.out.println(turns);
     return 0;
}

