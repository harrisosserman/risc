package models;

import java.util.*;
import com.mongodb.*;
import libraries.DBHelper;

public class UserManager{
	public static final String NAME_KEY = "name";
	public static final String PASSWORD_KEY = "password";
	public static final String GAMES_KEY = "games";

	public boolean createUser(String username, String password){
		System.out.println("username: " + username);
		System.out.println("pass: " + password);

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

}