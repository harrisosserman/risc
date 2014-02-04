package models;

import java.util.*;

public class Attacker{

	private int myOwner;
	private int myStrength;
	private int myLocation;

	public Attacker(int owner, int size, int territory){
		myStrength = size;
		myOwner = owner; 
	}

	public void setTerritory(int territory){
		myLocation = territory;
	}

	public int getStrength(){
		return myStrength;
	}

	public void setStrength(int size){
		myStrength = size;
	}

	public void setOwner(int owner){
		myOwner = owner;
	}

	public int getOwner(){
		return myOwner;
	}

}