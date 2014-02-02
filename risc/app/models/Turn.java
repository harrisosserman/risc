package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
import play.mvc.Http.RequestBody;
import java.net.UnknownHostException;


public class Turn {
	private static final String DEFAULT_GAME_ID = "12345";
	private static final int NUM_TERRITORIES = 50;
	private static final String GAME_DB = "game";
    private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
    private static final String PLAYER = "player";
    private static final String ATTACKING = "attacking";
    private static final String TROOPS = "troops";
    private static final String TERRITORIES = "territories";
    private static final String TURN = "turn";
    private static final String GAME_ID = "_id";
	
	public Turn(){

	}
public String createTurn(RequestBody jsonObject) throws UnknownHostException{
		MongoConnection connection = new MongoConnection();
        DB game = connection.getDB(GAME_DB);
        DBCollection committedTurns = game.getCollection(COMMITTED_TURNS_COLLECTION);
        //verify data
        String gameID = jsonObject.asJson().get(GAME_ID).toString();
        int ID = Integer.parseInt(gameID);
        String turnData = jsonObject.asJson().get(TURN).toString();

        //Integer.parseInt()
        BasicDBObject doc = new BasicDBObject();
        ArrayList<BasicDBObject> territoryList = new ArrayList<BasicDBObject>();

        connection.closeConnection();

        return turnData;
	}



}