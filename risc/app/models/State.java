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
import controllers.API;

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
    private static final String NUM_PLAYERS = "numPlayers";
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
    private static final String PLAYERINFO = "playerInfo";
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
    myActivePlayers = new HashMap<String, Player>();
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


public int loadPreviousState(){
    DBObject lastStateObject = DBHelper.getCurrentTurnForGame(myGameID);
    System.out.println("made it into load prev state");
    BasicDBList playerInfo = (BasicDBList) lastStateObject.get(PLAYERINFO);  
    BasicDBObject[] playerArray = playerInfo.toArray(new BasicDBObject[0]);
    for(BasicDBObject player : playerArray){
        String username = player.get(OWNER).toString();
        System.out.println("made it into playsr" + username);
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
        System.out.println(territory.get(POSITION));
        int position = Integer.parseInt(territory.get(POSITION).toString());
        int food = Integer.parseInt(territory.get(FOOD).toString());
        int technology = Integer.parseInt(territory.get(TECHNOLOGY).toString());
        Territory terr = new Territory(position, p, food, technology);
        System.out.println("new Territory");
        if(territory.get(INFANTRY_)!= null){
        int infantry = Integer.parseInt(territory.get(INFANTRY_).toString());
        for(int i=0; i<infantry; i++){
            Troop t = new Troop(p, TroopType.INFANTRY);
            terr.addTroop(t);
        }}
        if(territory.get(AUTOMATIC_)!= null){
        int automatic = Integer.parseInt(territory.get(AUTOMATIC_).toString());
        for(int i=0; i<automatic; i++){
            Troop t = new Troop(p, TroopType.AUTOMATIC);
            terr.addTroop(t);
        }}
        if(territory.get(ROCKETS_)!= null){
        int rockets = Integer.parseInt(territory.get(ROCKETS_).toString());
        for(int i=0; i<rockets; i++){
            Troop t = new Troop(p, TroopType.ROCKETS);
            terr.addTroop(t);
        }}
        if(territory.get(TANKS_)!= null){
        int tanks = Integer.parseInt(territory.get(TANKS_).toString());
        for(int i=0; i<tanks; i++){
            Troop t = new Troop(p, TroopType.TANKS);
            terr.addTroop(t);
        }}
        if(territory.get(IMPROVEDTANKS_)!= null){
        int impTanks = Integer.parseInt(territory.get(IMPROVEDTANKS_).toString());
        for(int i=0; i<impTanks; i++){
            Troop t = new Troop(p, TroopType.IMPROVEDTANKS);
            terr.addTroop(t);
        }}
        if(territory.get(PLANES_)!= null){
        int planes = Integer.parseInt(territory.get(PLANES_).toString());
        for(int i=0; i<planes; i++){
            Troop t = new Troop(p, TroopType.PLANES);
            terr.addTroop(t);
        }}
        territories.put(terr.getPosition(), terr);
    }
    updateStateWithMoves();
    return territories.size();

}

private TroopType getTroopType(String str){
    if(str.equals("INFANTRY")){
        return TroopType.INFANTRY;
    }
    if(str.equals("AUTOMATIC")){
        return TroopType.AUTOMATIC;
    }
    if(str.equals("ROCKETS")){
        return TroopType.ROCKETS;
    }
    if(str.equals("TANKS")){
        return TroopType.TANKS;
    }
    if(str.equals("IMPROVEDTANKS")){
        return TroopType.IMPROVEDTANKS;
    }
    if(str.equals("PLANES")){
        return TroopType.PLANES;
    }
        return null;
}

public void moveTypeMove(BasicDBObject move){
 System.out.println("made it into MOVE");
    int startInt = Integer.parseInt(move.get(START).toString());
    Territory start = territories.get(startInt);
    int endInt = Integer.parseInt(move.get(END).toString());
    Territory end = territories.get(endInt);
    String troopType = move.get(TROOPTYPE).toString();
    TroopType type = getTroopType(troopType);
    if(start.getDefendingArmy().containsTroop(type)){
        start.removeTroopFromArmy(type);
        Army entering = end.getDefendingArmy();
        entering.addTroop(type);
        end.setDefendingArmy(entering);
    }
    
}

