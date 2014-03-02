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


public class BattleTest{


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


public BattleTest(){
    Player p = new Player("Kat");
    ArrayList<Attacker> attackers = new ArrayList<Attacker>();
    Army defender = new Army(p);
    Player p1 = new Player("Harris");
    Player p2 = new Player("Julian");
    Attacker a1 = new Attacker(p1, 1);
    Attacker a2 = new Attacker(p2, 2);
    for(int i=0; i<1; i++){
        a1.addTroop(TroopType.PLANES);
            a2.addTroop(TroopType.ROCKETS);
       a2.addTroop(TroopType.PLANES);
        a1.addTroop(TroopType.ROCKETS);
        a2.addTroop(TroopType.INFANTRY);
        a1.addTroop(TroopType.INFANTRY);
        a1.addTroop(TroopType.AUTOMATIC);
        a1.addTroop(TroopType.TANKS);
        defender.addTroop(TroopType.INFANTRY);
        defender.addTroop(TroopType.PLANES);
        defender.addTroop(TroopType.TANKS);
        defender.addTroop(TroopType.ROCKETS);
    }
    attackers.add(a1);
    attackers.add(a2);
    battle(attackers, defender);
  //  attackers = new ArrayList<Attacker>();
}
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
}