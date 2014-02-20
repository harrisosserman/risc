package models;

import java.util.*;
import com.mongodb.*;
import libraries.DBHelper;

public class UserManager{
	public static final String NAME_KEY = "name";
	public static final String PASSWORD_KEY = "password";
	public static final String GAMES_KEY = "games";
	public static final String GAME_KEY = "game";

	public boolean createUser(String username, String password){
		username = username.toLowerCase();

		DBCollection playerCollection = DBHelper.getPlayerCollection();

		BasicDBObject usernameQuery = new BasicDBObject();
		usernameQuery.append(NAME_KEY, username);
		if (playerCollection.findOne(usernameQuery) == null) {
			BasicDBObject player = new BasicDBObject();
			player.append(NAME_KEY, username);
			player.append(PASSWORD_KEY, password);

			ArrayList<BasicDBObject> games = new ArrayList<BasicDBObject>();
			player.append(GAMES_KEY, games);

			playerCollection.insert(player);

			return true;
		}else{
			return false;
		}
	}

	private boolean doesUserExist(String username){
		DBObject player = DBHelper.getPlayer(username);
		return (player != null);
	}

	//Returns the player info without the password if it exists.
	//Returns null otherwise.
	public String getPublicPlayerInfoJson(String username){
		username = username.toLowerCase(); 

		if (doesUserExist(username)) {
			DBObject player = DBHelper.getPlayer(username);
			player.removeField(PASSWORD_KEY);
			return player.toString();
		}else{
			return null;
		}
	}

	public boolean doesUsernameMatchPassword(String username, String password){
		username = username.toLowerCase();

		if (doesUserExist(username)) {
			DBObject player = DBHelper.getPlayer(username);
			String playerPassword = player.get(PASSWORD_KEY).toString();
			if (playerPassword.equals(password)) {
				return true;
			}
		}

		return false;
	}

	public void addGameToUser(String gameID, String username){
		username = username.toLowerCase();

		if (doesUserExist(username)) {
			DBObject player = DBHelper.getPlayer(username);
			
			ArrayList<DBObject> games = (ArrayList<DBObject>)player.get(GAMES_KEY);
			DBObject gameToAdd = new BasicDBObject(GAME_KEY, gameID);
			DBObject updatedGames = new BasicDBObject(GAMES_KEY, gameToAdd);
			DBObject updateCommand = new BasicDBObject("$push", updatedGames);

			DBCollection playerCollection = DBHelper.getPlayerCollection();
			playerCollection.update(player, updateCommand);
		}
	}

}