public void moveTypeAttack(BasicDBObject move, Player p){
 System.out.println("made it into ATTACKING");
    int startInt = Integer.parseInt(move.get(START).toString());
    Territory start = territories.get(startInt);
    int endInt = Integer.parseInt(move.get(END).toString());
    Territory end = territories.get(endInt);
    String troopType = move.get(TROOPTYPE).toString();
    TroopType type = getTroopType(troopType);
    if(start.getDefendingArmy().containsTroop(type)){
          System.out.println("there was no " + type + " to move");
        end.addTrooptoAttacker(startInt, type, p);
        start.removeTroopFromArmy(type); 
    }
}

public void moveTypeUpgrade(BasicDBObject move){
    System.out.println("made it into UPGRADE");
    int position = Integer.parseInt(move.get(POSITION).toString());
    Territory position_ = territories.get(position);
    String troopType = move.get(TROOPTYPE).toString();
    String upgradeType = move.get(UPGRADETYPE).toString();
    TroopType trooptype = getTroopType(troopType);
    TroopType upgradetype = getTroopType(upgradeType);
    Army army = position_.getDefendingArmy();
    if(army.containsTroop(trooptype)){
        System.out.println("there was no " + trooptype + " to upgrade");
        army.deleteTroop(trooptype);
        army.addTroop(upgradetype);
        position_.setDefendingArmy(army);
    }
}

public void moveTypePlace(BasicDBObject move, Player p){
    System.out.println("made it into UPGRADE");
    int position = Integer.parseInt(move.get(POSITION).toString());
    Territory position_ = territories.get(position);
    String troopType = move.get(TROOPTYPE).toString();
    String upgradeType = move.get(UPGRADETYPE).toString();
    TroopType trooptype = getTroopType(troopType);
    TroopType upgradetype = getTroopType(upgradeType);
    Army army = position_.getDefendingArmy();
    if(){
        System.out.println("there was no " + trooptype + " to upgrade");
        army.deleteTroop(trooptype);
        army.addTroop(upgradetype);
        position_.setDefendingArmy(army);
    }
}

public void updateStateWithMoves(){
     System.out.println("made it into moves");
    for(String username : myActivePlayers.keySet()){
        System.out.println("made it into moves " + username);
        System.out.println(myGameID);
        DBObject lastCommittedTurn = DBHelper.getCommittedTurnForPlayerAndGame(myGameID, username);
        System.out.println("made it into moves " + username);
        BasicDBList movesList = (BasicDBList) lastCommittedTurn.get(MOVES);  
        System.out.println("made it into moves " + username);
        BasicDBObject[] movesArray = movesList.toArray(new BasicDBObject[0]);
        System.out.println("made it into moves " + username);
        for(BasicDBObject move : movesArray){
                    System.out.println("made it into moves array ");

            int moveType = Integer.parseInt(move.get(MOVETYPE).toString());
            Player p = myActivePlayers.get(username);
            if(moveType == 0){
                moveTypeUpgrade(move);
            }
            else if(moveType == 1){
                moveTypeMove(move);
            }
            else if(moveType == 2){
                moveTypeAttack(move, p);
            }

        }
    }
    doAttacksAndMoves();
}

public ArrayList<Attacker> attackersForBattle(HashMap<Player, Attacker> map){
    ArrayList<Attacker> attackers = new ArrayList<Attacker>();
    for(Player p : map.keySet()){
        attackers.add(map.get(p));
    }
    return attackers;
}

