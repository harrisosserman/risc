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
    private static final String ADDITIONALINFANTRY = "additionalInfantry";


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
    turn = Integer.parseInt(lastStateObject.get(TURN).toString());
    BasicDBList playerInfo = (BasicDBList) lastStateObject.get(PLAYERINFO);  
    BasicDBObject[] playerArray = playerInfo.toArray(new BasicDBObject[0]);
    for(BasicDBObject player : playerArray){
        String username = player.get(OWNER).toString();
        int food = Integer.parseInt(player.get(FOOD).toString());
//        System.out.println("made it into playsr" + username);
        int technology_level = Integer.parseInt(player.get(LEVEL).toString());
        int technology = Integer.parseInt(player.get(TECHNOLOGY).toString());
  //      System.out.println("made it into playsr" + technology);
        int addTroops = Integer.parseInt(player.get(ADDITIONALINFANTRY).toString());
        Player player1 = new Player(username);
    //    System.out.println("made it into playsr" + username);
        player1.setFood(food);
        player1.setTechnologyLevel(technology_level);
        System.out.println("made it into playsr" + technology);
        player1.setTechnology(technology);
        player1.setAdditionalTroops(addTroops);
        myActivePlayers.put(username, player1);
    }
    System.out.println("made it between");
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
    saveState(); 
    return 5;

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

public void moveTypeUpgrade(BasicDBObject move, Player p){
    System.out.println("made it into UPGRADE");
    int position = Integer.parseInt(move.get(POSITION).toString());
    Territory position_ = territories.get(position);
    String troopType = move.get(TROOPTYPE).toString();
    String upgradeType = move.get(UPGRADETYPE).toString();
    TroopType trooptype = getTroopType(troopType);
    TroopType upgradetype = getTroopType(upgradeType);
    Army army = position_.getDefendingArmy();
    if(army.containsTroop(trooptype)){
        int cost = computeCostOfTroopUpgrade(trooptype, upgradetype);
        if(p.getTechnology() >= cost){
            int newTech = p.getTechnology() - cost;
            p.setTechnology(newTech);
            myActivePlayers.put(p.getName(), p);
            army.deleteTroop(trooptype);
            army.addTroop(upgradetype);
            position_.setDefendingArmy(army);
        }
    }
}

public void moveTypePlace(BasicDBObject move, Player p){
    System.out.println("made it into place");
    int position = Integer.parseInt(move.get(POSITION).toString());
    Territory position_ = territories.get(position);
    String troopType = move.get(TROOPTYPE).toString();
    TroopType trooptype = getTroopType(troopType);
    Army army = position_.getDefendingArmy();
    if(p.getAdditionalTroops()>=0){
        army.addTroop(trooptype);
        p.setAdditionalTroops(p.getAdditionalTroops()-1);
    }
}

public int computeCostOfUpgrade(int oldlevel, int newlevel){
    if(oldlevel == 0){
        if(newlevel==1){
            return 20;
        }
        else if(newlevel==2){
            return 70;
        }
        else if(newlevel==3){
            return 150;
        }
        else if(newlevel==4){
            return 270;
        }
        else if(newlevel==5){
            return 420;
        }
    }
    else if(oldlevel == 1){
        if(newlevel==2){
            return 50;
        }
        else if(newlevel==3){
            return 130;
        }
        else if(newlevel==4){
            return 250;
        }
        else if(newlevel==5){
            return 400;
        }
    }
    else if(oldlevel == 2){
        if(newlevel==3){
            return 80;
        }
        else if(newlevel==4){
            return 200;
        }
        else if(newlevel==5){
            return 350;
        }
    }    
    else if(oldlevel == 3){
        if(newlevel==4){
            return 120;
        }
        else if(newlevel==5){
            return 270;
        }
    }
    else if(oldlevel == 4){
        if(newlevel == 5){
            return 150;
        }
    }
    return 0;
}

