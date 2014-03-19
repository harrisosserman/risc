package models;

import java.util.*;
import libraries.DBHelper;
import com.mongodb.*;
import models.WaitingRoom;

public class TimeoutManager extends Thread {

	private static final long MILLISECONDS_PER_HOUR = 3600000;
	private static final long MILLISECONDS_PER_10_SEC = 10000;

	private static boolean hasBeenStarted;


    public void run() {
    	hasBeenStarted = true;

        ArrayList<String> startedGameIDs = WaitingRoom.getGamesInProgress();

        for (String gameID : startedGameIDs) {
        	WaitingRoom wr = WaitingRoom.getWaitingRoom(gameID);

        	DBObject mostRecentTurn = DBHelper.getCurrentTurnForGame(gameID);
        	long mostRecentTurnCreationTime = (Long)mostRecentTurn.get(DBHelper.TIMESTAMP);
       		long timeDifference = System.currentTimeMillis() - mostRecentTurnCreationTime;
       		if (timeDifference > MILLISECONDS_PER_10_SEC) {		//For testing purposes only! Switch to MILLISECONDS_PER_HOUR after testing.
       			//Timeout has occurred
       			System.out.println("----------timeout occurred");
       			int mostRecentTurnNumber = (Integer)mostRecentTurn.get(DBHelper.TURN_KEY);
       			System.out.println("mostRecentTurnNumber: " + mostRecentTurnNumber);
       			for (String username : wr.getUsernames()) {
        			DBObject mostRecentCommittedTurn = DBHelper.getCommittedTurnForPlayerAndGame(gameID, username);
        			int mostRecentCommittedTurnNumber = 0;
        			if (mostRecentCommittedTurn != null) {
						mostRecentCommittedTurnNumber = (Integer)mostRecentCommittedTurn.get(DBHelper.TURN_KEY);
       				}
        			System.out.println("mostRecentCommittedTurnNumber: " + mostRecentCommittedTurnNumber);
        			//if user hasn't committed for the next turn
        			if (mostRecentCommittedTurnNumber < (mostRecentTurnNumber + 1)) {
        				System.out.println("---Got here");
        				//KAT TODO: Create a blank Turn for player (username) and Commit it to game (gameID)
        				    DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
                      BasicDBObject turn_doc = new BasicDBObject();
                      turn_doc.append(Constants.GAME_ID, gameID);
                      turn_doc.append(Constants.USERNAME, username);
                      turn_doc.append(Constants.TURN, mostRecentTurn);
                      turn_doc.append(Constants.COMMITTED, 1);
                      committedTurns.insert(turn_doc);
        				//To test, you must open localhost:9000 and log in because that's where TimeoutManager is made and starts running.
        				//See API.java's logUserIn(String username)
        				//You may also need to drop game.{info, state, committedTurns} because I added a timestamp to game.state.
        				//Then you'll have to create a few new games by playing them.
        			}
        		}
        	}
        }
    }

    public static boolean isStarted(){
    	return hasBeenStarted;
    }
}