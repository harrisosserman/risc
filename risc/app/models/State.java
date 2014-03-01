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
import models.TroopType;
import java.util.HashMap;

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
    private static final String INITIALIZATION_DB = "initialization";
    private static final String WAITING_PLAYERS_COLLECTION = "waitingPlayers";
    private static final String GAME_DB = "game";
    private static final String COMMITTED_TURNS_COLLECTION = "committedTurns";
    private static final String PLAYER = "player";
    private static final String USERNAME = "username";
    private static final String ATTACKING = "attacking";
    private static final String TROOPS = "troops";
    private static final String STATE = "state";
    private static final String TERRITORIES = "territories";
    private static final String TERRITORY = "territory";
    private static final String TURN = "turn";
    private static final String GAME_ID = "gameID";
    private static final String POSITION = "position";
    private static final String COUNT = "count";
    private static final String FOOD = "food";
    private static final String TECHNOLOGY = "technology";
    private static final String TECHNOLOGY_LEVEL = "technology_level";
    private static final String LEVEL = "level";
    private static final String MOVES = "moves";
    private static final String TROOPTYPE = "troopType";
    private static final String MOVETYPE = "moveType";
    private static final String START = "start";
    private static final String END = "end";
    private static final String UPGRADETYPE = "upgradeType";
    private static final String TIMESTAMP = "timeStamp";
    private static final String COMMITTED = "committed";
    private static final String PLAYERS = "players";
    private static final String INFANTRY_ = "INFANTRY";
    private static final String AUTOMATIC_ = "AUTOMATIC";
    private static final String ROCKETS_ = "ROCKETS";
    private static final String TANKS_ = "TANKS";
    private static final String IMPROVEDTANKS_ = "IMPROVEDTANKS";
    private static final String PLANES_ = "PLANES";
    private static final String OWNER = "owner";


    private int turn;
    private int myActivePlayerCount;
    private int playerID;
    private String myGameID;
    private HashMap<Integer, Territory> territories;
    private HashMap<String, Player> myActivePlayers;
    //private ArrayList<Attacker> attackers;