public int computeCostOfTroopUpgrade(TroopType old, TroopType new_){
    if(old.equals(TroopType.INFANTRY)){
        if(new_.equals(TroopType.AUTOMATIC)){
            return 3;
        }
        else if(new_.equals(TroopType.ROCKETS)){
            return 11;
        }
        else if(new_.equals(TroopType.TANKS)){
            return 30;
        }
        else if(new_.equals(TroopType.IMPROVEDTANKS)){
            return 55;
        }
        else if(new_.equals(TroopType.PLANES)){
            return 90;
        }
        return 0;
    }
    else if(old.equals(TroopType.INFANTRY)){
        if(new_.equals(TroopType.ROCKETS)){
            return 8;
        }
        else if(new_.equals(TroopType.TANKS)){
            return 27;
        }
        else if(new_.equals(TroopType.IMPROVEDTANKS)){
            return 52;
        }
        else if(new_.equals(TroopType.PLANES)){
            return 87;
        }
        return 0;
    }
    else if(old.equals(TroopType.INFANTRY)){
        if(new_.equals(TroopType.TANKS)){
            return 19;
        }
        else if(new_.equals(TroopType.IMPROVEDTANKS)){
            return 44;
        }
        else if(new_.equals(TroopType.PLANES)){
            return 79;
        }
        return 0;
    }    
    else if(old.equals(TroopType.INFANTRY)){
        if(new_.equals(TroopType.IMPROVEDTANKS)){
            return 25;
        }
        else if(new_.equals(TroopType.PLANES)){
            return 60;
        }
        return 0;
    }
    else if(old.equals(TroopType.INFANTRY)){
        if(new_.equals(TroopType.PLANES)){
            return 150;
        }
        return 0;
    }
    return 0;
}

