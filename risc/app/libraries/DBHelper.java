package libraries;

import java.util.*;
import com.mongodb.*;
import java.net.UnknownHostException;

public class DBHelper{

	private static final String GAME_DB = "game";
	private static final String INITIALIZATION_DB = "initialization";
	private static final String STATE_COLLECTION = "state";
	private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
	private static final String MAP_COLLECTION = "map";
	private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";

	private static final String GAME_ID = "gameID";

	private static MongoConnection getConnection(){
		try{
			MongoConnection connection = new MongoConnection();
			return connection;
		}catch (UnknownHostException exception){
			System.out.println(exception.toString());
			return null;
		}
	}

	public static void reset(String gameID){
		System.out.println("reset dbs");
		MongoConnection connection = DBHelper.getConnection();

		BasicDBObject removalCriteria = new BasicDBObject(GAME_ID, gameID);

		DBCollection waitingPlayersCollection = connection.getDB(INITIALIZATION_DB).getCollection(WAITING_PLAYERS_COLLECTION);
		waitingPlayersCollection.remove(removalCriteria);

		DB gameDB = connection.getDB(GAME_DB);

		DBCollection mapCollection = gameDB.getCollection(MAP_COLLECTION);
		mapCollection.remove(removalCriteria);

		DBCollection committedTurnsCollection = gameDB.getCollection(COMMITTED_TURNS_COLLECTION);
		committedTurnsCollection.remove(removalCriteria);

		DBCollection stateCollection = gameDB.getCollection(STATE_COLLECTION);
		stateCollection.remove(removalCriteria);

		connection.closeConnection();
	}
}