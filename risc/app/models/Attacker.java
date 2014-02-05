package models;

import java.util.*;

public class Attacker{

	private int myOwner;
	private int myStrength;
	private int myLocation;
	private int myHome;

	public Attacker(int owner, int size, int territory, int home){
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

	public void setHome(int home){
		myHome = home;
	}

	public int getHome(){
		return myHome;
	}

}