public void updateStateWithMoves(){
    System.out.println("made it into moves");
    for(String username : myActivePlayers.keySet()){
        Player p = myActivePlayers.get(username);
        System.out.println("made it into moves " + username);
        System.out.println(myGameID);
        DBObject lastCommittedTurn = DBHelper.getCommittedTurnForPlayerAndGame(myGameID, username);
        System.out.println("made it into moves " + username);
        int technology = Integer.parseInt(lastCommittedTurn.get(TECHNOLOGY).toString());
        int technology_level = Integer.parseInt(lastCommittedTurn.get(TECHNOLOGY_LEVEL).toString());
        if(technology_level != p.getTechnologyLevel()){
            int cost = computeCostOfUpgrade(p.getTechnologyLevel(), technology_level);
            if(technology >= cost){
                p.setTechnology(technology-cost);
                p.setTechnologyLevel(technology_level);
                myActivePlayers.put(p.getName(), p);
            }
        }
        BasicDBList movesList = (BasicDBList) lastCommittedTurn.get(MOVES);  
        System.out.println("made it into moves " + username);
        BasicDBObject[] movesArray = movesList.toArray(new BasicDBObject[0]);
        System.out.println("made it into moves " + username);
        for(BasicDBObject move : movesArray){
            System.out.println("made it into moves array ");
            int moveType = Integer.parseInt(move.get(MOVETYPE).toString());
            if(moveType == 0){
                moveTypeUpgrade(move, p);
            }
            else if(moveType == 1){
                moveTypeMove(move);
            }
            else if(moveType == 2){
                moveTypeAttack(move, p);
            }
            else if(moveType == 3){
                moveTypePlace(move, p);
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
            int attackingFrom = a.getTerritory();
            HashMap<Integer, Attacker> attackers_crossing = territories.get(attackingFrom).getAttackers();
            if(attackers_crossing.containsKey(position)){

            }
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
        System.out.println("the winner is " + winner.getOwner().getName());
        territories.get(position).setDefendingArmy(winner);

        territories.get(position).setOwner(winner.getOwner());
    }
    finalizeState();
}

/*public Army battleCrossing(Army attackers, Army defender){
    while(attackers.size()>0){
        System.out.println("loop 1");
        for(int i=attackers.size()-1; i>=0; i--){

            Army attacker = attackers.get(i).getArmy();
            System.out.println("first attacker is " + attackers.get(i).getName());
            Troop battler_1 = defender.getStrongest();
            System.out.println("first strongest defender is a " + battler_1.getType());
            Troop battler_2 = attacker.getWeakest();
            System.out.println("first weakest attacker is a " + battler_2.getType());
            double batt_1 = battler_1.battle();
            double batt_2 = battler_2.battle();
            if(batt_1 == batt_2){
                System.out.println("it was a tie " + batt_1);
                if(battler_1.getStrength() >= battler_2.getStrength()){
                    batt_1++;
                }
                else{
                    batt_2++;
                }
            }
            if(batt_1 < batt_2){
                System.out.println(batt_1 + " is less than  " +batt_2 + " the defender lost one troop");
                defender.deleteTroop(battler_1.getType());
                if(defender.getNumberOfTroops()==0){
                    System.out.println("the defender lost the battle");
                    defender = attacker;
                    System.out.println("the new defender is " + attacker.getName());
                    attackers.remove(i);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }
                }
            }
            else{
                System.out.println(batt_2 + " is less than  " + batt_1 + " the attacker lost one troop");
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getNumberOfTroops()==0){
                    attackers.remove(i);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }
                } 
            }
        }
        System.out.println("loop 2");
        for(int j=attackers.size()-1; j>=0; j--){
            Army attacker = attackers.get(j).getArmy();
            System.out.println("first attacker is " + attackers.get(j).getName());
            Troop battler_1 = defender.getWeakest();
            System.out.println("first weakest defender is " + battler_1.getType());
            Troop battler_2 = attacker.getStrongest();
            System.out.println("first strongest attacker is " + battler_2.getType());
            double batt_1 = battler_1.battle();
            double batt_2 = battler_2.battle();

            if(batt_1 < batt_2){
                System.out.println(batt_1 + " is less than  " + batt_2 + " the defender lost one troop");
                defender.deleteTroop(battler_1.getType());

                if(defender.getNumberOfTroops()==0){
                    defender = attacker;
                    attackers.remove(j);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }

                }                
            }
            else{
                 System.out.println(batt_1 + " is less than  " + batt_2 + " the attacker lost one troop");
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getNumberOfTroops()==0){
                    attackers.remove(j);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }
                } 
            }
        }
    }
    return defender;
}*/


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
        System.out.println("loop 1");
        for(int i=attackers.size()-1; i>=0; i--){

            Army attacker = attackers.get(i).getArmy();
            System.out.println("first attacker is " + attackers.get(i).getName());
            Troop battler_1 = defender.getStrongest();
            System.out.println("first strongest defender is a " + battler_1.getType());
            Troop battler_2 = attacker.getWeakest();
            System.out.println("first weakest attacker is a " + battler_2.getType());
            double batt_1 = battler_1.battle();
            double batt_2 = battler_2.battle();
            if(batt_1 == batt_2){
                System.out.println("it was a tie " + batt_1);
                if(battler_1.getStrength() >= battler_2.getStrength()){
                    batt_1++;
                }
                else{
                    batt_2++;
                }
            }
            if(batt_1 < batt_2){
                System.out.println(batt_1 + " is less than  " +batt_2 + " the defender lost one troop");
                defender.deleteTroop(battler_1.getType());
                if(defender.getNumberOfTroops()==0){
                    System.out.println("the defender lost the battle");
                    defender = attacker;
                    System.out.println("the new defender is " + attacker.getName());
                    attackers.remove(i);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }
                }
            }
            else{
                System.out.println(batt_2 + " is less than  " + batt_1 + " the attacker lost one troop");
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getNumberOfTroops()==0){
                    attackers.remove(i);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }
                } 
            }
        }
        System.out.println("loop 2");
        for(int j=attackers.size()-1; j>=0; j--){
            Army attacker = attackers.get(j).getArmy();
            System.out.println("first attacker is " + attackers.get(j).getName());
            Troop battler_1 = defender.getWeakest();
            System.out.println("first weakest defender is " + battler_1.getType());
            Troop battler_2 = attacker.getStrongest();
            System.out.println("first strongest attacker is " + battler_2.getType());
            double batt_1 = battler_1.battle();
            double batt_2 = battler_2.battle();

            if(batt_1 < batt_2){
                System.out.println(batt_1 + " is less than  " + batt_2 + " the defender lost one troop");
                defender.deleteTroop(battler_1.getType());

                if(defender.getNumberOfTroops()==0){
                    defender = attacker;
                    attackers.remove(j);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }

                }                
            }
            else{
                 System.out.println(batt_1 + " is less than  " + batt_2 + " the attacker lost one troop");
                attacker.deleteTroop(battler_2.getType());
                if(attacker.getNumberOfTroops()==0){
                    attackers.remove(j);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }
                } 
            }
        }
    }
    return defender;
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
    System.out.println("finalizing");
    for(String username: myActivePlayers.keySet()){
        Player player = myActivePlayers.get(username);
        int food = player.getFood();
        for(Integer position : territories.keySet()){
            Territory t = territories.get(position);
            if(t.getOwner().equals(player)){
                int newFood = player.getFood() + t.getFood();
                System.out.println("terr tech = " + t.getTechnology());
                System.out.println("player tech  = " +  player.getTechnology());
                int newTech = t.getTechnology() + player.getTechnology();
                System.out.println(newTech);
                player.setTechnology(newTech);
                player.setFood(newFood);
            }
        }
    }
    for(String username: myActivePlayers.keySet()){
        Player player = myActivePlayers.get(username);
        int troopsToDelete = 0;
        int food = player.getFood();
        for(Integer position : territories.keySet()){
            Territory t = territories.get(position);

            if(t.getOwner().equals(player)){
                System.out.println("the players were equal");
                int troops = t.getDefendingArmy().getNumberOfTroops();
                System.out.println("number of troops " + troops);
                if(food>=troops){
                    food = food - troops;
                    player.setFood(food);
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
                            return;
                        }
                    }
                }
            }
        } 
    }
    
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



