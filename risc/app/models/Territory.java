package models;

import java.util.*;
import models.Attacker;

public class Territory{

	private int myOwner;
	private int myTroops;
	private int myPosition;
	private int myFood;
	private int myTechnology;
	private ArrayList<Attacker> attackers;
	

	public Territory(int position, int owner, int troops, int food, int technology){
		myPosition = position;
		myOwner = owner;
		myTroops = troops;
		myTechnology = technology;
		myFood = food;
		attackers = new ArrayList<Attacker>();

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

	public int getOwner(){
		return myOwner;
	}

	public void setOwner(int owner){
		myOwner = owner;
	}

	public int getDefendingArmy(){
		return myTroops;
	}

	public void setDefendingArmy(int troops){
		myTroops = troops;
	}

	public Attacker getAttacker(int index){
		return attackers.get(index);
	}

	public void addAttacker(Attacker a){
		attackers.add(a);
	}

	public ArrayList<Attacker> getAttackers(){
		return attackers;
	}

	public void clearAttackers(){
		attackers = new ArrayList<Attacker>();
	}
}