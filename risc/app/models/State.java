package models;

import java.util.*;
import com.mongodb.*;
import com.mongodb.util.JSON;
import com.fasterxml.jackson.databind.*;
import java.net.UnknownHostException;
import models.*;
import play.mvc.Http.RequestBody;
import libraries.DBHelper;
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

    private int turn;
    private int myActivePlayerCount;
    private int playerID;
    private String myGameID;
    private HashMap<Integer, Territory> territories;
    private HashMap<String, Player> myActivePlayers;
    private HashMap<Player, TreeSet<Integer>> visibleTerritoriesForEachPlayer;
    private HashMap<Integer, ArrayList<Spy>> spies;
    private ArrayList<PotentialAlly> potentialAllies;
    private ArrayList<Trade> tradeOrders;
    //private ArrayList<Attacker> attackers;


public State(String gameID){
    territories = new HashMap<Integer, Territory>();
    myGameID = gameID;
    myActivePlayers = new HashMap<String, Player>();
    visibleTerritoriesForEachPlayer = new HashMap<Player, TreeSet<Integer>>();
    spies = new HashMap<Integer, ArrayList<Spy>>();
    potentialAllies = new ArrayList<PotentialAlly>();

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
 * @throws UnknownHost Exception Thrown to indicate that the IP address of a host could not 
 * be determined.
 */
public int loadPreviousState(){
    DBObject lastStateObject = DBHelper.getCurrentTurnForGame(myGameID);
    turn = Integer.parseInt(lastStateObject.get(Constants.TURN).toString());
    BasicDBList playerInfo = (BasicDBList) lastStateObject.get(Constants.PLAYERINFO);  
    BasicDBObject[] playerArray = playerInfo.toArray(new BasicDBObject[0]);
    for(BasicDBObject player : playerArray){
        String username = player.get(Constants.OWNER).toString();
        int food = Integer.parseInt(player.get(Constants.FOOD).toString());
        int technology_level = Integer.parseInt(player.get(Constants.LEVEL).toString());
        int technology = Integer.parseInt(player.get(Constants.TECHNOLOGY).toString());
        int addTroops = Integer.parseInt(player.get(Constants.ADDITIONALINFANTRY).toString());
        Player player1 = new Player(username);
        player1.setFood(food);
        player1.setTechnologyLevel(technology_level);
        player1.setTechnology(technology);
        player1.setAdditionalTroops(addTroops);
        myActivePlayers.put(username, player1);
    }
    for(BasicDBObject player : playerArray){
        String username = player.get(Constants.OWNER).toString();
        Player p_ = myActivePlayers.get(username);
        BasicDBList playerAllies = (BasicDBList) player.get(Constants.ALLIES);
        BasicDBObject[] myallies = playerAllies.toArray(new BasicDBObject[0]);
        for(BasicDBObject ally : myallies){
            String allyUsername = ally.toString();
            Player p = myActivePlayers.get(allyUsername);
            p_.addAlly(p);
        }
    }
    //players loaded
    BasicDBList territoryInfo = (BasicDBList) lastStateObject.get(Constants.TERRITORIES);
    BasicDBObject[] territoryArray = territoryInfo.toArray(new BasicDBObject[0]);
    for(BasicDBObject territory : territoryArray){
        String owner = territory.get(Constants.OWNER).toString();
        Player p = myActivePlayers.get(owner);
        System.out.println(territory.get(Constants.POSITION));
        int position = Integer.parseInt(territory.get(Constants.POSITION).toString());
        int food = Integer.parseInt(territory.get(Constants.FOOD).toString());
        int technology = Integer.parseInt(territory.get(Constants.TECHNOLOGY).toString());
        Territory terr = new Territory(position, p, food, technology);
        if(territory.get(Constants.INFANTRY_)!= null){
            int infantry = Integer.parseInt(territory.get(Constants.INFANTRY_).toString());
            for(int i=0; i<infantry; i++){
                Troop t = new Troop(p, TroopType.INFANTRY);
                terr.addTroop(t);
        }}
        if(territory.get(Constants.AUTOMATIC_)!= null){
            int automatic = Integer.parseInt(territory.get(Constants.AUTOMATIC_).toString());
            for(int i=0; i<automatic; i++){
                Troop t = new Troop(p, TroopType.AUTOMATIC);
                terr.addTroop(t);
        }}
        if(territory.get(Constants.ROCKETS_)!= null){
            int rockets = Integer.parseInt(territory.get(Constants.ROCKETS_).toString());
            for(int i=0; i<rockets; i++){
                Troop t = new Troop(p, TroopType.ROCKETS);
                terr.addTroop(t);
        }}
        if(territory.get(Constants.TANKS_)!= null){
            int tanks = Integer.parseInt(territory.get(Constants.TANKS_).toString());
            for(int i=0; i<tanks; i++){
                Troop t = new Troop(p, TroopType.TANKS);
                terr.addTroop(t);
        }}
        if(territory.get(Constants.IMPROVEDTANKS_)!= null){
            int impTanks = Integer.parseInt(territory.get(Constants.IMPROVEDTANKS_).toString());
            for(int i=0; i<impTanks; i++){
                Troop t = new Troop(p, TroopType.IMPROVEDTANKS);
                terr.addTroop(t);
        }}
        if(territory.get(Constants.PLANES_)!= null){
            int planes = Integer.parseInt(territory.get(Constants.PLANES_).toString());
            for(int i=0; i<planes; i++){
                Troop t = new Troop(p, TroopType.PLANES);
                terr.addTroop(t);
        }}
        BasicDBList alliedTroops = (BasicDBList) territory.get(Constants.ALLIEDTROOPS);
        BasicDBObject[] alliedArmies = alliedTroops.toArray(new BasicDBObject[0]);
        for(BasicDBObject ally : alliedArmies){
            Player p1 = myActivePlayers.get(ally.get(Constants.OWNER).toString());
            Army alliedArmy = new Army(p1);
            if(ally.get(Constants.INFANTRY_)!= null){
                int infantry = Integer.parseInt(ally.get(Constants.INFANTRY_).toString());
                for(int i=0; i<infantry; i++){
                    Troop t = new Troop(p, TroopType.INFANTRY);
                    alliedArmy.addTroop(t);
            }}
            if(ally.get(Constants.AUTOMATIC_)!= null){
                int automatic = Integer.parseInt(ally.get(Constants.AUTOMATIC_).toString());
                for(int i=0; i<automatic; i++){
                    Troop t = new Troop(p, TroopType.AUTOMATIC);
                    alliedArmy.addTroop(t);
            }}
            if(ally.get(Constants.ROCKETS_)!= null){
                int rockets = Integer.parseInt(ally.get(Constants.ROCKETS_).toString());
                for(int i=0; i<rockets; i++){
                    Troop t = new Troop(p, TroopType.ROCKETS);
                    alliedArmy.addTroop(t);
            }}
            if(ally.get(Constants.TANKS_)!= null){
                int tanks = Integer.parseInt(ally.get(Constants.TANKS_).toString());
                for(int i=0; i<tanks; i++){
                    Troop t = new Troop(p, TroopType.TANKS);
                    alliedArmy.addTroop(t);
            }}
            if(ally.get(Constants.IMPROVEDTANKS_)!= null){
                int impTanks = Integer.parseInt(ally.get(Constants.IMPROVEDTANKS_).toString());
                for(int i=0; i<impTanks; i++){
                    Troop t = new Troop(p, TroopType.IMPROVEDTANKS);
                    alliedArmy.addTroop(t);
            }}
            if(ally.get(Constants.PLANES_)!= null){
                int planes = Integer.parseInt(ally.get(Constants.PLANES_).toString());
                for(int i=0; i<planes; i++){
                    Troop t = new Troop(p, TroopType.PLANES);
                    alliedArmy.addTroop(t);
            }}

            terr.addAlly(p1, alliedArmy);
        }

        territories.put(terr.getPosition(), terr);
    }
    System.out.println("territories loaded");
    BasicDBList spyInfo = (BasicDBList) lastStateObject.get(Constants.SPIES);
    BasicDBObject[] spyArray = spyInfo.toArray(new BasicDBObject[0]);
    for(BasicDBObject spy : spyArray){
        String owner = spy.get(Constants.OWNER).toString();
        Player p = myActivePlayers.get(owner);
        int position = Integer.parseInt(spy.get(Constants.POSITION).toString());
        String type_string = spy.get(Constants.PREVIOUSTYPE).toString();
        int percentage = Integer.parseInt(spy.get(Constants.PERCENTAGE).toString());
        TroopType type = getTroopType(type_string);
        Spy spy_ = new Spy(p, type);
        Territory location = territories.get(position);
        spy_.setLocation(location);
        spy_.setCatchPercentage(percentage);
        ArrayList<Spy> spys = new ArrayList<Spy>();
        if(spies.containsKey(position)){
            spys = spies.get(position);
        }
        else{
            spys = new ArrayList<Spy>();

        }
        spys.add(spy_);
        spies.put(position, spys);

    }
    System.out.println("spies loaded");

    updateStateWithMoves();
    System.out.println("between methods");
    saveState(); 
    System.out.println("state saved");
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
    if(str.equals("SPIES")){
        return TroopType.SPIES;
    }
        return null;
}
//needs to be finished...
public void moveTypeMove(BasicDBObject move, Player p){
    int startInt = Integer.parseInt(move.get(Constants.START).toString());
    Territory start = territories.get(startInt);
    System.out.println(startInt);
    int endInt = Integer.parseInt(move.get(Constants.END).toString());
    Territory end = territories.get(endInt);
    System.out.println(endInt);
    String troopType = move.get(Constants.TROOPTYPE).toString();
    TroopType type = getTroopType(troopType);
    if(type.equals(TroopType.SPIES)){
        if(spies.containsKey(startInt)){
            ArrayList<Spy> spys = spies.get(startInt);
            Spy spyToMove = null; 
            int k =0;
            for(Spy spyChoice : spys){
                if(spyChoice.getOwner().equals(p)){
                    spyToMove = spyChoice;
                    System.out.println("k" + k);
                    spys.remove(k);
                    spies.put(startInt, spys);
                    break;
                }
                k++;
                System.out.println("after k loop");            
            }
      
            if(spyToMove!=null){
                 System.out.println("spy to move !=nulll");
                if(checkAdjacency(startInt, endInt)){
                    if(spyToMove.canMove()){
                        System.out.println("spy to move can move ");
                        spyToMove.setLocation(territories.get(endInt));
                        if(spies.containsKey(endInt)){
                            spys = spies.get(endInt);
                        }
                        else{
                            spys = new ArrayList<Spy>();
                        }
                        spys.add(spyToMove);
                        spies.put(endInt, spys);          
                    }
                }  
            }
        }
    }
    else{
        if(p.equals(start.getOwner())){
            if(start.getDefendingArmy().containsTroop(type)){
                start.removeTroopFromArmy(type);
            }
        }
        else{
            if(start.getAllyArmy(p)!=null){
            Army a = start.getAllyArmy(p);
            if(a.containsTroop(type)){
                a.deleteTroop(type);
                start.addAlly(p, a);
            }
            }
        }
        if(p.equals(end.getOwner())){
            end.getDefendingArmy().addTroop(type);
        }
        else{
            if(end.getAllyArmy(p)!=null){
                Army a = end.getAllyArmy(p);
                a.addTroop(type);
                end.addAlly(p, a);
            }
            else{
                Army a = new Army(p);
                a.addTroop(type);
                end.addAlly(p, a);
            }
        }
    }
        
    }
    

public boolean checkAdjacency(int start, int end){
    AdjacencyMap mapOfAdjacencies = new AdjacencyMap();
    ArrayList<Integer> adjacencies = mapOfAdjacencies.getAdjacencies(start);
    if(adjacencies.contains(end)){
        return true;
    }
    else{
        return false;
    }
}

public void moveTypeAttack(BasicDBObject move, Player p){
    System.out.println("attack");
    int startInt = Integer.parseInt(move.get(Constants.START).toString());
    Territory start = territories.get(startInt);
    int endInt = Integer.parseInt(move.get(Constants.END).toString());
    Territory end = territories.get(endInt);
    String troopType = move.get(Constants.TROOPTYPE).toString();
    TroopType type = getTroopType(troopType);
    if(start.getDefendingArmy().containsTroop(type)){
        end.addTrooptoAttacker(startInt, type, p);
        start.removeTroopFromArmy(type); 
    }
}

public void moveTypeUpgrade(BasicDBObject move, Player p){
    int position = Integer.parseInt(move.get(Constants.POSITION).toString());
    Territory position_ = territories.get(position);
    String troopType = move.get(Constants.TROOPTYPE).toString();
    String upgradeType = move.get(Constants.UPGRADETYPE).toString();
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
            if(upgradetype.equals(TroopType.SPIES)){
                Spy spy_ = new Spy(p, trooptype);
                spy_.setLocation(position_);
                spy_.setCatchPercentage(1);
                ArrayList<Spy> spys = new ArrayList<Spy>();
                if(spies.containsKey(position)){
                    spys = spies.get(position);
                }
                else{
                    spys = new ArrayList<Spy>();
                }
                spys.add(spy_);
                spies.put(position, spys);
            }
            else if(trooptype.equals(TroopType.SPIES)){
                if(spies.containsKey(position_)){
                    ArrayList<Spy> spys_ = spies.get(position_);
                    Spy spy;
                    int k = 0;
                    for(Spy s : spys_){
                        if(s.getOwner().equals(p)){
                            spys_.remove(k);
                            army.addTroop(upgradetype);
                        }
                        k++;
                    }
                }
            }
            else{
                army.addTroop(upgradetype);
            }
            position_.setDefendingArmy(army);
        }
    }
}

