package models;

import java.util.*;
import libraries.MongoConnection;
import com.mongodb.*;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import models.Territory;
import models.Troop;

public class State{

	private static final String DEFAULT_GAME_ID = "12345";
	private static final int NUM_TERRITORIES = 50;
	private static final String INITIALIZATION_DB = "initialization";
    private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
    private static final String NAME = "name";
    private static final String COUNT = "count";
    private static final String READY = "ready";
    private static final String PLAYERS = "players";
    private static final String GAME_ID = "gameID";

	private String myGameID;
	private ArrayList<Player> myPlayers;
	private Territory[] myTerritories;

public State(){

}	


}