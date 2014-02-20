package libraries;

import java.util.*;
import com.mongodb.*;
import java.net.UnknownHostException;
import libraries.ConnectionManager;

public class DBHelper{

	private static final String GAME_DB = "game";
	private static final String INITIALIZATION_DB = "initialization";
	private static final String PLAYERS_DB = "players";

	private static final String STATE_COLLECTION = "state";
	private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
	private static final String MAP_COLLECTION = "map";
	private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
	private static final String INFO_COLLECTION = "info";
	private static final String PLAYER_COLLECTION = "player";

	public static final String GAME_ID_KEY = "gameID";
	public static final String NAME_KEY = "name";
    public static final String COUNT_KEY = "count";
    public static final String READY_KEY = "ready";
    public static final String PLAYERS_KEY = "players";
    public static final String NUM_PLAYERS_KEY = "numPlayers";
    public static final String TERRITORIES_KEY = "territories";
    public static final String OWNER_KEY = "owner";
    public static final String TROOPS_KEY = "troops";
    public static final String ADDITIONAL_TROOPS_KEY = "additionalTroops";
    public static final String TURN_KEY = "turn";
    public static final String ACTIVE_PLAYER_COUNT_KEY = "activePlayerCount";
    public static final String ACTIVE_PLAYERS_KEY = "activePlayers";
    public static final String PLAYER_NUMBER_KEY = "playerNumber";
    public static final String PLAYER_ID_KEY = "playerId";
    public static final String PLAYER_NAME_KEY = "name";

	private static MongoConnection myConnection;

	private static MongoConnection getConnection(){
		return ConnectionManager.getInstance().getConnection();
	}

	public static void reset(String gameID){
		MongoConnection connection = DBHelper.getConnection();

		BasicDBObject removalCriteria = new BasicDBObject(GAME_ID_KEY, gameID);

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

	//Get DBs

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

	public static DB getPlayersDB(){
		return DBHelper.getDB(PLAYERS_DB);
	}

	//Get Collections

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

	public static DBCollection getPlayerCollection(){
		return DBHelper.getCollection(PLAYERS_DB, PLAYER_COLLECTION);
	}

	//Get Objects

	public static DBObject getWaitingPlayersForGame(String gameID){
		DBCollection waitingPlayersCollection = DBHelper.getWaitingPlayersCollection();
		BasicDBObject gameQuery = new BasicDBObject(GAME_ID_KEY, gameID);
		return waitingPlayersCollection.findOne(gameQuery);
	}

	public static DBObject getMapForGame(String gameID){
		DBCollection mapsCollection = DBHelper.getMapCollection();
		BasicDBObject gameQuery = new BasicDBObject(GAME_ID_KEY, gameID);
		return mapsCollection.findOne(gameQuery);
	}

	public static DBObject getInfoForGame(String gameID){
		DBCollection infoCollection = DBHelper.getInfoCollection();
		BasicDBObject gameQuery = new BasicDBObject(GAME_ID_KEY, gameID);
		return infoCollection.findOne(gameQuery);
	}

	public static DBObject getCurrentTurnForGame(String gameID){
		DBCursor stateCursor = DBHelper.getStateCursorForGame(gameID);
		DBCursor highestTurnCursor = stateCursor.sort(new BasicDBObject(DBHelper.TURN_KEY, -1));
		return highestTurnCursor.next();
	}

	public static DBObject getPlayer(String name){
		DBCollection playersCollection = DBHelper.getPlayerCollection();
		BasicDBObject playerQuery = new BasicDBObject(PLAYER_NAME_KEY, name);
		return playersCollection.findOne(playerQuery);
	}

	//Get Cursors

	public static DBCursor getStateCursorForGame(String gameID){
		DBCollection stateCollection = DBHelper.getStateCollection();
		BasicDBObject gameQuery = new BasicDBObject(GAME_ID_KEY, gameID);
		return stateCollection.find(gameQuery);
	}
}