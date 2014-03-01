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
	

	public Territory(int position, Player owner, int food, int technology){
		myPosition = position;
		myOwner = owner;
		myTechnology = technology;
		myFood = food;
		myArmy = new Army(myOwner);
		attackers = new HashMap<Integer, Attacker>();

	}
	public int getFood(){
		return myFood;
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

	public void setDefendingArmy(Army army_){
		myArmy = army_;
	}

	public void addTroop(Troop t){
		myArmy.addTroop(t.getType(), t);
	}

	public Attacker getAttacker(int index){
		return attackers.get(index);
	}

	public void addAttacker(Integer position, Attacker a){
		ArrayList<Attacker> attackers_ = attackers.get(position);
	    
    }

	public ArrayList<Attacker> getAttackers(){
		return attackers;
	}

	public void clearAttackers(){
		attackers = new ArrayList<Attacker>();
	}
}