public boolean playerOwnsATerritory(Player owner){
    for(Integer position : territories.keySet()){
        Territory t = territories.get(position);
        if(t.getOwner().equals(owner)){
            return true;
        }
    }
    return false;
}


public void saveState(){
    System.out.println("saving");
    DBCollection state = DBHelper.getStateCollection();
    BasicDBObject turn_doc = new BasicDBObject();
    turn_doc.append(GAME_ID, myGameID);
    turn = turn + 1;
    System.out.println(turn);
    turn_doc.append(TURN, turn);
        System.out.println("saving");
    turn_doc.append(NUM_PLAYERS, myActivePlayers.size());
        System.out.println("saving");
    List<BasicDBObject> territory_list = new ArrayList<BasicDBObject>();
    for(Integer position : territories.keySet()){
        System.out.println("territories");
        Territory t = territories.get(position);
        BasicDBObject territory_doc = new BasicDBObject();
        int position_ = t.getPosition();
        territory_doc.append(POSITION, position_);
        Player owner = territories.get(position).getOwner();
        territory_doc.append(OWNER, owner.getName());
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
        System.out.println("players");
        BasicDBObject player_doc = new BasicDBObject();
        Player p = myActivePlayers.get(username);
        if(playerOwnsATerritory(p)){
            player_doc.append(OWNER, p.getName());
            player_doc.append(LEVEL, p.getTechnologyLevel());
            player_doc.append(FOOD, p.getFood());
            player_doc.append(TECHNOLOGY, p.getTechnology());
            player_doc.append(ADDITIONALINFANTRY, p.getAdditionalTroops());
            player_list.add(player_doc);
        }

    }
    turn_doc.append(PLAYERINFO, player_list);
    state.insert(turn_doc);

    return;
}

}