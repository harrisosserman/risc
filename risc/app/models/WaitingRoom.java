package models;

import java.util.*;
import com.mongodb.*;
import com.mongodb.util.JSON;
import libraries.DBHelper;

public class WaitingRoom{
	public static final String STATE_KEY = "state";
	public static final String PLAYERS_KEY = "players";
	public static final String PLAYER_KEY = "name";
	public static final String READY_KEY = "ready";
	public static final String ID_KEY = "_id";
	public static final String GAME_ID_KEY = "gameID";
	public static final int MAX_PLAYERS_PER_GAME = 5;
	public static final int MIN_PLAYERS_PER_GAME = 2;
	public static final int GAME_STATE_NOT_YET_STARTED = 0;
	public static final int GAME_STATE_STARTED = 1;
	public static final int GAME_STATE_FINISHED = 2;

	private DBObject myInfo;
	private String myGameID;

	public WaitingRoom(){

	}

	public void createNewWaitingRoom(String username){
		username = username.toLowerCase();

		DBCollection infoCollection = DBHelper.getInfoCollection();

		BasicDBObject info = new BasicDBObject();

		info.append(STATE_KEY, GAME_STATE_NOT_YET_STARTED);

		ArrayList<DBObject> players = new ArrayList<DBObject>();
		BasicDBObject firstPlayer = createPlayer(username);
		players.add(firstPlayer);
		info.append(PLAYERS_KEY, players);

		infoCollection.insert(info);

		DBObject infoWithID = infoCollection.findOne(info);
		String id = infoWithID.get(ID_KEY).toString();

		BasicDBObject gameID = new BasicDBObject(GAME_ID_KEY, id);
		BasicDBObject updateCommand = new BasicDBObject("$set", gameID);
		infoCollection.update(info, updateCommand);

		myInfo = infoCollection.findOne(infoWithID);
		myGameID = id;
	}

	public static WaitingRoom getWaitingRoom(String gameID){
		DBObject info = DBHelper.getInfoForGame(gameID);
		WaitingRoom wr = new WaitingRoom();
		wr.setInfo(info);
		wr.setGameID(gameID);
		return wr;
	}

	public static String getJoinableWaitingRoomsJson(){
		DBCollection infoCollection = DBHelper.getInfoCollection();

		BasicDBObject stateQuery = new BasicDBObject(STATE_KEY, GAME_STATE_NOT_YET_STARTED);
		BasicDBObject playersLengthQuery = new BasicDBObject("$where", "this." + PLAYERS_KEY + ".length < " + MAX_PLAYERS_PER_GAME);
		BasicDBObject andQuery = DBHelper.andQueriesTogether(stateQuery, playersLengthQuery);

		DBCursor joinableGamesCursor = infoCollection.find(andQuery);
		String joinableGamesJson = JSON.serialize(joinableGamesCursor).toString();
		return joinableGamesJson;
	}

	public static ArrayList<String> getGamesInProgress(){
		DBCollection infoCollection = DBHelper.getInfoCollection();
		BasicDBObject stateQuery = new BasicDBObject(STATE_KEY, GAME_STATE_STARTED);
		DBCursor startedGamesCursor = infoCollection.find(stateQuery);

		ArrayList<String> startedGameIDs = new ArrayList<String>();
		while(startedGamesCursor.hasNext()){
			DBObject startedGameInfo = startedGamesCursor.next();
			String gameID = (String)startedGameInfo.get(GAME_ID_KEY);
			startedGameIDs.add(gameID);
		}

		return startedGameIDs;
	}

	private BasicDBObject createPlayer(String username){
		BasicDBObject player = new BasicDBObject();
		player.append(PLAYER_KEY, username);
		player.append(READY_KEY, false);
		return player;
	}

