package libraries;

import java.util.*;
import com.mongodb.*;
import java.net.UnknownHostException;
import libraries.ConnectionManager;

public class DBHelper{

	private static final String GAME_DB = "game";
	private static final String INITIALIZATION_DB = "initialization";
	private static final String STATE_COLLECTION = "state";
	private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
	private static final String MAP_COLLECTION = "map";
	private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
	private static final String INFO_COLLECTION = "info";

	private static final String GAME_ID = "gameID";

	private static MongoConnection myConnection;

	private static MongoConnection getConnection(){
		return ConnectionManager.getInstance().getConnection();
	}

	public static void reset(String gameID){
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

		DBCollection infoCollection = gameDB.getCollection(INFO_COLLECTION);
		infoCollection.remove(removalCriteria);
	}

	private static DB getDB(String dbName){
		MongoConnection connection = DBHelper.getConnection();
		DB db = connection.getDB(dbName);
		return db;
	}

	public static DB getInitializationDB(){
		return DBHelper.getDB(INITIALIZATION_DB);
	}

	public static DB getGameDB(){
		return DBHelper.getDB(GAME_DB);
	}

	private static DBCollection getCollection(String dbName, String collectionName){
		MongoConnection connection = DBHelper.getConnection();
		DBCollection collection = connection.getDB(dbName).getCollection(collectionName);
		return collection;
	}

	public static DBCollection getWaitingPlayersCollection(){
		return DBHelper.getCollection(INITIALIZATION_DB, WAITING_PLAYERS_COLLECTION);
	}

	public static DBCollection getMapCollection(){
		return DBHelper.getCollection(GAME_DB, MAP_COLLECTION);
	}

	public static DBCollection getCommittedTurnsCollection(){
		return DBHelper.getCollection(GAME_DB, COMMITTED_TURNS_COLLECTION);
	}

	public static DBCollection getStateCollection(){
		return DBHelper.getCollection(GAME_DB, STATE_COLLECTION);
	}

	public static DBCollection getInfoCollection(){
		return DBHelper.getCollection(GAME_DB, INFO_COLLECTION);
	}

}