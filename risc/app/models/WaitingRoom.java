package models;

import java.util.*;
import com.mongodb.*;
import libraries.DBHelper;

public class WaitingRoom{
	public static final String STATE_KEY = "state";
	public static final String PLAYERS_KEY = "players";
	public static final String PLAYER_KEY = "player";
	public static final String READY_KEY = "ready";
	public static final String ID_KEY = "_id";
	public static final String GAME_ID_KEY = "gameID";

	private DBObject myInfo;
	private String myGameID;

	public WaitingRoom(){

	}

	public void createNewWaitingRoom(String username){
		username = username.toLowerCase();

		DBCollection infoCollection = DBHelper.getInfoCollection();

		BasicDBObject info = new BasicDBObject();
		
		info.append(STATE_KEY, 0);

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

	private BasicDBObject createPlayer(String username){
		BasicDBObject player = new BasicDBObject();
		player.append(PLAYER_KEY, username);
		player.append(READY_KEY, false);
		return player;
	}

	public void addPlayer(String username){
		DBCollection infoCollection = DBHelper.getInfoCollection();
		BasicDBObject player = createPlayer(username);
		DBHelper.addObjectToListAndUpdateCollection(myInfo, player, PLAYERS_KEY, infoCollection);

		myInfo = DBHelper.getInfoForGame(myGameID);
	}

	public void markPlayerAsReady(String username){
		String readyPath = PLAYERS_KEY + ".$." + READY_KEY;
		String namePath = PLAYERS_KEY + "." + PLAYER_KEY;

		BasicDBObject updatedReady = new BasicDBObject(readyPath, true);
		BasicDBObject updateCommand = new BasicDBObject("$set", updatedReady);

		ArrayList<DBObject> andList = new ArrayList<DBObject>();
		BasicDBObject playerQuery = new BasicDBObject(namePath, username);
		andList.add(playerQuery);
		BasicDBObject gameQuery = new BasicDBObject(GAME_ID_KEY, myGameID);
		andList.add(gameQuery);
		BasicDBObject andQuery = new BasicDBObject("$and", andList);

		DBCollection infoCollection = DBHelper.getInfoCollection();
		infoCollection.update(andQuery, updateCommand);

		myInfo = DBHelper.getInfoForGame(myGameID);
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