public void doAttacksAndMoves(){
    /*
    for each territory:
    */
    for(Integer position : territories.keySet()){
        HashMap<Integer, Attacker> attackers = territories.get(position).getAttackers();
        HashMap<Player, Attacker> attacks = new HashMap<Player, Attacker>();
        for(Integer attackerint : attackers.keySet()){
            Attacker a = attackers.get(attackerint);
            Player p = a.getOwner();
            if(attackers.containsKey(p)){
                Attacker a_inMap = attackers.get(p);
                a_inMap.combineAttackers(a);
                attacks.put(p, a_inMap);
            }
            else{
                attacks.put(p, a);
            }
        }

        Army winner = battle(attackersForBattle(attacks), territories.get(position).getDefendingArmy());
        territories.get(position).setDefendingArmy(winner);
    }
    finalizeState();
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

//add in troops size
public Army battle(ArrayList<Attacker> attackers, Army defender){
    Collections.shuffle(attackers);
    System.out.println("battle has begun");
    while(attackers.size()>0){
        for(int i=attackers.size(); i>0; i--){
            System.out.println("first attacker");
            Army attacker = attackers.get(i).getArmy();
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
                if(defender.getNumberOfTroops()==0){
                    defender = attacker;
                    attackers.remove(i);
                    if(attackers.size()==0){
                        return defender;
                    }
                }
            }
            else{
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getNumberOfTroops()==0){
                    attackers.remove(i);
                    if(attackers.size()==0){
                        return defender;
                    }
                } 
            }
        }
        for(int j=attackers.size(); j>0; j--){
            Army attacker = attackers.get(j).getArmy();
            Troop battler_1 = defender.getWeakest();
            Troop battler_2 = attacker.getStrongest();
            double batt_1 = battler_1.battle();
            double batt_2 = battler_2.battle();

            if(battler_1.battle() < battler_2.battle()){
                defender.deleteTroop(battler_1.getType());
                if(defender.getNumberOfTroops()==0){
                    defender = attacker;
                    attackers.remove(j);
                    if(attackers.size()==0){
                        return defender;
                    }

                }                
            }
            else{
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getNumberOfTroops()==0){
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

 public void finalizeState(){
    for(String username: myActivePlayers.keySet()){
        Player player = myActivePlayers.get(username);
        int troopsToDelete = 0;
        int food = player.getFood();
        for(Integer position : territories.keySet()){
            Territory t = territories.get(position);
            if(t.getOwner().equals(player)){
                int troops = t.getDefendingArmy().getNumberOfTroops();
                if(food>=troops){
                    food = food - troops;
                }
                else{
                    troopsToDelete = troopsToDelete + (troops - food);
                }
            }
        }
        if(troopsToDelete>0){
            ArrayList<TroopType> types = new ArrayList<TroopType>();
            types.add(TroopType.INFANTRY);
            types.add(TroopType.AUTOMATIC);
            types.add(TroopType.ROCKETS);
            types.add(TroopType.TANKS);
            types.add(TroopType.IMPROVEDTANKS);
            types.add(TroopType.PLANES);
            for(TroopType type : types){
                for(Integer position : territories.keySet()){
                    Territory t = territories.get(position);
                    if(t.getOwner().equals(player)){
                        while(t.tryToDeleteTroop(type) && troopsToDelete >0){
                            troopsToDelete --;
                        }
                        if(troopsToDelete==0){
                            player.setFood(food);
                            return;
                        }
                    }
                }
            }
        } 
    }
    saveState(); 
 }
 
 public String typeToString(TroopType t){
    	switch(t){
			case INFANTRY: return "INFANTRY";
			case AUTOMATIC: return "AUTOMATIC";
			case ROCKETS: return "ROCKETS";
			case TANKS: return "TANKS";
			case IMPROVEDTANKS: return "IMPROVEDTANKS";
			case PLANES: return "PLANES";
			default: return "INFANTRY";
		}
}

 public void saveState(){
    DBCollection state = DBHelper.getStateCollection();
    BasicDBObject turn_doc = new BasicDBObject();
    turn_doc.append(GAME_ID, myGameID);
    turn_doc.append(TURN, turn++);
    turn_doc.append(NUM_PLAYERS, myActivePlayers.size());
    List<BasicDBObject> territory_list = new ArrayList<BasicDBObject>();
    for(Integer position : territories.keySet()){
        Territory t = territories.get(position);
        BasicDBObject territory_doc = new BasicDBObject();
        int position_ = t.getPosition();
        territory_doc.append(POSITION, position_);
        Player owner = territories.get(position).getOwner();
        territory_doc.append(OWNER, owner);
        HashMap<TroopType, ArrayList<Troop>> a = t.getDefendingArmy().getTroops();
        for(TroopType type : a.keySet()){
            ArrayList<Troop> troops = a.get(type);
            int size = troops.size();
            territory_doc.append(typeToString(type), size);
        }
        territory_doc.append(FOOD, t.getFood());
        territory_doc.append(TECHNOLOGY, t.getTechnology());
        territory_list.add(territory_doc);
    }
    turn_doc.append(TERRITORIES, territory_list);

    List<BasicDBObject> player_list = new ArrayList<BasicDBObject>();
    for(String username : myActivePlayers.keySet()){
        BasicDBObject player_doc = new BasicDBObject();
        Player p = myActivePlayers.get(username);
        player_doc.append(OWNER, p.getName());
        player_doc.append(LEVEL, p.getTechnologyLevel());
        player_doc.append(FOOD, p.getFood());
        player_doc.append(TECHNOLOGY, p.getTechnology());
        player_list.add(player_doc);
    }
    turn_doc.append(PLAYERINFO, player_list);
    state.insert(turn_doc);

    return;
}

}