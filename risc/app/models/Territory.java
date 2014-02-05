package models;

import java.util.*;
import models.Troop;

public class Territory{

	private int myOwner;
	private int myTroops;
	private int myPosition;

	public Territory(int position, int owner, int troops){
		myPosition = position;
		myOwner = owner;
		myTroops = troops;
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
}