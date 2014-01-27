package models;

import java.util.*;

public class Territory{

	private Player myOwner;
	private Army myDefendingArmy;
	private int myPosition;

	public Territory(int position, Player owner){
		myPosition = position;
		myOwner = owner;
		myDefendingArmy = new Army(0);
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
		return myDefendingArmy;
	}

	public void setDefendingArmy(Army army){
		myDefendingArmy = army;
	}

	public String toString(){
		return "Pos:" + myPosition + 
			" Def str:" + myDefendingArmy.getTotalStrength() + 
			" Owner:" + myOwner.getName();
	}
}