public void moveTypePlace(BasicDBObject move, Player p){
    int position = Integer.parseInt(move.get(Constants.POSITION).toString());
    Territory position_ = territories.get(position);
    String troopType = move.get(Constants.TROOPTYPE).toString();
    TroopType trooptype = getTroopType(troopType);
    Army army = position_.getDefendingArmy();
    if(p.getAdditionalTroops()>=0){
        army.addTroop(trooptype);
        p.setAdditionalTroops(p.getAdditionalTroops()-1);
    }
}

public void moveTypeAllign(BasicDBObject move, Player p){
    String username = move.get(Constants.ALLY).toString();
    Player ally = myActivePlayers.get(username);
    PotentialAlly k = new PotentialAlly(p, ally);
    potentialAllies.add(k);
}

public void moveTypeTrade(BasicDBObject move, Player p){
    String giver_ = move.get(Constants.GIVER).toString();
    String receiver_ = move.get(Constants.RECEIVER).toString();
    int amount = Integer.parseInt(move.get(Constants.NUMBER).toString());
    String type = move.get(Constants.TYPE).toString();
    Trade newTrade = new Trade(4, giver_, receiver_, amount, type);
    tradeOrders.add(newTrade);
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
    if(old.equals(TroopType.SPIES)){
        return 5;
    }
    if(new_.equals(TroopType.SPIES)){
        return 35;
    }
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

public void approveAlliances(){
    for(PotentialAlly p : potentialAllies){
        Player p1 = p.getProposer();
        Player p2 = p.getAccepter();
        for(PotentialAlly k : potentialAllies){
            if(k.getAccepter().equals(p1)){
                if(k.getProposer().equals(p2)){
                    p1.addAlly(p2);
                    p2.addAlly(p1);
                }
            }
        }
    }
}

public void settleTrades(){
    ArrayList<Trade> tradeListSample = tradeOrders;
    for(int i=tradeOrders.size(); i>0; i--){
        for(int j=tradeListSample.size(); j>0; j--){
            Trade t1 = tradeOrders.get(i);
            Trade t2 = tradeListSample.get(j);
            if(t1.equivalentTo(t2)){
                executeTrade(t1);
                tradeListSample.remove(i);
                break;

            }
        }
    }
}

public void executeTrade(Trade t){
    Player p1 = myActivePlayers.get(t.getGiver());
    Player p2 = myActivePlayers.get(t.getReceiver());
    TradeType type_= t.getTradeType();
    TroopType type;
    switch(type_){
        case INFANTRY:
        {       
            type = TroopType.INFANTRY;
            break;
        }
        case AUTOMATIC:         
        {       
            type = TroopType.AUTOMATIC;
            break;
        }
        case ROCKETS: 
        {       
            type = TroopType.ROCKETS;
            break;
        }
        case TANKS:
        {       
             type = TroopType.TANKS;
            break;
        }
        case IMPROVEDTANKS:
        {       
             type = TroopType.IMPROVEDTANKS;
            break;

        }
        case PLANES:
        {       
             type = TroopType.PLANES;
            break;

        }

                
        case SPIES: return;
        case INTERCEPTOR: return;
        case NUKE: return;
        case TERRITORY: return;
        case FOOD: 
            {
                int food = t.getAmount();
                p1.setFood(p1.getFood()-food);
                p2.setFood(p2.getFood()+food);
                return;

            }
        case TECHNOLOGY:
            {
                int tech = t.getAmount();
                p1.setTechnology(p1.getTechnology()-tech);
                p2.setTechnology(p2.getTechnology()+tech);
                return;
            }
        default: return;
    }
        //army stuff
        int troopsToDelete = t.getAmount();
                for(Integer position : territories.keySet()){
                    Territory terr = territories.get(position);
                    if(terr.getOwner().equals(p1)){
                        while(terr.tryToDeleteTroop(type) && troopsToDelete >0){
                            troopsToDelete --;
                        }
                        if(troopsToDelete==0){
                            return;
                        }
                    }
                }
                int troopsToAdd = t.getAmount();
                for(Integer position : territories.keySet()){
                    Territory terr = territories.get(position);
                    if(terr.getOwner().equals(p2)){
                        while(troopsToAdd >0){
                            terr.addTroop(type);
                            troopsToAdd --;
                        }
                        if(troopsToAdd==0){
                            return;
                        }
                    }
                }
    

}

public void updateStateWithMoves(){
    System.out.println("within state with moves method");
    for(String username : myActivePlayers.keySet()){
        Player p = myActivePlayers.get(username);
        System.out.println(myGameID);
        System.out.println("within state with moves method");
        DBObject lastCommittedTurn = DBHelper.getCommittedTurnForPlayerAndGame(myGameID, username);
        int technology = Integer.parseInt(lastCommittedTurn.get(Constants.TECHNOLOGY).toString());
        int technology_level = Integer.parseInt(lastCommittedTurn.get(Constants.TECHNOLOGY_LEVEL).toString());
        System.out.println("within state with moves method");
        if(technology_level != p.getTechnologyLevel()){
            System.out.println("techn level");
            int cost = computeCostOfUpgrade(p.getTechnologyLevel(), technology_level);
            if(technology >= cost){
                System.out.println("techn level if");
                p.setTechnology(technology-cost);
                p.setTechnologyLevel(technology_level);
                myActivePlayers.put(p.getName(), p);
            }
        }
        System.out.println("within state with moves method");
        BasicDBList movesList = (BasicDBList) lastCommittedTurn.get(Constants.MOVES);  
        BasicDBObject[] movesArray = movesList.toArray(new BasicDBObject[0]);
        for(BasicDBObject move : movesArray){
            int moveType = Integer.parseInt(move.get(Constants.MOVETYPE).toString());
            if(moveType == 5){
                moveTypeAllign(move, p);
                System.out.println("move type = allign");

            }
            approveAlliances();
        }
        for(BasicDBObject move : movesArray){
            int moveType = Integer.parseInt(move.get(Constants.MOVETYPE).toString());
            if(moveType == 4){
                moveTypeTrade(move, p);
                System.out.println("move type = trade");

            }
            approveAlliances();
        }

        for(BasicDBObject move : movesArray){
            int moveType = Integer.parseInt(move.get(Constants.MOVETYPE).toString());
            if(moveType == 0){
                System.out.println("move type = 0");
                moveTypeUpgrade(move, p);
            }
            else if(moveType == 1){
                moveTypeMove(move, p);
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

public ArrayList<Army> alliesForBattle(HashMap<Player, Army> map){
    ArrayList<Army> allies = new ArrayList<Army>();
    for(Player p : map.keySet()){
        allies.add(map.get(p));

    }
    return allies;
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
                Attacker opposingAttacker = attackers_crossing.get(position);
                int winner = battleCrossing(opposingAttacker.getArmy(), position, a.getArmy(), attackingFrom);
                if(winner == attackingFrom){
                    attackers.remove(attackingFrom);
                }
                else{
                    attackers_crossing.remove(position);
                }   
            }
            Player p = a.getOwner();
            if(attacks.containsKey(p)){
                Attacker a_inMap = attacks.get(p);
                a_inMap.combineAttackers(a);
                attacks.put(p, a_inMap);
            }
            else{
                attacks.put(p, a);
            }
        }



        HashMap<Player, Army> allies = territories.get(position).getAllies();

        Army winner = battle(attackersForBattle(attacks), territories.get(position).getDefendingArmy(), alliesForBattle(allies));
       // territories.get(position).setDefendingArmy(winner);
        compareSittingTroops();
        Territory t = territories.get(position);
        t.addTroop(TroopType.INFANTRY);
        t.setOwner(winner.getOwner());
        territories.put(position, t);

    }
    finalizeState();
}

public void compareSittingTroops(){
    for(Integer position : territories.keySet()){
        Territory terr = territories.get(position);
        HashMap<Player, Army> alliedArmies = terr.getAllies();
        for(Player p : alliedArmies.keySet()){
            for(Player q : alliedArmies.keySet()){
                if(!p.containsAlly(q) && !p.equals(q)){
                    Army p_army = alliedArmies.get(p);
                    Army q_army = alliedArmies.get(q);
                    Player winner = battleOnHomeGround(p_army, p, q_army, q);
                    if(winner.equals(p)){
                        alliedArmies.remove(q);
                        terr.setAllies(alliedArmies);
                    }
                    else{
                        alliedArmies.remove(p);
                        terr.setAllies(alliedArmies);
                    }
                }
            }
        }
    }
}

public Player battleOnHomeGround(Army attacker, Player attacker_player, Army defender, Player defender_player){
    while(attacker.getNumberOfTroops()>0 & defender.getNumberOfTroops()>0){

        Troop battler_1 = defender.getStrongest();
  //      System.out.println("first strongest defender is a " + battler_1.getType());
        Troop battler_2 = attacker.getWeakest();
    //    System.out.println("first weakest attacker is a " + battler_2.getType());
        double batt_1 = battler_1.battle();
        double batt_2 = battler_2.battle();
        if(batt_1 == batt_2){
      //      System.out.println("it was a tie " + batt_1);
            if(battler_1.getStrength() >= battler_2.getStrength()){
                batt_1++;
            }
            else{
                batt_2++;
            }
        }
        if(batt_1 < batt_2){
 //           System.out.println(batt_1 + " is less than  " +batt_2 + " the defender lost one troop");
            defender.deleteTroop(battler_1.getType());
            if(defender.getNumberOfTroops()==0){
                return attacker_player;
            }
        }
        else{
   //         System.out.println(batt_2 + " is less than  " + batt_1 + " the attacker lost one troop");
            attacker.deleteTroop(battler_2.getType());
            if(attacker.getNumberOfTroops()==0){
                return defender_player;
            }
        }       
        battler_1 = defender.getWeakest();
    //    System.out.println("first weakest defender is " + battler_1.getType());
        battler_2 = attacker.getStrongest();
      //  System.out.println("first strongest attacker is " + battler_2.getType());
        batt_1 = battler_1.battle();
        batt_2 = battler_2.battle();
        if(batt_1 < batt_2){
        //    System.out.println(batt_1 + " is less than  " + batt_2 + " the defender lost one troop");
            defender.deleteTroop(battler_1.getType());
            if(defender.getNumberOfTroops()==0){
                return attacker_player;
            }                
        }
        else{
        //    System.out.println(batt_1 + " is less than  " + batt_2 + " the attacker lost one troop");
            attacker.deleteTroop(battler_2.getType());
            if(attacker.getNumberOfTroops()==0){
                return defender_player;
            } 
        }
    }

    if(attacker.getNumberOfTroops()>0){
        return attacker_player;
    }
    else{
        return defender_player;
    }
      
}

public int battleCrossing(Army attacker, int attackerHome, Army defender, int defenderHome){
    while(attacker.getNumberOfTroops()>0 & defender.getNumberOfTroops()>0){

        Troop battler_1 = defender.getStrongest();
  //      System.out.println("first strongest defender is a " + battler_1.getType());
        Troop battler_2 = attacker.getWeakest();
    //    System.out.println("first weakest attacker is a " + battler_2.getType());
        double batt_1 = battler_1.battle();
        double batt_2 = battler_2.battle();
        if(batt_1 == batt_2){
      //      System.out.println("it was a tie " + batt_1);
            if(battler_1.getStrength() >= battler_2.getStrength()){
                batt_1++;
            }
            else{
                batt_2++;
            }
        }
        if(batt_1 < batt_2){
 //           System.out.println(batt_1 + " is less than  " +batt_2 + " the defender lost one troop");
            defender.deleteTroop(battler_1.getType());
            if(defender.getNumberOfTroops()==0){
                return attackerHome;
            }
        }
        else{
   //         System.out.println(batt_2 + " is less than  " + batt_1 + " the attacker lost one troop");
            attacker.deleteTroop(battler_2.getType());
            if(attacker.getNumberOfTroops()==0){
                return defenderHome;
            }
        }       
        battler_1 = defender.getWeakest();
    //    System.out.println("first weakest defender is " + battler_1.getType());
        battler_2 = attacker.getStrongest();
      //  System.out.println("first strongest attacker is " + battler_2.getType());
        batt_1 = battler_1.battle();
        batt_2 = battler_2.battle();
        if(batt_1 < batt_2){
        //    System.out.println(batt_1 + " is less than  " + batt_2 + " the defender lost one troop");
            defender.deleteTroop(battler_1.getType());
            if(defender.getNumberOfTroops()==0){
                return attackerHome;
            }                
        }
        else{
        //    System.out.println(batt_1 + " is less than  " + batt_2 + " the attacker lost one troop");
            attacker.deleteTroop(battler_2.getType());
            if(attacker.getNumberOfTroops()==0){
                return defenderHome;
            } 
        }
    }

    if(attacker.getNumberOfTroops()>0){
        return attackerHome;
    }
    else{
        return defenderHome;
    }
      
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
public Army battle(ArrayList<Attacker> attackers, Army defender, ArrayList<Army> allies){
    
    Collections.shuffle(attackers);




    while(attackers.size()>0){
        System.out.println("battle has begun");
        for(int i=attackers.size()-1; i>=0; i--){

            Army attacker = attackers.get(i).getArmy();
            Army currentDefender = defender;
            Troop battler_1 = defender.getStrongest();
            for(Army a : allies){
                Troop k = a.getStrongest();
                if(k.findStrength()>battler_1.findStrength()){
                    if(!k.getOwner().containsAlly(attacker.getOwner())){
                        battler_1 = k; 
                        currentDefender = a;
                    }
                 

                }
            }
            if(battler_1==null){
                    System.out.println("the defender lost the battle because he had no army");
                    defender = attacker;
      //              System.out.println("the new defender is " + attacker.getName());
                    attackers.remove(i);
                    if(attackers.size()==0){
                        System.out.println("the battle is over");
                        return defender;
                    }
                    continue;       
            }
          //  System.out.println("first strongest defender is a " + battler_1.getType());
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
            Army currentDefender = defender;
            System.out.println("first weakest defender is " + battler_1.getType());
            Troop battler_2 = attacker.getStrongest();
            for(Army a : allies){
                Troop k = a.getWeakest();
                if(k.findStrength()<battler_1.findStrength()){
                    if(!k.getOwner().containsAlly(attacker.getOwner())){
                        battler_1 = k; 
                        System.out.println("first weakest defender is " + battler_1.getType());
                        currentDefender = a;
                    }
                 

                }
            }
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
    for(Integer k : spies.keySet()){
        ArrayList<Spy> spys_ = spies.get(k);
        if(spys_.size()>0){
            int x = 0;
            for(Spy spy : spys_){
                Player p1 = spy.getOwner();
                Player p2 = spy.getLocation().getOwner();
                if(p1.equals(p2)){
                    spy.setCatchPercentage(1);
                }
                else{

                    boolean caught = spy.spyCaughtOrNot();
                    System.out.println("spy was caught ? "+caught);
                    if(caught){
                        spys_.remove(x);
                        spies.put(k, spys_);
                        break;
                    }
                }
                x++;
            }
        }
    }

    System.out.println("finalize");
    for(String username: myActivePlayers.keySet()){
         System.out.println("finalize players");
        Player player = myActivePlayers.get(username);
        int food = player.getFood();
        for(Integer position : territories.keySet()){
            Territory t = territories.get(position);
            if(t.getOwner().equals(player)){
                int newFood = player.getFood() + t.getFood();
                int newTech = t.getTechnology() + player.getTechnology();
                player.setTechnology(newTech);
                player.setFood(newFood);
            }
        }
    }
    for(String username: myActivePlayers.keySet()){
        System.out.println("players again");
        Player player = myActivePlayers.get(username);
        int troopsToDelete = 0;
        int food = player.getFood();
        for(Integer position : territories.keySet()){
            Territory t = territories.get(position);

            if(t.getOwner().equals(player)){
                int troops = t.getDefendingArmy().getNumberOfTroops();
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

public void visibleTerritories(){
    AdjacencyMap adjacencies = new AdjacencyMap();
    for(String username : myActivePlayers.keySet()){
        Player p = myActivePlayers.get(username);
        TreeSet<Integer> set = new TreeSet<Integer>();
        visibleTerritoriesForEachPlayer.put(p,set);
        
    }
    for(Integer position : territories.keySet()){
        Territory t = territories.get(position);
        Player p = t.getOwner();
        TreeSet<Integer> ints = visibleTerritoriesForEachPlayer.get(p);
        ints.add(position);
        ArrayList<Integer> adjacentPositions = adjacencies.getAdjacencies(position);
        for(Integer positionAdjacent : adjacentPositions){
            ints.add(positionAdjacent);
        } 
        visibleTerritoriesForEachPlayer.put(p, ints);
    }
    for(Integer spyposition : spies.keySet()){
        ArrayList<Spy> spy = spies.get(spyposition);
        for(Spy s : spy){
            Player p = s.getOwner();
            TreeSet<Integer> ints = visibleTerritoriesForEachPlayer.get(p);
            ints.add(spyposition);
            visibleTerritoriesForEachPlayer.put(p, ints);
        }

    }
    //add all visible territories to the allies
    for(String username : myActivePlayers.keySet()){
        Player p = myActivePlayers.get(username);
        ArrayList<Player> allies = p.getAllies();
        TreeSet<Integer> visibleTerrs = visibleTerritoriesForEachPlayer.get(p);
        for(Player q : allies){
            TreeSet<Integer> visibleToAlly = visibleTerritoriesForEachPlayer.get(q);
            for(Integer k : visibleToAlly){
                visibleTerrs.add(k);
            }   
        }
        visibleTerritoriesForEachPlayer.put(p,visibleTerrs);
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
            case SPIES: return "SPIES";
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
    visibleTerritories();
    System.out.println("saving");
    DBCollection state = DBHelper.getStateCollection();
    BasicDBObject turn_doc = new BasicDBObject();
    turn_doc.append(Constants.GAME_ID, myGameID);
    long millis = System.currentTimeMillis();
    turn_doc.append(Constants.TIMESTAMP, new Long(millis));
    turn = turn + 1;
    turn_doc.append(Constants.TURN, turn);
    turn_doc.append(Constants.NUM_PLAYERS, myActivePlayers.size());
    List<BasicDBObject> territory_list = new ArrayList<BasicDBObject>();
    for(Integer position : territories.keySet()){
        Territory t = territories.get(position);
        BasicDBObject territory_doc = new BasicDBObject();
        int position_ = t.getPosition();
        territory_doc.append(Constants.POSITION, position_);
        Player owner = territories.get(position).getOwner();
        territory_doc.append(Constants.OWNER, owner.getName());
        HashMap<TroopType, ArrayList<Troop>> a = t.getDefendingArmy().getTroops();
        for(TroopType type : a.keySet()){
            ArrayList<Troop> troops = a.get(type);
            int size = troops.size();
            territory_doc.append(typeToString(type), size);
        }
        territory_doc.append(Constants.FOOD, t.getFood());
        territory_doc.append(Constants.TECHNOLOGY, t.getTechnology());
        List<BasicDBObject> ally_list = new ArrayList<BasicDBObject>();
        HashMap<Player, Army> alliedTroopMap = t.getAllies();

        for(Player p : alliedTroopMap.keySet()){
            BasicDBObject ally_doc = new BasicDBObject();
            ally_doc.append(Constants.OWNER, p.getName());
            HashMap<TroopType, ArrayList<Troop>> troopsOfType = alliedTroopMap.get(p).getTroops();
            for(TroopType type : troopsOfType.keySet()){
                ArrayList<Troop> troops = troopsOfType.get(type);
                int size = troops.size();
                ally_doc.append(typeToString(type), size);
            }
            ally_list.add(ally_doc);

        }        
        territory_doc.append(Constants.ALLIEDTROOPS, ally_list);
        territory_list.add(territory_doc);
    }
    turn_doc.append(Constants.TERRITORIES, territory_list);
    WaitingRoom wr = new WaitingRoom();
    wr = wr.getWaitingRoom(myGameID);
    ArrayList<String> usernames = wr.getUsernames();
    List<BasicDBObject> player_list = new ArrayList<BasicDBObject>();
    for(String username : usernames){
        BasicDBObject player_doc = new BasicDBObject();
        Player p = myActivePlayers.get(username);
        if(playerOwnsATerritory(p)){
            player_doc.append(Constants.OWNER, p.getName());
            player_doc.append(Constants.LEVEL, p.getTechnologyLevel());
            player_doc.append(Constants.FOOD, p.getFood());
            player_doc.append(Constants.TECHNOLOGY, p.getTechnology());
            player_doc.append(Constants.ADDITIONALINFANTRY, 0);
            List<BasicDBObject> otherPlayer_list = new ArrayList<BasicDBObject>();
            for(String username_ : wr.getUsernames()){
                System.out.println(username_ + "in loop");
                if(username_.equals(p.getName())){
                    System.out.println(username_ + " equal");
                    continue;
                }
                else{
                    BasicDBObject otherPlayer_doc = new BasicDBObject();
                    otherPlayer_doc.append(Constants.OWNER, username_);
                    System.out.println("username is " + username_);
                    otherPlayer_doc.append(Constants.LEVEL, myActivePlayers.get(username_).getTechnologyLevel());
                    otherPlayer_list.add(otherPlayer_doc);
                }
            }
            player_doc.append(Constants.HIGHESTTECHNOLOGY, otherPlayer_list);
            System.out.println(visibleTerritoriesForEachPlayer.size() + "size");
            player_doc.append(Constants.TERRITORIESVISIBLE, visibleTerritoriesForEachPlayer.get(p));
            player_doc.append(Constants.ALLIES, p.getAllyNames());
            player_list.add(player_doc);   
        }

    }
    turn_doc.append(Constants.PLAYERINFO, player_list);
    List<BasicDBObject> spy_list = new ArrayList<BasicDBObject>();
    for(Integer k : spies.keySet()){
        ArrayList<Spy> spys_ = spies.get(k);
        if(spys_.size()>0){
            for(Spy spy : spys_){
            BasicDBObject spy_object = new BasicDBObject();
            spy_object.append(Constants.OWNER, spy.getOwner().getName());
            spy_object.append(Constants.POSITION, spy.getPosition());
            spy_object.append(Constants.PERCENTAGE, spy.getCatchPercentage());
            spy_object.append(Constants.PREVIOUSTYPE, spy.getType().toString());              
            spy_list.add(spy_object);
            }
    
        }
    }
    turn_doc.append(Constants.SPIES, spy_list);
    state.insert(turn_doc);

    return;
}

}