	public void addPlayer(String username){
		username = username.toLowerCase();

		boolean isUserAlreadyInGame = false;
		ArrayList<DBObject> players = (ArrayList<DBObject>)myInfo.get(PLAYERS_KEY);
		for (DBObject player : players) {
			if (player.get(PLAYER_KEY).toString().equals(username)) {
				isUserAlreadyInGame = true;
				break;
			}
		}

		if (!isUserAlreadyInGame) {
			DBCollection infoCollection = DBHelper.getInfoCollection();
			BasicDBObject player = createPlayer(username);
			DBHelper.addObjectToListAndUpdateCollection(myInfo, player, PLAYERS_KEY, infoCollection);

			myInfo = DBHelper.getInfoForGame(myGameID);
		}
	}

	public void markPlayerAsReady(String username){
		username = username.toLowerCase();

		String readyPath = PLAYERS_KEY + ".$." + READY_KEY;
		String namePath = PLAYERS_KEY + "." + PLAYER_KEY;

		BasicDBObject updatedReady = new BasicDBObject(readyPath, true);
		BasicDBObject updateCommand = new BasicDBObject("$set", updatedReady);

		BasicDBObject playerQuery = new BasicDBObject(namePath, username);
		BasicDBObject gameQuery = new BasicDBObject(GAME_ID_KEY, myGameID);
		BasicDBObject andQuery = DBHelper.andQueriesTogether(playerQuery, gameQuery);

		DBCollection infoCollection = DBHelper.getInfoCollection();
		infoCollection.update(andQuery, updateCommand);

		myInfo = DBHelper.getInfoForGame(myGameID);
	}

	public boolean shouldGameBegin(){
		boolean areThereEnoughPlayers = (getNumberOfPlayers() >= MIN_PLAYERS_PER_GAME);
        boolean isEveryoneReady = (getNumberOfReadyPlayers() == getNumberOfPlayers());
        return (areThereEnoughPlayers && isEveryoneReady);
	}

	//This must be called after the game has been created for the first time.
	//Ensures that no other players can join the game.
	public void markRoomAsNotJoinable(){
		updateGameInfoState(GAME_STATE_STARTED);
	}

	public void markRoomsGameAsEnded(){
		updateGameInfoState(GAME_STATE_FINISHED);
	}

	private void updateGameInfoState(int gameState){
		BasicDBObject updatedGameState = new BasicDBObject(STATE_KEY, gameState);
		BasicDBObject updateCommand = new BasicDBObject("$set", updatedGameState);

		BasicDBObject gameQuery = new BasicDBObject(GAME_ID_KEY, myGameID);

		DBCollection infoCollection = DBHelper.getInfoCollection();
		infoCollection.update(gameQuery, updateCommand);

		myInfo = DBHelper.getInfoForGame(myGameID);
	}

	public int getNumberOfPlayers(){
		ArrayList<DBObject> players = (ArrayList<DBObject>)myInfo.get(PLAYERS_KEY);
		return players.size();
	}

	public ArrayList<String> getUsernames(){
		ArrayList<DBObject> players = (ArrayList<DBObject>)myInfo.get(PLAYERS_KEY);
		ArrayList<String> usernames = new ArrayList<String>();
		for (DBObject player : players) {
			String username = (String)player.get(PLAYER_KEY);
			usernames.add(username);
		}
		return usernames;
	}

	private int getNumberOfReadyPlayers(){
		ArrayList<DBObject> players = (ArrayList<DBObject>)myInfo.get(PLAYERS_KEY);
		int readyCount = 0;
		for (DBObject player : players) {
			boolean isReady = (Boolean)player.get(READY_KEY);
			if (isReady) {
				readyCount++;
			}
		}
		return readyCount;
	}

	public void setInfo(DBObject info){
		myInfo = info;
	}

	public void setGameID(String gameID){
		myGameID = gameID;
	}

	public String getGameID(){
		return myGameID;
	}

	public String toString(){
		if (myInfo != null) {
			return myInfo.toString();
		}else{
			return "myInfo is null";
		}
	}
}