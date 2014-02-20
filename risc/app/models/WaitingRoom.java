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
	}

	public static WaitingRoom getWaitingRoom(String gameID){
		DBObject info = DBHelper.getInfoForGame(gameID);
		WaitingRoom wr = new WaitingRoom();
		wr.setInfo(info);
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
	}

	public void markPlayerAsReady(String username){
		//TODO: Start here.
	}

	public void setInfo(DBObject info){
		myInfo = info;
	}

	public String toString(){
		return myInfo.toString();
	}
}