public State(String gameID){
    territories = new HashMap<Integer, Territory>();
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

public void loadPreviousState(String gameID) throws UnknownHostException{
    DBObject lastStateObject = DBHelper.getCurrentTurnForGame(gameID);
    BasicDBList playerInfo = (BasicDBList) lastStateObject.get(PLAYERS);  
    BasicDBObject[] playerArray = playerInfo.toArray(new BasicDBObject[0]);
    for(BasicDBObject player : playerArray){
        String username = player.get(OWNER).toString();
        int food = Integer.parseInt(player.get(FOOD).toString());
        int technology_level = Integer.parseInt(player.get(LEVEL).toString());
        int technology = Integer.parseInt(player.get(TECHNOLOGY).toString());
        Player player1 = new Player(username);
        player1.setFood(food);
        player1.setTechnologyLevel(technology_level);
        player1.setTechnology(technology);
        myActivePlayers.put(username, player1);
    }
    BasicDBList territoryInfo = (BasicDBList) lastStateObject.get(TERRITORIES);
    BasicDBObject[] territoryArray = territoryInfo.toArray(new BasicDBObject[0]);
    for(BasicDBObject territory : territoryArray){
        String owner = territory.get(OWNER).toString();
        Player p = myActivePlayers.get(owner);
        int position = Integer.parseInt(territory.get(POSITION).toString());
        int food = Integer.parseInt(territory.get(FOOD).toString());
        int technology = Integer.parseInt(territory.get(TECHNOLOGY).toString());
        Territory terr = new Territory(position, p, food, technology);
        int infantry = Integer.parseInt(territory.get(INFANTRY_).toString());
        for(int i=0; i<infantry; i++){
            Troop t = new Troop(p, TroopType.INFANTRY);
            terr.addTroop(t);
        }
        int automatic = Integer.parseInt(territory.get(AUTOMATIC_).toString());
        for(int i=0; i<automatic; i++){
            Troop t = new Troop(p, TroopType.AUTOMATIC);
            terr.addTroop(t);
        }
        int rockets = Integer.parseInt(territory.get(ROCKETS_).toString());
        for(int i=0; i<rockets; i++){
            Troop t = new Troop(p, TroopType.ROCKETS);
            terr.addTroop(t);
        }
        int tanks = Integer.parseInt(territory.get(TANKS_).toString());
        for(int i=0; i<tanks; i++){
            Troop t = new Troop(p, TroopType.TANKS);
            terr.addTroop(t);
        }
        int impTanks = Integer.parseInt(territory.get(IMPROVEDTANKS_).toString());
        for(int i=0; i<impTanks; i++){
            Troop t = new Troop(p, TroopType.IMPROVEDTANKS);
            terr.addTroop(t);
        }
        int planes = Integer.parseInt(territory.get(PLANES_).toString());
        for(int i=0; i<planes; i++){
            Troop t = new Troop(p, TroopType.PLANES);
            terr.addTroop(t);
        }
        territories.put(terr.getPosition(), terr);
    }

}

public void moveTypeMove(BasicDBObject move)

public void moveTypeAttack(BasicDBObject move)

public void moveTypeUpgrade(BasicDBObject move)

public void moveTypeTechUpgrade(BasicDBObject move)

public void updateStateWithTurn(){

    /*
    get the 5 turns
    for each turn, actual upgrades, troop upgrades, model upgrades
    write a move method
    write an upgrade method
    write an attack method

    */
    for(String username : myActivePlayers.keySet()){
        DBObject lastCommittedTurn = DBHelper.getCommittedTurnForPlayerAndGame(gameID, username);
        BasicDBList movesList = (BasicDBList) lastCommittedTurn.get(MOVES);  
        BasicDBObject[] movesArray = movesList.toArray(new BasicDBObject[0]);
        for(BasicDBObject move : movesArray){
            int moveType = Integer.parseInt(move.get(MOVETYPE).toString());
            if(moveType == 0){
                moveTypeMove(move);
            }
            else if(moveType == 1){

            }
            else if(moveType == 2){

            }
            else if(moveType == 3){

            }
        }
    }
    
}

public void assembleState(int turn_number) throws UnknownHostException{
    turn = turn_number;
    DBCollection committedTurns = DBHelper.getCommittedTurnsCollection();
    BasicDBObject query = new BasicDBObject();
    query.put(GAME_ID, myGameID);
    query.put(TURN, turn);
    DBCursor cursor = committedTurns.find(query);
    for(int i=0; i<NUM_TERRITORIES; i++){
     //   Territory territory_empty = new Territory(i, null, -1, 0, 0);
     //   territories.add(territory_empty);
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
       //     territories.get(position).setOwner(playerID);
         //   territories.get(position).setDefendingArmy(troops);
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
   // saveState();
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
    Player defender = battlefield.getOwner();
    Army defender_troops = battlefield.getDefendingArmy();
    //System.out.println("the defending player is " + defender + " with " + defender_troops + " troops.");
    ArrayList<Attacker> attackers = battlefield.getAttackers();
        for(int j=0; j<attackers.size(); j++){
            Attacker attacker = attackers.get(j);
//            int[] winner = battle(attacker.getOwner(), attacker.getStrength(), defender, defender_troops);
  //          defender = winner[0];
    //        defender_troops = winner[1];

        }
        battlefield.setOwner(defender);
     //   battlefield.setDefendingArmy(defender_troops);
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
                if(battler_1.getStrength() >= battler_2.getStrength()){
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
    return null;
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


/* public void saveState() throws UnknownHostException{
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
        Player owner = territories.get(i).getOwner();
        territory_doc.append(OWNER, owner);

        BasicDBObject gameQuery = new BasicDBObject(GAME_ID, myGameID);
        DBObject info = DBHelper.getInfoCollection().findOne(gameQuery);
        ArrayList<DBObject> activePlayers = (ArrayList<DBObject>)info.get(ACTIVE_PLAYERS);

        int additionalTroopCount = 0;
        for (DBObject activePlayer : activePlayers) {
 //           if ((Integer)activePlayer.get(PLAYER_NUMBER) == owner) {
    //            additionalTroopCount = ADDITIONAL_TROOPS;
         //   }
        }
        territory_doc.append(TROOPS, territories.get(i).getDefendingArmy() + additionalTroopCount);
        territory_list.add(territory_doc);
    }

    turn_doc.append(TERRITORIES, territory_list);
    state.insert(turn_doc);

    return;
}*/

}