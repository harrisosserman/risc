package models;

import java.util.*;
//test

public class Attacker{

	private Player myOwner;
	private Army myStrength;
	private int myLocation;
	private int myHome;

	public Attacker(Player owner, Army size, int territory, int home){
		myStrength = size;
		myOwner = owner; 
		myHome = home;
		myLocation = territory;
	}

	public void setTerritory(int territory){
		myLocation = territory;
	}

	public int getTerritory(){
		return myLocation;
	}

	public Army getStrength(){
		return myStrength;
	}

	public void setStrength(Army size){
		myStrength = size;
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

	}
