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
 * the turns are committed. The constants listed below are used for accessing
 * the different things in the database. 
 *
 * The private ints, and ArrayLists are common to the entire state and
 * instatitated in the constructor, and edited by all methods in the 
 * class. 
 *
 * This class is instatitated and called after all turns are committed, 
 * within the commitTurn API post call. 
 */


public class State{

    private static final int NUM_TERRITORIES = 25;
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
 * This implementation first queries the database for the instances of committed turn by 
 * the turn number and gameID. This is done by creating a BasicDBObject with the those
 * parameters and querying the committedTurns collection in the game database. 
 * 
 * An empty territories map is then created so that the correct number of territories are
 * in the arraylist and can be accessed and updated and will remain in the correct order. 
 * (territory 0 will be in position 0 etc). 
 * 
 * A cursor points to the first document that was returned by the query, and the territories
 * and attackers that are in each committed turn are updated into the state. The attackers 
 * are added to an array list within the territory they are attacking. 
 * 
 * The attackers that come from the same owner in one attacker are combined into a single 
 * attacker. Once the state is assembled (the array of territories is created), the state
 * is calculated and then the state is saved in the database. Those methods are called 
 * and explained below.
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
    while(cursor.hasNext()) {
        DBObject object = cursor.next();
        playerID = Integer.parseInt(object.get(PLAYER).toString());
        BasicDBList territoryData = (BasicDBList) object.get(TERRITORIES);
        BasicDBObject[] territoryArray = territoryData.toArray(new BasicDBObject[0]);
        for(BasicDBObject terr : territoryArray){
            String troops_str = terr.get(TROOPS).toString();
            int troops = Integer.parseInt(troops_str);
            String position_str = terr.get(POSITION).toString();
            int position = Integer.parseInt(position_str);
            territories.get(position).setOwner(playerID);
            territories.get(position).setDefendingArmy(troops);
            BasicDBList attackerData = (BasicDBList) terr.get(ATTACKING);
            BasicDBObject[] attackerArray = attackerData.toArray(new BasicDBObject[0]);
            attackerLoop:
            for(BasicDBObject attack : attackerArray){
                int attacker_territory = Integer.parseInt(attack.get(TERRITORY).toString());
                int attacker_number = Integer.parseInt(attack.get(TROOPS).toString());
           //     Attacker a = new Attacker(playerID, attacker_number, attacker_territory, position);
           //     territories.get(attacker_territory).addAttacker(a);
         //       System.out.println("the owner of the attacker is " + a.getOwner());
            }     

        }
    }
    findState();
    saveState();
    return;
}

/* 
 * The find state method is where the new state is found after all of the turns
 * are entered into an array list of territories. The territories are then updated
 * after each attack takes place. In a for loop for each territory, the defender 
 * of the battle is set as the owner, and the array list of attackers is assembled.
 * 
 * Each attacker individually attacks the defender and if the attacker wins, they
 * become the defender for the next attacker. The battle method is called to 
 * execute the battles. 
 * 
 * The method updates the already existing Territory ArrayList so that the list
 * is ready to be stored down to the database in the saveState method that is called
 * after findState().
 *
 * 
 * 
 */


public void findState(){
   for(int i=0; i<territories.size(); i++){
    //System.out.println("we are analyzing territory :" + i);
    Territory battlefield = territories.get(i);
    int defender = battlefield.getOwner();
    int defender_troops = battlefield.getDefendingArmy();
    //System.out.println("the defending player is " + defender + " with " + defender_troops + " troops.");
    ArrayList<Attacker> attackers = battlefield.getAttackers();
        for(int j=0; j<attackers.size(); j++){
            Attacker attacker = attackers.get(j);
//            int[] winner = battle(attacker.getOwner(), attacker.getStrength(), defender, defender_troops);
  //          defender = winner[0];
    //        defender_troops = winner[1];

        }
        battlefield.setOwner(defender);
        battlefield.setDefendingArmy(defender_troops);
    }
}


//add in troops size
public Army battle(ArrayList<Army> attackers, Army defender){
    Collections.shuffle(attackers);
    while(attackers.size()>0){
        for(int i=attackers.size(); i>0; i--){
            Army attacker = attackers.get(i);
            Troop battler_1 = defender.getStrongest();
            Troop battler_2 = attacker.getWeakest();
            double batt_1 = battler_1.battle();
            double batt_2 = battler_2.battle();
            if(batt_1 == batt_2){
                if(battler_1.getStrength() >= battler_2.getStrength){
                    batt_1++;
                }
                else{
                    batt_2++;
                }
            }
            if(batt_1 < batt_2){
                defender.deleteTroop(battler_1.getType());
                if(defender.getSize()==0){
                    defender = attacker;
                    attackers.remove(i);
                    if(attackers.size()==0){
                        return defender;
                    }
                }
            }
            else{
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getSize()==0){
                    attackers.remove(i);
                    if(attackers.size()==0){
                        return defender;
                    }
                } 
            }
        }
        for(int j=attackers.size(); j>0; j--){
            Army attacker = attackers.get(j);
            Troop battler_1 = defender.getWeakest();
            Troop battler_2 = attacker.getStrongest();
            double batt_1 = battler_1.battle();
            double batt_2 = battler_2.battle();

            if(battler_1.battle() < battler_2.battle()){
                defender.deleteTroop(battler_1.getType());
                if(defender.getSize()==0){
                    defender = attacker;
                    attackers.remove(j);
                    if(attackers.size()==0){
                        return defender;
                    }

                }                
            }
            else{
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getSize()==0){
                    attackers.remove(j);
                    if(attackers.size()==0){
                        return defender;
                    }
                } 
            }
        }
    }
}


/* 
 * The save state method takes the current ArrayList of territories
 * and stores it into the game.state format that can be queried and 
 * read by the front end. 
 *
 * It accesses the state collection, creates a new document in the 
 * form of a BasicDBObject, appends the turn number and gameID and 
 * then iterates through the territories, storing the data for each 
 * territory in an ArrayList of BasicDBObjects, and eventually appending
 * the array list to the original object.
 *
 * The document is then added to the appropriate collection.
 * 
 *
 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not 
 * be determined.
 */


 public void saveState() throws UnknownHostException{
    // delete first line?
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