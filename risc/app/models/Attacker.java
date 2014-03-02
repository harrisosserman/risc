package models;

import java.util.*;
//test

public class Attacker{

	private Player myOwner;
	private Army myArmy;
	private int myLocation;
	private int myHome;

	public Attacker(Player owner, Army army, int territory, int home){
		myArmy = army;
		myOwner = owner; 
		myHome = home;
		myLocation = territory;
	}
	public Attacker(Player owner, int territory){
		myArmy = new Army(owner);
		myOwner = owner; 
		myLocation = territory;
	}
	public String getName(){
		return myOwner.getName();
	}

	public void setTerritory(int territory){
		myLocation = territory;
	}

	public int getTerritory(){
		return myLocation;
	}

	public void setOwner(Player owner){
		myOwner = owner;
	}

	public Player getOwner(){
		return myOwner;
	}

	public void setHome(int home){
		myHome = home;
	}

	public int getHome(){
		return myHome;
	}

	public Army getArmy(){
		return myArmy;
	}

	public void combineAttackers(Attacker a2){
    	ArrayList<Troop> a = a2.getArmy().getArrayOfTroops();
   		myArmy.addTroops(a);
	}

	public void addTroop(TroopType t){
		myArmy.addTroop(t);
	}
	
	public void addTroop(Troop t){
		myArmy.addTroop(t);
	}

	}
