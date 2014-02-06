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
<<<<<<< HEAD

=======
>>>>>>> 1faca581c899ac64f0b16d21edf41b5f09dcb019
	public int getOwner(){
		return myOwner;
	}

	public void setOwner(int owner){
		myOwner = owner;
	}

	public int getDefendingArmy(){
		return myTroops;
<<<<<<< HEAD
	}

	public void setDefendingArmy(int troops){
		myTroops = troops;
	}


=======
	}

	public void setDefendingArmy(int troops){
		myTroops = troops;
	}
>>>>>>> 1faca581c899ac64f0b16d21edf41b5f09dcb019
}