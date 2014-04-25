package models;

import java.util.*;
import models.Attacker;

public class Territory{

	private Player myOwner;
	private int myTroops;
	private int myPosition;
	private int myFood;
	private int myTechnology;
	private HashMap<Integer, Attacker> attackers;
	private Army myArmy;
	private HashMap<Player, Army> alliedTroops;
	

	public Territory(int position, Player owner, int food, int technology){
		myPosition = position;
		myOwner = owner;
		myTechnology = technology;
		myFood = food;
		myArmy = new Army(myOwner);
		attackers = new HashMap<Integer, Attacker>();
		alliedTroops = new HashMap<Player, Army>();

	}

	public void addAlly(Player p, Army a){
		alliedTroops.put(p, a);
	}

	public Army getAllyArmy(Player p){
		return alliedTroops.get(p);
	}
    
    public HashMap<Player, Army> getAllies(){
        return alliedTroops;
    }

	public int getFood(){
		return myFood;
	}

	public int getNumberOfTroops(){
		return myArmy.getNumberOfTroops();
	}

	public int getTechnology(){
		return myTechnology;
	}

	public int getPosition(){
		return myPosition;
	}

	public Player getOwner(){
		return myOwner;
	}

	public void setOwner(Player owner){
		myOwner = owner;
	}

	public Army getDefendingArmy(){
		return myArmy;
	}

	public void removeTroopFromArmy(TroopType type){
		myArmy.deleteTroop(type);
	}

	public void setDefendingArmy(Army army_){
		myArmy = army_;
	}

	public void addTroop(Troop t){
		myArmy.addTroop(t.getType());
	}

	public void addTroop(TroopType type){
		myArmy.addTroop(type);
	}

    public boolean tryToDeleteTroop(TroopType type){
    	if(myArmy.containsTroop(type)){
    		myArmy.deleteTroop(type);
    		return true;
    	}
    	else{
    		return false;
    	}

    }

	public void addTrooptoAttacker(Integer position, TroopType t, Player p){
		if(attackers.containsKey(position)){
			Attacker a = attackers.get(position);
			a.addTroop(t);
			attackers.put(position, a);
		}
		else{
			Attacker a = new Attacker(p, position);
			a.addTroop(t);
			attackers.put(position, a);

		}
    }    

	public HashMap<Integer, Attacker> getAttackers(){
		return attackers;
	}

	public void clearAttackers(){
		attackers = new HashMap<Integer, Attacker>();
	}
}