package libraries;

import java.util.*;
import com.mongodb.*;
import java.net.UnknownHostException;

public class ConnectionManager{
	private static ConnectionManager instance;

	private MongoConnection connection;

	private ConnectionManager(){
		try{
			 connection = new MongoConnection();
		}catch (UnknownHostException exception){
			System.out.println(exception.toString());
		}
	}

	public static ConnectionManager getInstance(){
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	public MongoConnection getConnection(){
		return connection;
	}
}