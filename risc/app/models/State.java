package models;

import java.util.*;
import com.mongodb.*;
import com.mongodb.util.JSON;
import com.fasterxml.jackson.databind.*;
import java.net.UnknownHostException;
import models.Territory;
import models.Troop;
import play.mvc.Http.RequestBody;
import models.Attacker;
import libraries.DBHelper;

/**
 * This class is responsible for computing and updating the state after all
 * the turns are committed. The 
 * 
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * 
 */


public class State{

    private static final int NUM_TERRITORIES = 25;
    private static final String INITIALIZATION_DB = "initialization";
    private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
    private static final String GAME_DB = "game";
    private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
    private static final String PLAYER = "player";
    private static final String ATTACKING = "attacking";
    private static final String TROOPS = "troops";
    private static final String STATE = "state";
    private static final String TERRITORIES = "territories";
    private static final String TERRITORY = "territory";
    private static final String TURN = "turn";
    private static final String GAME_ID = "gameID";
    private static final String OWNER = "owner";
    private static final String POSITION = "position";
    private static final int ADDITIONAL_TROOPS = 1;
    private static final String ACTIVE_PLAYER_COUNT = "activePlayerCount";
    private static final String COUNT = "count";
    private static final String ACTIVE_PLAYERS = "activePlayers";
    private static final String PLAYER_NUMBER = "playerNumber";

    private int turn;
    private int myActivePlayerCount;
    private int playerID;
    private String myGameID;
    private ArrayList<Territory> territories;
    private ArrayList<DBObject> myActivePlayers;
    //private ArrayList<Attacker> attackers;


public State(String gameID){
    territories = new ArrayList<Territory>();
    myGameID = gameID;
  //  attackers = new ArrayList<Attacker>();
}

/*
 * This method assembles the state by reading the committed turns from the database after
 * all the turns have been committed. This method returns void because it saves the state
 * down to the database beore completion.  
 * 
 * This implementation first queries the database for the 
 *
 * @param turn_number the number turn the game is on
 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not 
 * be determined.
 */

public void assembleState(int turn_number) throws UnknownHostException{
    turn = turn_number;
    DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
    BasicDBObject query = new BasicDBObject();
    query.put(GAME_ID, myGameID);
    query.put(TURN, turn);
    DBCursor cursor = committedTurns.find(query);
    for(int i=0; i<NUM_TERRITORIES; i++){
        Territory territory_empty = new Territory(i, -1, -1);
        territories.add(territory_empty);
    }
    System.out.println("sample");
    while(cursor.hasNext()) {
        DBObject object = cursor.next();
        System.out.println("my game id is " + myGameID);
        playerID = Integer.parseInt(object.get(PLAYER).toString());
        System.out.println("the territories object is: " + object.get(TERRITORIES).toString());
        BasicDBList territoryData = (BasicDBList) object.get(TERRITORIES);
        BasicDBObject[] territoryArray = territoryData.toArray(new BasicDBObject[0]);
        for(BasicDBObject terr : territoryArray){
            String troops_str = terr.get(TROOPS).toString();
            int troops = Integer.parseInt(troops_str);
            String position_str = terr.get(POSITION).toString();
            int position = Integer.parseInt(position_str);
            territories.get(position).setOwner(playerID);
            territories.get(position).setDefendingArmy(troops);
            System.out.println("the owner of territory " + position + " is " + territories.get(position).getOwner());
            BasicDBList attackerData = (BasicDBList) terr.get(ATTACKING);
            BasicDBObject[] attackerArray = attackerData.toArray(new BasicDBObject[0]);
            attackerLoop:
            for(BasicDBObject attack : attackerArray){
                int attacker_territory = Integer.parseInt(attack.get(TERRITORY).toString());
                int attacker_number = Integer.parseInt(attack.get(TROOPS).toString());
                ArrayList<Attacker> attackers_inTerritory = territories.get(attacker_territory).getAttackers();
                for(Attacker _attacker : attackers_inTerritory){
                    if(_attacker.getOwner() == playerID){
                        _attacker.setStrength(_attacker.getStrength() + attacker_number);
                        continue attackerLoop;
                    }
                }
                Attacker a = new Attacker(playerID, attacker_number, attacker_territory, position);
                territories.get(attacker_territory).addAttacker(a);
                System.out.println("the owner of the attacker is " + a.getOwner());
            }
        }
    }
    findState();
    saveState();
    return;
}

public void findState(){
   for(int i=0; i<territories.size(); i++){
    System.out.println("we are analyzing territory :" + i);
    Territory battlefield = territories.get(i);
    int defender = battlefield.getOwner();
    int defender_troops = battlefield.getDefendingArmy();
    System.out.println("the defending player is " + defender + " with " + defender_troops + " troops.");
    ArrayList<Attacker> attackers = battlefield.getAttackers();
        for(int j=0; j<attackers.size(); j++){
            Attacker attacker = attackers.get(j);

            System.out.println("attacker number " + j + "is " + attacker.getOwner() + " with " + attacker.getStrength() +" troops.");
            int[] winner = battle(attacker.getOwner(), attacker.getStrength(), defender, defender_troops);
            defender = winner[0];
            defender_troops = winner[1];
            System.out.println("Territory " + i + " was just won by " + defender + " who now has " + defender_troops + " troops.");
        }
        battlefield.setOwner(defender);
        battlefield.setDefendingArmy(defender_troops);
    }
}
//add in troops size
public int[] battle(int attacker, int a_troops, int defender, int d_troops){
    int[] winnerStrength = new int[2];
    while(a_troops > 0 && d_troops > 0){
        double a = Math.random()*19 + 1;
        double d = Math.random()*19 + 1;
        if(a > d){
            d_troops--;
            System.out.println("defender " + defender + "lost a troop to " + attacker);

        }
        else{
            System.out.println("the attacker" + attacker + " lost a troop to " + defender);
            a_troops--;
        }
    }
    if(a_troops > 0){
        winnerStrength[0] = attacker;
        System.out.println("the attacker won " + attacker);
        winnerStrength[1] = a_troops;
    }
    else{
        winnerStrength[0] = defender;
        System.out.println("the defender won " + defender);
        winnerStrength[1] = d_troops;
    }
    return winnerStrength;
}

 public void saveState() throws UnknownHostException{
    DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
    DBCollection state = DBHelper.getStateCollection();
    BasicDBObject turn_doc = new BasicDBObject();
    turn_doc.append(GAME_ID, myGameID);
    turn_doc.append(TURN, turn++);
    List<BasicDBObject> territory_list = new ArrayList<BasicDBObject>();
    for(int i=0; i<territories.size(); i++){
        BasicDBObject territory_doc = new BasicDBObject();
        int position = territories.get(i).getPosition();
        territory_doc.append(POSITION, position);
        int owner = territories.get(i).getOwner();
        territory_doc.append(OWNER, owner);

        BasicDBObject gameQuery = new BasicDBObject(GAME_ID, myGameID);
        DBObject info = DBHelper.getInfoCollection().findOne(gameQuery);
        ArrayList<DBObject> activePlayers = (ArrayList<DBObject>)info.get(ACTIVE_PLAYERS);

        int additionalTroopCount = 0;
        for (DBObject activePlayer : activePlayers) {
            if ((Integer)activePlayer.get(PLAYER_NUMBER) == owner) {
                additionalTroopCount = ADDITIONAL_TROOPS;
            }
        }
        territory_doc.append(TROOPS, territories.get(i).getDefendingArmy() + additionalTroopCount);
        territory_list.add(territory_doc);
    }

    turn_doc.append(TERRITORIES, territory_list);
    state.insert(turn_doc);

    return;
}

}